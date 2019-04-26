package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.PhoneEvent;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

class Synchronizer {

    private static final String FILE_EVENTS = "events.json";

    private final GoogleDrive drive;
    private final Database database;

    public Synchronizer(Context context, Account account, Database database) {
        drive = new GoogleDrive(context, account);
        this.database = database;
    }

    public void execute() throws IOException {
        download();
//        merge();
        //  upload();
    }

    private void download() throws IOException {
        InputStream stream = drive.open(FILE_EVENTS);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            List<PhoneEvent> events = new ArrayList<>();
            parser.parseArrayAndClose(events, PhoneEvent.class);
            stream.close();

            for (PhoneEvent event : events) {
                database.putEvent(event);
            }
        }
    }

    private void upload() throws IOException {
        StringWriter writer = new StringWriter();
        serializeEvents(writer);
        String data = writer.toString();
        drive.write(FILE_EVENTS, data);
        writer.close();
    }

    private void serializeEvents(StringWriter writer) throws IOException {
        JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer);
        generator.enablePrettyPrint();
        generator.serialize(database.getEvents().asList());
        generator.flush();
    }
}
