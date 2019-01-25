package com.bopr.android.smailer.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.Locator;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.Notifications;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.SmsReceiver;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.ui.ContextAsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bopr.android.smailer.MailerService.createEventIntent;
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

        Preference[] preferences = {

                createSimplePreference("Emulate Sms", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onEmulateSms();
                    }
                }),

                createSimplePreference("Set default preferences", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onRestorePreferences();
                    }
                }),

                createSimplePreference("Send default mail", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSendDefaultMail();
                    }
                }),

                createSimplePreference("Send mail", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSendMail();
                    }
                }),

                createSimplePreference("Clear preferences", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onClearPreferences();
                    }
                }),

                createSimplePreference("Require Sms permission", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onRequireReceiveSmsPermission();
                    }
                }),

                createSimplePreference("Request Sms permission", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        requestSmsPermission();
                    }
                }),

                createSimplePreference("Get location", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onGetLocation();
                    }
                }),

                createSimplePreference("Get contact", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onGetContact();
                    }
                }),

                createSimplePreference("Save log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSaveLog();
                    }
                }),

                createSimplePreference("Send log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSendLog();
                    }
                }),

                createSimplePreference("Destroy database", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.destroy();
                    }
                }),

                createSimplePreference("Show password", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onShowPassword();
                    }
                }),

                createSimplePreference("Populate log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onPopulateLog();
                    }
                }),

                createSimplePreference("Clear log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onClearLog();
                    }
                }),

                createSimplePreference("Show notification", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onShowNotification();
                    }
                }),

                createSimplePreference("Show concurrent", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onShowConcurrent();
                    }
                }),

                createSimplePreference("Crash!", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        throw new RuntimeException("Test crash");
                    }
                }),

        };


        Arrays.sort(preferences, new Comparator<Preference>() {
            @Override
            public int compare(Preference o1, Preference o2) {

                return o1.getTitle().toString().compareTo(o2.toString());
            }
        });

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        for (Preference preference : preferences) {
            screen.addPreference(preference);
        }
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

    private Preference createSimplePreference(String title, Preference.OnPreferenceClickListener listener) {
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
        if (AndroidUtil.isPermissionsDenied(getActivity(), RECEIVE_SMS)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{RECEIVE_SMS},
                    PERMISSIONS_REQUEST_RECEIVE_SMS);
        }
    }

    private void onRestorePreferences() {
        Properties properties = getDebugProperties();

        getSharedPreferences()
                .edit()
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
        new GetLocationTask(getActivity(), locator).execute();
    }

    private void onClearPreferences() {
        getSharedPreferences().edit().clear().apply();
        refreshPreferences();
    }

    private void onSendDefaultMail() {
        new SendDefaultMailTask(getActivity(), getDebugProperties()).execute();
    }

    private void onSendMail() {
/*
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                PhoneEvent message = new PhoneEvent();
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
        PhoneEvent event = new PhoneEvent();
        event.setPhone("+79052345678");
        event.setStartTime(start);
        event.setEndTime(start + 10000);

        getActivity().startService(createEventIntent(getActivity(), event));
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
        database.putEvent(new PhoneEvent("+79052345671", true, time, null, false, "Debug message", null, null, PhoneEvent.State.PENDING));
        database.putEvent(new PhoneEvent("+79052345672", false, time += 1000, null, false, "Debug message", null, null, PhoneEvent.State.PROCESSED));
        database.putEvent(new PhoneEvent("+79052345673", true, time += 1000, time + 10000, false, null, null, null, PhoneEvent.State.IGNORED));
        database.putEvent(new PhoneEvent("+79052345674", false, time += 1000, time + 10000, false, null, null, null, PhoneEvent.State.PENDING));
        database.putEvent(new PhoneEvent("+79052345675", true, time += 1000, time + 10000, true, null, null, null, PhoneEvent.State.PENDING));
        database.putEvent(new PhoneEvent("+79052345671", true, time += 1000, null, false, "Debug message", null, "Test exception +79052345671", PhoneEvent.State.PENDING));
        database.putEvent(new PhoneEvent("+79052345672", false, time += 1000, null, false, "Debug message", null, "Test exception +79052345672", PhoneEvent.State.PENDING));
        database.putEvent(new PhoneEvent("+79052345673", true, time += 1000, time + 10000, false, null, null, "Test exception +79052345673", PhoneEvent.State.PENDING));
        database.putEvent(new PhoneEvent("+79052345674", false, time += 1000, time + 10000, false, null, null, "Test exception +79052345674", PhoneEvent.State.PENDING));
        database.putEvent(new PhoneEvent("+79052345675", true, time += 1000, time + 10000, true, null, null, "Test exception +79052345675", PhoneEvent.State.PENDING));
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
        new SendLogTask(getActivity(), getDebugProperties()).execute();
    }

    private void onShowConcurrent() {
        StringBuilder b = new StringBuilder();

        Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
        List<ResolveInfo> activities = getActivity().getPackageManager().queryBroadcastReceivers(intent, 0);
        for (ResolveInfo resolveInfo : activities) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                b.append(activityInfo.packageName)
                        .append(" : ")
                        .append(resolveInfo.priority)
                        .append("\n");
                log.debug("Concurrent package:" + activityInfo.packageName + " priority: " + resolveInfo.priority);
            }
        }
        AndroidUtil.dialogBuilder(getActivity())
                .setMessage(b.toString())
                .show();
    }

    private static class GetLocationTask extends ContextAsyncTask<Void, Void, GeoCoordinates> {

        private final Locator locator;

        private GetLocationTask(Activity activity, Locator locator) {
            super(activity);
            this.locator = locator;
        }

        @Override
        protected GeoCoordinates doInBackground(Void... params) {
            return locator.getLocation(3000);
        }

        @Override
        protected void onPostExecute(GeoCoordinates coordinates) {
            Toast.makeText(getContext(),
                    coordinates != null ? formatLocation(coordinates)
                            : "No location received",
                    Toast.LENGTH_LONG).show();
        }
    }

    private static class SendDefaultMailTask extends ContextAsyncTask<Void, Void, Void> {

        private Properties properties;

        private SendDefaultMailTask(Activity activity, Properties properties) {
            super(activity);
            this.properties = properties;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String user = properties.getProperty("default_sender");

            MailTransport transport = new MailTransport();
            transport.init(user,
                    properties.getProperty("default_password"),
                    "smtp.gmail.com",
                    "465");

            try {
                transport.send(
                        "test subject",
                        "test message from " + getDeviceName(getContext()),
                        user,
                        properties.getProperty("default_recipient")
                );
            } catch (Exception x) {
                log.error("FAILED: ", x);
            }
            return null;
        }
    }

    private static class SendLogTask extends LongAsyncTask<Void, Void, Void> {

        private Properties properties;

        private SendLogTask(Activity activity, Properties properties) {
            super(activity);
            this.properties = properties;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String user = properties.getProperty("default_sender");

            MailTransport transport = new MailTransport();
            transport.init(user,
                    properties.getProperty("default_password"),
                    "smtp.gmail.com",
                    "465");

            File logDir = new File(getContext().getFilesDir(), "log");
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
    }

    private abstract class DefaultClickListener implements Preference.OnPreferenceClickListener {

        protected abstract void onClick(Preference preference);

        @Override
        public boolean onPreferenceClick(Preference preference) {
            onClick(preference);
            //    getActivity().finish();
            return true;
        }
    }
}
