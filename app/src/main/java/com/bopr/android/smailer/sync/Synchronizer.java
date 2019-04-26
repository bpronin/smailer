package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.PhoneEvent;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

class Synchronizer {

    private static final String FILE_EVENTS = "events.json";

    private final GoogleDrive drive;
    private final Database database;

    Synchronizer(Context context, Account account, Database database) {
        drive = new GoogleDrive(context, account);
        this.database = database;
    }

    void execute() throws IOException {
        List<PhoneEvent> events = download();
        for (PhoneEvent event : events) {
            database.putEvent(event);
        }
        upload(database.getEvents().toList());
    }

    @NonNull
    private List<PhoneEvent> download() throws IOException {
        List<PhoneEvent> events = new ArrayList<>();
        InputStream stream = drive.open(FILE_EVENTS);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            parser.parseArrayAndClose(events, PhoneEvent.class);
        }
        return events;
    }

    private void upload(@NonNull List<PhoneEvent> events) throws IOException {
        Writer writer = new StringWriter();
        JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer);
        generator.serialize(events);
        generator.flush();

        String data = writer.toString();
        drive.write(FILE_EVENTS, data);

        generator.close();
    }
}
