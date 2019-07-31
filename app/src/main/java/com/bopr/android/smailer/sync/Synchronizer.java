package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.Settings;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static com.bopr.android.smailer.Settings.KEY_SYNC_TIME;
import static com.bopr.android.smailer.util.Util.requireNonNull;

class Synchronizer {

    private static final Logger log = LoggerFactory.getLogger("Synchronizer");

    private static final String META_FILE = "meta.json";
    private static final String DATA_FILE = "data.json";

    private final GoogleDrive drive;
    private final Database database;
    private final Settings settings;

    Synchronizer(Context context, Account account, Database database, Settings settings) {
        this.settings = settings;
        this.database = database;
        drive = new GoogleDrive(context, account);
    }

    Synchronizer(Context context, Account account) {
        this(context, account, new Database(context), new Settings(context));
    }

    void synchronize() throws IOException {
        MetaData meta = getMetaData();
        MetaData remoteMeta = download(META_FILE, MetaData.class);
        if (remoteMeta == null || meta.syncTime >= remoteMeta.syncTime) {
            upload(META_FILE, meta);
            upload(DATA_FILE, getData());

            log.debug("Update remote data");
        } else {
            SyncData data = requireNonNull(download(DATA_FILE, SyncData.class));
            putData(data);

            log.debug("Update local data");
        }
    }

    @NonNull
    private MetaData getMetaData() {
        MetaData meta = new MetaData();
        meta.syncTime = settings.getLong(KEY_SYNC_TIME, 0);
        return meta;
    }

    @NonNull
    private SyncData getData() {
        SyncData data = new SyncData();

        PhoneEventFilter filter = settings.getFilter();
        data.phoneBlacklist = filter.getPhoneBlacklist();
        data.phoneWhitelist = filter.getPhoneWhitelist();
        data.textBlacklist = filter.getTextBlacklist();
        data.textWhitelist = filter.getTextWhitelist();

        final List<SyncData.Event> events = new ArrayList<>();
        database.getEvents().forEach(new Consumer<PhoneEvent>() {

            @Override
            public void accept(PhoneEvent event) {
                events.add(serializeEvent(event));
            }
        });
        data.events = events;

        return data;
    }

    private void putData(@NonNull SyncData data) {
        for (SyncData.Event event : data.events) {
//            if (event.getRecipient() == null){
//                event.setRecipient(AndroidUtil.devicePhoneNumber(context));
//            }
            database.putEvent(deserializeEvent(event));
        }

        PhoneEventFilter filter = settings.getFilter();
        filter.setPhoneWhitelist(data.phoneWhitelist);
        filter.setPhoneBlacklist(data.phoneBlacklist);
        filter.setTextWhitelist(data.textWhitelist);
        filter.setTextBlacklist(data.textBlacklist);
        settings.putFilter(filter);
    }

    @NonNull
    private SyncData.Event serializeEvent(@NonNull PhoneEvent event) {
        SyncData.Event data = new SyncData.Event();
        data.state = event.getState();
        data.phone = event.getPhone();
        data.text = event.getText();
        data.incoming = event.isIncoming();
        data.missed = event.isMissed();
        data.details = event.getDetails();
        data.startTime = event.getStartTime();
        data.endTime = event.getEndTime();
        data.recipient = event.getRecipient();
        data.latitude = event.getLocation().getLatitude();
        data.longitude = event.getLocation().getLongitude();
        return data;
    }

    @NonNull
    private PhoneEvent deserializeEvent(@NonNull SyncData.Event data) {
        PhoneEvent event = new PhoneEvent();
        event.setState(data.state);
        event.setPhone(data.phone);
        event.setText(data.text);
        event.setIncoming(data.incoming);
        event.setMissed(data.missed);
        event.setStartTime(data.startTime);
        event.setEndTime(data.endTime);
        event.setDetails(data.details);
        event.setRecipient(data.recipient);
        event.setLocation(new GeoCoordinates(data.latitude, data.longitude));
        return event;
    }

    @Nullable
    private <T> T download(@NonNull String filename, @NonNull Class<? extends T> objectClass) throws IOException {
        InputStream stream = drive.open(filename);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            return parser.parseAndClose(objectClass);
        } else {
            return null;
        }
    }

    private void upload(@NonNull String filename, @NonNull Object object) throws IOException {
        Writer writer = new StringWriter();
        JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer);
        generator.serialize(object);
        generator.flush();
        drive.write(filename, writer.toString());
        generator.close();
    }
}
