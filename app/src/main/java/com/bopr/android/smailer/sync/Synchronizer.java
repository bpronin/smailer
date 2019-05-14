package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.Settings;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import static com.bopr.android.smailer.Settings.KEY_SYNC_TIME;
import static java.lang.System.currentTimeMillis;

class Synchronizer {

    private static final String FILENAME = "data.json";

    private final GoogleDrive drive;
    private final Database database;
    private final Context context;
    private final Settings settings;

    Synchronizer(Context context, Account account, Database database, Settings settings) {
        this.context = context;
        this.settings = settings;
        this.database = database;
        drive = new GoogleDrive(context, account);
    }

    Synchronizer(Context context, Account account) {
        this(context, account, new Database(context), new Settings(context));
    }

    void synchronize() throws IOException {
//        setLastSyncTime(0);
        SyncDto remoteData = downloadData();
        if (remoteData == null || remoteData.time <= getLastSyncTime()) {
            SyncDto localData = createData();
            uploadData(localData);
            setLastSyncTime(localData.time);
        } else {
            applyData(remoteData);
            setLastSyncTime(remoteData.time);
        }
    }

    private SyncDto createData() {
        PhoneEventFilter filter = settings.getFilter();

        SyncDto data = new SyncDto();
        data.time = currentTimeMillis();
        data.phoneBlacklist = filter.getPhoneBlacklist();
        data.phoneWhitelist = filter.getPhoneWhitelist();
        data.textBlacklist = filter.getTextBlacklist();
        data.textWhitelist = filter.getTextWhitelist();
        data.events = database.getEvents().toList();

        return data;
    }

    private void applyData(SyncDto data) {
        for (PhoneEvent event : data.events) {
//            if (event.getRecipient() == null){
//                event.setRecipient(AndroidUtil.devicePhoneNumber(context));
//            }
            database.putEvent(event);
        }

        PhoneEventFilter filter = settings.getFilter();
        filter.getPhoneWhitelist().addAll(data.phoneWhitelist);
        filter.getPhoneBlacklist().addAll(data.phoneBlacklist);
        filter.getTextWhitelist().addAll(data.textWhitelist);
        filter.getTextBlacklist().addAll(data.textBlacklist);
        settings.putFilter(filter);
    }

    @Nullable
    private SyncDto downloadData() throws IOException {
        InputStream stream = drive.open(FILENAME);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            return parser.parseAndClose(SyncDto.class);
        }
        return null;
    }

    private void uploadData(@NonNull SyncDto data) throws IOException {
        Writer writer = new StringWriter();
        JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer);
        generator.serialize(data);
        generator.flush();
        drive.write(FILENAME, writer.toString());
        generator.close();
    }

    private long getLastSyncTime() {
        return settings.getLong(KEY_SYNC_TIME, 0);
    }

    private void setLastSyncTime(long time) {
        settings.edit().putLong(KEY_SYNC_TIME, time).apply();
    }

}
