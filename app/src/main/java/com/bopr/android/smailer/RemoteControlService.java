package com.bopr.android.smailer;


import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import androidx.annotation.NonNull;

import static com.bopr.android.smailer.Settings.KEY_PREF_FIREBASE_TOKEN;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.requireNonNull;

public class RemoteControlService extends FirebaseMessagingService {

    private static Logger log = LoggerFactory.getLogger("RemoteControlService");
    private Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();
        settings = new Settings(this);
    }

    @Override
    public void onNewToken(String token) {
        saveToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> data = message.getData();
        log.debug("Message received: " + data);

        String action = data.get("action");
        if ("blacklist".equals(action)) {
            String number = data.get("phone");
            addToBlacklist(number);
        }
    }

    private void addToBlacklist(String number) {
        PhoneEventFilter filter = settings.getFilter();
        filter.getPhoneBlacklist().add(number);
        settings.putFilter(filter);

        log.debug("Blacklisted phone number: " + number);

        String message = formatter(R.string.added_to_blacklist_remotely, this)
                .put("number", number)
                .format();
        new Notifications(this).showRemoteAction(message);
    }

    private static void saveToken(Context context, String token) {
        log.debug("Token received: " + token);
        new Settings(context).edit()
                .putString(KEY_PREF_FIREBASE_TOKEN, token)
                .apply();
    }

    private static void requestAccessToken(Context context) throws Exception {
//        FileInputStream serviceAccount = new FileInputStream("path/to/serviceAccountKey.json");

//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .setDatabaseUrl("https://smailer-24874.firebaseio.com")
//                .build();

//        FirebaseOptions options = new FirebaseOptions.Builder().;
//        FirebaseApp.initializeApp(context, options);
//        Bundle options = new Bundle();

//        AccountManager manager = AccountManager.get(context);
//        manager.getAuthToken(
//                myAccount_,                     // Account retrieved using getAccountsByType()
//                "Manage your tasks",            // Auth scope
//                options,                        // Authenticator-specific options
//                context,
//                new OnTokenAcquired(),          // Callback called when a token is successfully acquired
//                new Handler(new OnError()));    // Callback called if an error occurs

//        GoogleCredential googleCredential = GoogleCredential
//                .fromStream(new FileInputStream("service-account.json"))
//                .createScoped(Arrays.asList(SCOPES));
//        googleCredential.refreshToken();
//        return googleCredential.getAccessToken();
    }

    public static void enable(final Context context) {
        /* Request current app instance token                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 token */
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                new OnCompleteListener<InstanceIdResult>() {

                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            saveToken(context, requireNonNull(task.getResult()).getToken());
                        } else {
                            log.warn("getInstanceId failed", task.getException());
                        }
                    }
                });
    }
}
