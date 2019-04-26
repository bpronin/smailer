package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport;
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

    private final Context context;
    private final Drive service;

    public GoogleDrive(Context context, Account account) {
        this.context = context;
        GoogleAccountCredential credential = usingOAuth2(context, ImmutableSet.of(DRIVE_APPDATA))
                .setSelectedAccount(account);

        service = new Drive.Builder(
                newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("smailer")
                .build();
    }

    @Nullable
    public InputStream open(String filename) throws IOException {
        String fileId = find(filename);
        if (fileId != null) {
            return service.files().get(fileId).executeMediaAsInputStream();
        }
        return null;
    }

    public void write(String filename, String data) throws IOException {
        String fileId = find(filename);
        if (fileId == null) {
            create(filename, data);
        } else {
            update(fileId, filename, data);
        }
    }

    public String create(String filename, String data) throws IOException {
        File metadata = new File()
                .setParents(ImmutableList.of(APP_DATA_FOLDER))
                .setMimeType(MIME_JSON)
                .setName(filename);
        ByteArrayContent content = ByteArrayContent.fromString(MIME_JSON, data);

        return service.files()
                .create(metadata, content)
                .setFields("id")
                .execute()
                .getId();
    }

    public void update(String fileId, String filename, String data) throws IOException {
        File metadata = new File()
                .setName(filename);
        ByteArrayContent content = ByteArrayContent.fromString(MIME_JSON, data);

        service.files()
                .update(fileId, metadata, content)
                .execute();
    }

    @Nullable
    public String find(String filename) throws IOException {
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

//    public void saveConfiguration(OnCompleteListener<String> listener) {
//        Tasks.call(executor, new Callable<String>() {
//
//            @Override
//            public String call() throws Exception {
//                return doSave(DATABASE_NAME, new FileContent("application/octet-stream",
//                        context.getDatabasePath(DATABASE_NAME)));
//            }
//        }).addOnCompleteListener(listener);
//    }

//    public void loadConfiguration(OnCompleteListener<String> listener) {
//        Tasks.call(executor, new Callable<String>() {
//
//            @Override
//            public String call() throws Exception {
//                return doload(DATABASE_NAME, new FileContent("application/octet-stream",
//                        context.getDatabasePath(DATABASE_NAME)));
//            }
//        }).addOnCompleteListener(listener);
//    }

    //    private String doSave(String filename, FileContent content) throws java.io.IOException {
//        File meta = new File();
//        meta.setParents(singletonList(APP_DATA_FOLDER));
//        meta.setName(filename);
//
//        return service.files().create(meta, content)
//                .setFields("id")
//                .execute()
//                .getId();
//    }
//

}