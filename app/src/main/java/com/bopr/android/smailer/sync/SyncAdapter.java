package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

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

import static com.bopr.android.smailer.Settings.PREF_SYNC_TIME;

/**
 * Handle the transfer of data between a server and an app, using the Android sync adapter framework.
 * <p>
 * Required by synchronization framework.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
/* To debug it (to put breakpoints) remove android:process=":sync" from AndroidManifest */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final Logger log = LoggerFactory.getLogger("SyncAdapter");

    private static final String META_FILE = "meta.json";
    private static final String DATA_FILE = "data.json";

    private final Database database;
    private final Settings settings;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.settings = new Settings(context);
        this.database = new Database(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        try {
            sync(getContext(), account);
        } catch (Exception x) {
            log.warn("Synchronization failed ", x);
        }
    }

    public void sync(Context context, Account account) throws IOException {
        GoogleDrive drive = new GoogleDrive(context, account);
        MetaData meta = getMetaData();
        MetaData remoteMeta = drive.download(META_FILE, MetaData.class);
        if (remoteMeta == null || meta.syncTime >= remoteMeta.syncTime) {
            upload(drive);
        } else {
            download(drive);
        }
    }

    public void download(GoogleDrive drive) throws IOException {
        SyncData data = drive.download(DATA_FILE, SyncData.class);
        if (data != null) {
            putData(data);

            log.debug("Downloaded remote data");
        } else {
            log.debug("No remote data");
        }
    }

    public void upload(GoogleDrive drive) throws IOException {
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
        database.getEvents().forEach(new Consumer<PhoneEvent>() {

            @Override
            public void accept(PhoneEvent event) {
                data.events.add(eventToData(event));
            }
        });

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
        settings.putFilter(filter);
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
        data.latitude = event.getLocation().getLatitude();
        data.longitude = event.getLocation().getLongitude();
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
        event.setLocation(new GeoCoordinates(data.latitude, data.longitude));
        return event;
    }


}


