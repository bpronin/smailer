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
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bopr.android.smailer.Settings.KEY_PREF_SYNC_ITEMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_SYNC_EVENTS;
import static com.bopr.android.smailer.Settings.VAL_PREF_SYNC_FILTER_LISTS;

class Synchronizer {

    private static final String FILE_EVENTS = "events.json";
    private static final String FILE_FILTERS = "filters.json";

    private final GoogleDrive drive;
    private final Database database;
    private final Settings settings;

    Synchronizer(Context context, Account account, Database database, Settings settings) {
        this.settings = settings;
        this.database = database;
        drive = new GoogleDrive(context, account);
    }

    void execute() throws IOException {
        Set<String> items = settings.getStringSet(KEY_PREF_SYNC_ITEMS, ImmutableSet.<String>of());

        if (items.contains(VAL_PREF_SYNC_EVENTS)) {
            List<PhoneEvent> events = downloadEvents();
            for (PhoneEvent event : events) {
                database.putEvent(event);
            }
            uploadJson(FILE_EVENTS, database.getEvents().toList());
        }

        if (items.contains(VAL_PREF_SYNC_FILTER_LISTS)) {
            PhoneEventFilter filter = downloadFilters();
            if (filter != null) {
                mergeFilter(filter, settings);
            }
            uploadJson(FILE_FILTERS, settings.getFilter());
        }
    }

    private void mergeFilter(PhoneEventFilter newFilter, Settings settings) {
        PhoneEventFilter filter = settings.getFilter();
        filter.getPhoneWhitelist().addAll(newFilter.getPhoneWhitelist());
        filter.getPhoneBlacklist().addAll(newFilter.getPhoneBlacklist());
        filter.getTextWhitelist().addAll(newFilter.getTextWhitelist());
        filter.getTextBlacklist().addAll(newFilter.getTextBlacklist());
        settings.putFilter(filter);
    }

    @NonNull
    private List<PhoneEvent> downloadEvents() throws IOException {
        List<PhoneEvent> events = new ArrayList<>();
        InputStream stream = drive.open(FILE_EVENTS);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            parser.parseArrayAndClose(events, PhoneEvent.class);
        }
        return events;
    }

    @Nullable
    private PhoneEventFilter downloadFilters() throws IOException {
        InputStream stream = drive.open(FILE_FILTERS);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            return parser.parseAndClose(PhoneEventFilter.class);
        }
        return null;
    }

    private void uploadJson(String filename, @NonNull Object value) throws IOException {
        Writer writer = new StringWriter();
        JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer);
        generator.serialize(value);
        generator.flush();

        String data = writer.toString();
        drive.write(filename, data);

        generator.close();
    }

}
