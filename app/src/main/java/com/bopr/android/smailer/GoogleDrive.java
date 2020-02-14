package com.bopr.android.smailer;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2;
import static com.google.api.services.drive.DriveScopes.DRIVE_APPDATA;

/**
 * Helper class to access Google drive.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GoogleDrive {

    private static final Logger log = LoggerFactory.getLogger("GoogleDrive");

    private static final String APP_DATA_FOLDER = "appDataFolder";
    private static final String MIME_JSON = "text/json";

    private final Drive service;

    public GoogleDrive(@NonNull Context context, @NonNull Account account) {
        GoogleAccountCredential credential = usingOAuth2(context, ImmutableSet.of(DRIVE_APPDATA))
                .setSelectedAccount(account);

        service = new Drive.Builder(new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("smailer")
                .build();
    }

    @Nullable
    private InputStream open(String filename) throws IOException {
        String fileId = find(filename);
        if (fileId != null) {
            return service.files().get(fileId).executeMediaAsInputStream();
        }
        return null;
    }

    private void write(String filename, String json) throws IOException {
        String fileId = find(filename);
        if (fileId == null) {
            create(filename, json);
        } else {
            update(fileId, filename, json);
        }
    }

    private void create(String filename, String json) throws IOException {
        File metadata = new File()
                .setParents(ImmutableList.of(APP_DATA_FOLDER))
                .setMimeType(MIME_JSON)
                .setName(filename);
        ByteArrayContent content = ByteArrayContent.fromString(MIME_JSON, json);

        service.files()
                .create(metadata, content)
                .setFields("id")
                .execute();
    }

    private void update(String fileId, String filename, String json) throws IOException {
        File metadata = new File()
                .setName(filename);
        ByteArrayContent content = ByteArrayContent.fromString(MIME_JSON, json);

        service.files()
                .update(fileId, metadata, content)
                .execute();
    }

    @Nullable
    private String find(String filename) throws IOException {
        List<File> files = service.files().list()
                .setSpaces(APP_DATA_FOLDER)
                .setQ("name='" + filename + "'")
                .setFields("files(id)")
                .execute()
                .getFiles();

        if (!files.isEmpty()) {
            if (files.size() > 1) {
                log.error("Multiple files found");
            }
            return files.get(0).getId();
        }
        return null;
    }

    public void clear() throws IOException {
        for (File file : list()) {
            service.files()
                    .delete(file.getId())
                    .execute();
        }
    }

    public List<File> list() throws IOException {
        return service.files().list()
                .setSpaces(APP_DATA_FOLDER)
                .setFields("files(id, name)")
                .execute()
                .getFiles();
    }

    public void upload(@NonNull String filename, @NonNull Object object) throws IOException {
        Writer writer = new StringWriter();
        JsonGenerator generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer);
        generator.serialize(object);
        generator.flush();
        write(filename, writer.toString());
        generator.close();
    }

    @Nullable
    public <T> T download(@NonNull String filename, @NonNull Class<? extends T> objectClass)
            throws IOException {
        InputStream stream = open(filename);
        if (stream != null) {
            JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(stream);
            return parser.parseAndClose(objectClass);
        } else {
            return null;
        }
    }
}