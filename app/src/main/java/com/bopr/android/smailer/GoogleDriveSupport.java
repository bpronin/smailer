package com.bopr.android.smailer;

import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport;

/**
 * Application database.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GoogleDriveSupport {

    private static final String TAG = "GoogleDriveSupport";

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;
    private Drive service;

    public GoogleDriveSupport(Context context) {
        this.context = context;
    }

    public void init(String accountName) {
        GoogleAccountCredential credential = AuthorizationHelper.createCredential(context,
                accountName,
                DriveScopes.DRIVE_APPDATA);

        service = new Drive.Builder(
                newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("smailer")
                .build();
    }

    public void save(OnCompleteListener<String> listener) {
        Tasks.call(executor, new Callable<String>() {

            @Override
            public String call() throws Exception {
                File metadata = new File()
//                        .setParents(Collections.singletonList("root"))
                        .setMimeType("text/plain")
                        .setName("text.txt");

                File file = service.files().create(metadata).execute();
                if (file == null) {
                    throw new IOException("Null result when requesting file creation.");
                }

                return file.getId();
            }
        }).addOnCompleteListener(listener);
    }

}