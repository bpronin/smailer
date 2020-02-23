package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.GoogleDrive;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

import static com.bopr.android.smailer.GeoCoordinates.coordinatesOf;
import static com.bopr.android.smailer.Settings.PREF_SYNC_TIME;

public class Synchronizer {

    private static final Logger log = LoggerFactory.getLogger("SyncAdapter");

    private static final String META_FILE = "meta.json";
    private static final String DATA_FILE = "data.json";

    private final Database database;
    private final Settings settings;
    private final GoogleDrive drive;

    public Synchronizer(Context context, Account account, Database database) {
        this.database = database;
        settings = new Settings(context);
        drive = new GoogleDrive(context, account);
    }

    public void sync() throws IOException {
        MetaData meta = getMetaData();
        MetaData remoteMeta = drive.download(META_FILE, MetaData.class);
        if (remoteMeta == null || meta.syncTime >= remoteMeta.syncTime) {
            upload();
        } else {
            download();
        }
    }

    public void download() throws IOException {
        SyncData data = drive.download(DATA_FILE, SyncData.class);
        if (data != null) {
            putData(data);

            log.debug("Downloaded remote data");
        } else {
            log.debug("No remote data");
        }
    }

    public void upload() throws IOException {
        drive.upload(META_FILE, getMetaData());
        drive.upload(DATA_FILE, getData());

        log.debug("Uploaded local data");
    }

    @NonNull
    private MetaData getMetaData() {
        MetaData meta = new MetaData();
        meta.syncTime = settings.getLong(PREF_SYNC_TIME, 0);
        return meta;
    }

    @NonNull
    private SyncData getData() {
        final SyncData data = new SyncData();

        PhoneEventFilter filter = settings.getFilter();
        data.phoneBlacklist = filter.getPhoneBlacklist();
        data.phoneWhitelist = filter.getPhoneWhitelist();
        data.textBlacklist = filter.getTextBlacklist();
        data.textWhitelist = filter.getTextWhitelist();

        data.events = new ArrayList<>();
        database.getEvents().forEach(event -> data.events.add(eventToData(event)));

        return data;
    }

    private void putData(@NonNull SyncData data) {
        for (SyncData.Event event : data.events) {
            database.putEvent(dataToEvent(event));
        }

        PhoneEventFilter filter = settings.getFilter();
        filter.setPhoneWhitelist(data.phoneWhitelist);
        filter.setPhoneBlacklist(data.phoneBlacklist);
        filter.setTextWhitelist(data.textWhitelist);
        filter.setTextBlacklist(data.textBlacklist);
        settings.edit().putFilter(filter).apply();
    }

    @NonNull
    private SyncData.Event eventToData(@NonNull PhoneEvent event) {
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

        GeoCoordinates coordinates = event.getLocation();
        if (coordinates != null) {
            data.latitude = coordinates.getLatitude();
            data.longitude = coordinates.getLongitude();
        }
        return data;
    }

    @NonNull
    private PhoneEvent dataToEvent(@NonNull SyncData.Event data) {
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
        event.setLocation(coordinatesOf(data.latitude, data.longitude));
        return event;
    }


}


