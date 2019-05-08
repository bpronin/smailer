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

class Synchronizer {

    private static final String FILENAME = "data.json";

    private final GoogleDrive drive;
    private final Database database;
    private final Settings settings;

    Synchronizer(Context context, Account account, Database database, Settings settings) {
        this.settings = settings;
        this.database = database;
        drive = new GoogleDrive(context, account);
    }

    void execute() throws IOException {
        SyncData remoteData = downloadData();
        SyncData localData = createData();
        if (remoteData != null && remoteData.time > localData.time) {
            applyData(remoteData);
        } else {
            uploadData(localData);
        }
    }

    private SyncData createData() {
        PhoneEventFilter filter = settings.getFilter();

        SyncData data = new SyncData();
        data.time = settings.getLastSyncTime();
        data.phoneBlacklist = filter.getPhoneBlacklist();
        data.phoneWhitelist = filter.getPhoneWhitelist();
        data.textBlacklist = filter.getTextBlacklist();
        data.textWhitelist = filter.getTextWhitelist();
        data.events = database.getEvents().toList();

        return data;
    }

    private void applyData(SyncData data) {
        for (PhoneEvent event : data.events) {
            database.putEvent(event);
        }

        PhoneEventFilter filter = settings.getFilter();
        filter.getPhoneWhitelist().addAll(data.phoneWhitelist);
        filter.getPhoneBlacklist().addAll(data.phoneBlacklist);
        filter.getTextWhitelist().addAll(data.textWhitelist);
        filter.getTextBlacklist().addAll(data.textBlacklist);
        settings.putFilter(filter);

        settings.setLastSyncTime(data.time);
    }

    @Nullable
    private SyncData downloadData() throws IOException {
        InputStream stream = drive.open(FILENAME);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            return parser.parseAndClose(SyncData.class);
        }
        return null;
    }

    private void uploadData(@NonNull SyncData data) throws IOException {
        Writer writer = new StringWriter();
        JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer);
        generator.serialize(data);
        generator.flush();
        drive.write(FILENAME, writer.toString());
        generator.close();
    }

}
