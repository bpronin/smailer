package com.bopr.android.smailer.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.Locator;
import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.Notifications;
import com.bopr.android.smailer.PermissionsChecker;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.SmsReceiver;
import com.bopr.android.smailer.util.AndroidUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.Preference.OnPreferenceClickListener;
import static com.bopr.android.smailer.MailerService.createIncomingCallIntent;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_LOCALE;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static com.bopr.android.smailer.Settings.getDeviceName;
import static com.bopr.android.smailer.util.Util.asSet;
import static com.bopr.android.smailer.util.Util.formatLocation;

/**
 * For debug purposes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DebugFragment extends BasePreferenceFragment {

    private static Logger log = LoggerFactory.getLogger("DebugFragment");
    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 100;

    private Locator locator;
    private Cryptor cryptor;
    private Database database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = new Database(getActivity());
        locator = new Locator(getActivity(), database);
        cryptor = new Cryptor(getActivity());

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());

        screen.addPreference(createSimplePreference("Emulate Sms", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onEmulateSms();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Set default preferences", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onRestorePreferences();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Send default mail", new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onSendDefaultMail();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Send mail", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onSendMail();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Clear preferences", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onClearPreferences();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Require Sms permission", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onRequireReceiveSmsPermission();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Request Sms permission", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestSmsPermission();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Get location", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onGetLocation();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Get contact", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onGetContact();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Save log", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onSaveLog();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Send log", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onSendLog();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Destroy database", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                database.destroy();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Show password", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onShowPassword();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Populate log", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onPopulateLog();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Clear log", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onClearLog();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Show notification", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onShowNotification();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Show concurrent", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onShowConcurrent();
                return true;
            }
        }));

        screen.addPreference(createSimplePreference("Crash!", new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                throw new RuntimeException("Test crash");
            }
        }));

        setPreferenceScreen(screen);
    }

    @Override
    public void onStart() {
        super.onStart();
        locator.start();
    }

    @Override
    public void onStop() {
        locator.stop();
        super.onStop();
    }

    private Preference createSimplePreference(String title, OnPreferenceClickListener listener) {
        Preference preference = new Preference(getActivity());
        preference.setTitle(title);
        preference.setOnPreferenceClickListener(listener);
        return preference;
    }

    @NonNull
    private Properties getDebugProperties() {
        Properties properties = new Properties();
        try {
            InputStream stream = getActivity().getAssets().open("debug.properties");
            properties.load(stream);
        } catch (IOException x) {
            log.error("Cannot read debug properties", x);
        }
        return properties;
    }

    private boolean smsPermissionDenied() {
        return ContextCompat.checkSelfPermission(getActivity(), RECEIVE_SMS) != PERMISSION_GRANTED;
    }

    public void requestSmsPermission() {
        if (PermissionsChecker.isPermissionsDenied(getActivity(), RECEIVE_SMS)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{RECEIVE_SMS},
                    PERMISSIONS_REQUEST_RECEIVE_SMS);
        }
    }

    private void onRestorePreferences() {
        Properties properties = getDebugProperties();

        getSharedPreferences()
                .edit()
                .putBoolean(KEY_PREF_SERVICE_ENABLED, true)
                .putString(KEY_PREF_SENDER_ACCOUNT, properties.getProperty("default_sender"))
                .putString(KEY_PREF_SENDER_PASSWORD, cryptor.encrypt(properties.getProperty("default_password")))
                .putString(KEY_PREF_RECIPIENTS_ADDRESS, properties.getProperty("default_recipient"))
                .putString(KEY_PREF_EMAIL_HOST, DEFAULT_HOST)
                .putString(KEY_PREF_EMAIL_PORT, DEFAULT_PORT)
                .putStringSet(KEY_PREF_EMAIL_TRIGGERS, asSet(VAL_PREF_TRIGGER_IN_SMS,
                        VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_MISSED_CALLS,
                        VAL_PREF_TRIGGER_OUT_CALLS, VAL_PREF_TRIGGER_OUT_SMS))
                .putStringSet(KEY_PREF_EMAIL_CONTENT, asSet(VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME))
                .putString(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE)
                .putBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, true)
                .putBoolean(KEY_PREF_RESEND_UNSENT, true)
                .apply();
        refreshPreferences();
    }

    private void onGetContact() {
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        AndroidUtil.dialogBuilder(getActivity())
                .setTitle("Phone number")
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phone = input.getText().toString();
                        String contact = Contacts.getContactName(getActivity(), phone);
                        String text = contact != null ? (phone + ": " + contact) : "Contact not found";

                        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @SuppressWarnings("ResourceType")
    private void onGetLocation() {
        new AsyncTask<Void, Void, GeoCoordinates>() {

            @Override
            protected GeoCoordinates doInBackground(Void... params) {
                return locator.getLocation(3000);
            }

            @Override
            protected void onPostExecute(GeoCoordinates coordinates) {
                Toast.makeText(getActivity(),
                        coordinates != null ? formatLocation(coordinates)
                                : "No location received",
                        Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void onClearPreferences() {
        getSharedPreferences().edit().clear().apply();
        refreshPreferences();
    }

    private void onSendDefaultMail() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Properties properties = getDebugProperties();
                String user = properties.getProperty("default_sender");

                MailTransport transport = new MailTransport();
                transport.init(user,
                        properties.getProperty("default_password"),
                        "smtp.gmail.com",
                        "465");

                try {
                    transport.send(
                            "test subject",
                            "test message from " + getDeviceName(getActivity()),
                            user,
                            properties.getProperty("default_recipient")
                    );
                } catch (Exception x) {
                    log.error("FAILED: ", x);
                }
                return null;
            }
        }.execute();
    }

    private void onSendMail() {
/*
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                MailMessage message = new MailMessage();
                message.setPhone("+79052345678");
                message.setIncoming(true);
                message.setSms(true);
                message.setText("Debug message.");
                message.setStartTime(System.currentTimeMillis());
                message.setLocation(new GeoCoordinates(30.0, 60.0));

                new Mailer(getActivity(), database).send(message);

                return null;
            }
        }.execute();
*/
        long start = System.currentTimeMillis();
        getActivity().startService(createIncomingCallIntent(getActivity(), "+79052345678", start, start + 10000));
    }

    private void onRequireReceiveSmsPermission() {
        if (!smsPermissionDenied()) {
            getActivity().enforceCallingOrSelfPermission(
                    Manifest.permission.RECEIVE_SMS, "Testing SMS permission");
        } else {
            Toast.makeText(getActivity(), "SMS PERMISSION DENIED", Toast.LENGTH_LONG).show();
        }
    }

    public void onSaveLog() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                log.append(line).append("\n");
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"boprsoft.dev@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "SMailer log");
            intent.putExtra(Intent.EXTRA_TEXT, log.toString());

            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (IOException x) {
            log.error("Save log failed", x);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults
    ) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onShowPassword() {
        String text = cryptor.decrypt(getSharedPreferences().getString(KEY_PREF_SENDER_PASSWORD, null));
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private void onPopulateLog() {
        long time = System.currentTimeMillis();
        database.updateMessage(new MailMessage("+79052345671", true, time, null, false, true, "Debug message", null, true, null));
        database.updateMessage(new MailMessage("+79052345672", false, time += 1000, null, false, true, "Debug message", null, true, null));
        database.updateMessage(new MailMessage("+79052345673", true, time += 1000, time + 10000, false, false, null, null, true, null));
        database.updateMessage(new MailMessage("+79052345674", false, time += 1000, time + 10000, false, false, null, null, true, null));
        database.updateMessage(new MailMessage("+79052345675", true, time += 1000, time + 10000, true, false, null, null, true, null));


        database.updateMessage(new MailMessage("+79052345671", true, time += 1000, null, false, true, "Debug message", null, false, "Test exception +79052345671"));
        database.updateMessage(new MailMessage("+79052345672", false, time += 1000, null, false, true, "Debug message", null, false, "Test exception +79052345672"));
        database.updateMessage(new MailMessage("+79052345673", true, time += 1000, time + 10000, false, false, null, null, false, "Test exception +79052345673"));
        database.updateMessage(new MailMessage("+79052345674", false, time += 1000, time + 10000, false, false, null, null, false, "Test exception +79052345674"));
        database.updateMessage(new MailMessage("+79052345675", true, time += 1000, time + 10000, true, false, null, null, false, "Test exception +79052345675"));
    }

    private void onClearLog() {
        database.destroy();
    }

    private void onShowNotification() {
        new Notifications(getActivity()).showMailError("Test notification text", 100, Notifications.ACTION_SHOW_CONNECTION);
    }

    private void onEmulateSms() {
        Intent intent = new Intent(SmsReceiver.SMS_RECEIVED_ACTION);
        intent.putExtra("pdus", new Object[]{Base64.decode("ACADgSHzAABhQEASFTQhBcgym/0G", Base64.NO_WRAP)});
        intent.putExtra("format", "3gpp");

        getActivity().sendBroadcast(intent);
    }

    private void onSendLog() {
        new AsyncTask<Void, Void, Void>() {

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(getActivity(), getString(R.string.app_name), "Sending mail");
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Properties properties = getDebugProperties();
                String user = properties.getProperty("default_sender");

                MailTransport transport = new MailTransport();
                transport.init(user,
                        properties.getProperty("default_password"),
                        "smtp.gmail.com",
                        "465");

                File logDir = new File(getActivity().getFilesDir(), "log");
                if (logDir.exists()) {
                    for (String fileName : logDir.list()) {
                        try {
                            transport.send(
                                    "SMailer log",
                                    "See attachment",
                                    new File(fileName).toURI().toURL(),
                                    user,
                                    properties.getProperty("developer_email")
                            );
                        } catch (Exception x) {
                            log.error("Send mail failed: ", x);
                        }
                    }
                }
                return null;
            }
        }.execute();
    }

    private void onShowConcurrent() {
        String s = "";

        Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
        List<ResolveInfo> activities = getActivity().getPackageManager().queryBroadcastReceivers(intent, 0);
        for (ResolveInfo resolveInfo : activities) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                s += activityInfo.packageName + " : " + resolveInfo.priority + "\n";
                log.debug("Concurrent package:" + activityInfo.packageName + " priority: " + resolveInfo.priority);
            }
        }
        AndroidUtil.dialogBuilder(getActivity())
                .setMessage(s)
                .show();
    }

}
