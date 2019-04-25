package com.bopr.android.smailer.sync;

import android.content.Context;

import com.bopr.android.smailer.GoogleAuthorizationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.bopr.android.smailer.Database.DATABASE_NAME;
import static com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport;

/**
 * Helper class to access Google drive.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GoogleDriveSupport {

    private static final String APP_DATA_FOLDER = "appDataFolder";

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;
    private Drive service;

    public GoogleDriveSupport(Context context) {
        this.context = context;
    }

    public void init(String accountName) {
        GoogleAccountCredential credential = GoogleAuthorizationHelper.createCredential(context,
                accountName,
                DriveScopes.DRIVE_APPDATA);

        service = new Drive.Builder(
                newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("smailer")
                .build();
    }

    public void saveConfiguration(OnCompleteListener<String> listener) {
        Tasks.call(executor, new Callable<String>() {

            @Override
            public String call() throws Exception {
                return doSave(DATABASE_NAME, new FileContent("application/octet-stream",
                        context.getDatabasePath(DATABASE_NAME)));
            }
        }).addOnCompleteListener(listener);
    }

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

    private String doSave(String filename, FileContent content) throws java.io.IOException {
        File meta = new File();
        meta.setParents(Collections.singletonList(APP_DATA_FOLDER));
        meta.setName(filename);

        return service.files().create(meta, content)
                .setFields("id")
                .execute()
                .getId();
    }

    public void list(OnCompleteListener<List<File>> listener) {
        Tasks.call(executor, new Callable<List<File>>() {

            @Override
            public List<File> call() throws Exception {
                return service.files().list()
                        .setSpaces(APP_DATA_FOLDER)
                        .setQ(DATABASE_NAME)
                        .setFields("files(id)")
                        .execute()
                        .getFiles();
            }
        }).addOnCompleteListener(listener);
    }

}