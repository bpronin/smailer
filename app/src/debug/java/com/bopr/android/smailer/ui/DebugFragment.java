package com.bopr.android.smailer.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import com.bopr.android.smailer.CallProcessorService;
import com.bopr.android.smailer.CallReceiver;
import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.GeoLocator;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.Notifications;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.ui.ActivityAsyncTask;
import com.bopr.android.smailer.util.ui.LongAsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import static android.Manifest.permission.BROADCAST_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_LOCALE;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_BLACKLIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_TEXT_BLACKLIST;
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
import static com.bopr.android.smailer.util.Util.asSet;
import static com.bopr.android.smailer.util.Util.commaSeparated;
import static com.bopr.android.smailer.util.Util.formatLocation;
import static com.bopr.android.smailer.util.Util.quoteRegex;

/**
 * For debug purposes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DebugFragment extends BasePreferenceFragment {

    private static Logger log = LoggerFactory.getLogger("DebugFragment");

    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 100;
    private static final int PERMISSIONS_REQUEST_BROADCAST_SMS = 101;

    private Context context;
    private GeoLocator locator;
    private Cryptor cryptor;
    private Database database;
    private PreferenceScreen screen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        /* do not use fragment's context. see: https://developer.android.com/guide/topics/ui/settings/programmatic-hierarchy*/
        context = getPreferenceManager().getContext();

        database = new Database(context);
        locator = new GeoLocator(context, database);
        cryptor = new Cryptor(context);

        screen = getPreferenceManager().createPreferenceScreen(context);

        addCategory("Settings",

                createPreference("Set debug settings", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSetDebugPreferences();
                    }
                }),

                createPreference("Clear settings", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onClearPreferences();
                    }
                })
        );

        addCategory("Call processing",

                createPreference("Start process single event", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onStartProcessSingleEvent();
                    }
                }),

                createPreference("Start process pending events", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onStartProcessPendingEvents();
                    }
                }),

                createPreference("Send debug mail", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSendDebugMail();
                    }
                })
        );

        addCategory("Database",

                createPreference("Add an item to calls log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onAddHistoryItem();
                    }
                }),

                createPreference("Add 10 items to calls log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onPopulateHistory();
                    }
                }),

                createPreference("Mark all as unread", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.getEvents().iterate(new Consumer<PhoneEvent>() {

                            @Override
                            public void accept(PhoneEvent event) {
                                event.setRead(false);
                                database.putEvent(event);
                            }
                        });
                        database.notifyChanged();
                        showDone();
                    }
                }),

                createPreference("Mark all as read", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.getEvents().iterate(new Consumer<PhoneEvent>() {

                            @Override
                            public void accept(PhoneEvent event) {
                                event.setRead(true);
                                database.putEvent(event);
                            }
                        });
                        database.notifyChanged();
                        showDone();
                    }
                }),

                createPreference("Clear calls log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.clearEvents();
                        database.notifyChanged();
                        showDone();
                    }
                }),

                createPreference("Destroy database", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.destroy();
                        showDone();
                    }
                })

        );

        addCategory("Permissions",

                createPreference("Require Sms permission", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onRequireReceiveSmsPermission();
                    }
                }),

                createPreference("Request Sms permission", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onRequestSmsPermission();
                    }
                })
        );

        addCategory("Logging",

                createPreference("Send logs to developer", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        new SendLogTask(getActivity(), loadDebugProperties()).execute();
                    }
                }),

                createPreference("Clear logs", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onClearLogs();
                    }
                }),

                createPreference("Save logcat log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSaveLogcatLog();
                    }
                })
        );

        addCategory("Notifications",

                createPreference("Mail error. Show connection", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        new Notifications(context).showMailError("Test notification text", 100L, Notifications.ACTION_SHOW_CONNECTION);
                    }
                }),

                createPreference("Mail error. Show log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        new Notifications(context).showMailError("Test notification text", 100L, Notifications.ACTION_SHOW_LOG);
                    }
                }),

                createPreference("Mail success", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        new Notifications(context).showMailSuccess(100);
                    }
                })

        );

        addCategory("Other",

                createPreference("Emulate Sms", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onEmulateSms();
                    }
                }),

                createPreference("Get location", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        new GetLocationTask(getActivity(), locator).execute();
                    }
                }),

                createPreference("Get contact", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onGetContact();
                    }
                }),

                createPreference("Show password", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onShowPassword();
                    }
                }),

                createPreference("Show concurrent applications", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onShowConcurrent();
                    }
                }),

                createPreference("Crash!", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        throw new RuntimeException("Test crash");
                    }
                })
        );

        setPreferenceScreen(screen);
    }

    @NonNull
    private Properties loadDebugProperties() {
        Properties properties = new Properties();
        try {
            InputStream stream = context.getAssets().open("debug.properties");
            properties.load(stream);
        } catch (IOException x) {
            log.error("Cannot read debug properties", x);
        }
        return properties;
    }

    private Preference createPreference(String title, Preference.OnPreferenceClickListener listener) {
        Preference preference = new Preference(context);
        preference.setTitle(title);
        preference.setIcon(R.drawable.ic_bullet);
        preference.setOnPreferenceClickListener(listener);
        return preference;
    }

/*
    private void addPreference(String title, Preference.OnPreferenceClickListener listener) {
        Preference preference = createPreference(title, listener);
        screen.addPreference(preference);
    }
*/

    private void addCategory(String title, Preference... preferences) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);

        category.setTitle(title);
        for (Preference preference : preferences) {
            category.addPreference(preference);
        }
    }

    private void showDone() {
        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
    }

    private void onSetDebugPreferences() {
        Properties properties = loadDebugProperties();

        settings.edit()
                .putString(KEY_PREF_SENDER_ACCOUNT, properties.getProperty("default_sender"))
                .putString(KEY_PREF_SENDER_PASSWORD, cryptor.encrypt(properties.getProperty("default_password")))
                .putString(KEY_PREF_RECIPIENTS_ADDRESS, properties.getProperty("default_recipient"))
                .putString(KEY_PREF_EMAIL_HOST, DEFAULT_HOST)
                .putString(KEY_PREF_EMAIL_PORT, DEFAULT_PORT)
                .putStringSet(KEY_PREF_EMAIL_TRIGGERS, asSet(VAL_PREF_TRIGGER_IN_SMS,
                        VAL_PREF_TRIGGER_IN_CALLS,
                        VAL_PREF_TRIGGER_MISSED_CALLS,
                        VAL_PREF_TRIGGER_OUT_CALLS,
                        VAL_PREF_TRIGGER_OUT_SMS))
                .putStringSet(KEY_PREF_EMAIL_CONTENT, asSet(VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME))
                .putString(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE)
                .putBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, true)
                .putBoolean(KEY_PREF_RESEND_UNSENT, true)
                .putString(KEY_PREF_FILTER_BLACKLIST, commaSeparated(asSet("+123456789", "+9876543*")))
                .putString(KEY_PREF_FILTER_TEXT_BLACKLIST, commaSeparated(asSet("Bad text", quoteRegex("Expression"))))
                .apply();

        refreshPreferences();
        showDone();
    }

    private void onGetContact() {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        AndroidUtil.dialogBuilder(context)
                .setTitle("Phone number")
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phone = input.getText().toString();
                        String contact = Contacts.getContactName(context, phone);
                        String text = contact != null ? (phone + ": " + contact) : "Contact not found";

                        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
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

    private void onClearPreferences() {
        settings.edit().clear().apply();
        refreshPreferences();
        showDone();
    }

    private void onSendDebugMail() {
        new SendDefaultMailTask(getActivity(), loadDebugProperties()).execute();
        showDone();
    }

    private void onStartProcessSingleEvent() {
        long start = System.currentTimeMillis();

        PhoneEvent event = new PhoneEvent();
        event.setPhone("+12345678901");
        event.setText("SMS TEXT");
        event.setIncoming(true);
        event.setStartTime(start);
        event.setEndTime(start + 10000);

        CallProcessorService.start(context, event);
        showDone();
    }

    private void onStartProcessPendingEvents() {
        CallProcessorService.start(context);
        showDone();
    }

    private void onRequireReceiveSmsPermission() {
        if (ContextCompat.checkSelfPermission(context, RECEIVE_SMS) == PERMISSION_GRANTED) {
            context.enforceCallingOrSelfPermission(Manifest.permission.RECEIVE_SMS, "Testing SMS permission");
        } else {
            Toast.makeText(context, "SMS PERMISSION DENIED", Toast.LENGTH_LONG).show();
        }
    }

    private void onRequestSmsPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{RECEIVE_SMS},
                PERMISSIONS_REQUEST_RECEIVE_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                showMessage(context, "Permission granted");
            } else {
                showMessage(context, "Permission denied");
            }
        } else if (requestCode == PERMISSIONS_REQUEST_BROADCAST_SMS) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                onEmulateSms();
            } else {
                showMessage(context, "Permission denied");
            }
        }
    }

    private void onSaveLogcatLog() {
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

    private void onClearLogs() {
        File[] logs = new File(requireContext().getFilesDir(), "log").listFiles();
        for (File file : logs) {
            if (!file.delete()) {
                log.warn("Cannot delete file");
            }
        }
        showDone();
    }

    private void onShowPassword() {
        String text = cryptor.decrypt(settings.getString(KEY_PREF_SENDER_PASSWORD, null));
        showMessage(context, text);
    }

    private void onAddHistoryItem() {
        database.putEvent(new PhoneEvent("+79052345670", true, System.currentTimeMillis(), null, false, "Debug message", null, null, PhoneEvent.State.PENDING));
        database.notifyChanged();
        showDone();
    }

    private void onPopulateHistory() {
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
        database.notifyChanged();

        showDone();
    }

    private void onEmulateSms() {
        if (!AndroidUtil.isPermissionsDenied(context, BROADCAST_SMS)) {

            Intent intent = new Intent(CallReceiver.SMS_RECEIVED);
            intent.putExtra("pdus", new Object[]{Base64.decode("ACADgSHzAABhQEASFTQhBcgym/0G", Base64.NO_WRAP)});
            intent.putExtra("format", "3gpp");

            context.sendBroadcast(intent);
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{BROADCAST_SMS},
                    PERMISSIONS_REQUEST_BROADCAST_SMS);
        }
    }

    private void onShowConcurrent() {
        StringBuilder b = new StringBuilder();

        Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
        List<ResolveInfo> activities = context.getPackageManager().queryBroadcastReceivers(intent, 0);
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
        showMessage(context, b.toString());
    }

    private static void showMessage(Context context, String message) {
        AndroidUtil.dialogBuilder(context)
                .setMessage(message)
//                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                })
                .show();
    }

    private static class GetLocationTask extends ActivityAsyncTask<Void, Void, GeoCoordinates> {

        private final GeoLocator locator;

        private GetLocationTask(Activity activity, GeoLocator locator) {
            super(activity);
            this.locator = locator;
        }

        @Override
        protected GeoCoordinates doInBackground(Void... params) {
            return locator.getLocation(3000);
        }

        @Override
        protected void onPostExecute(GeoCoordinates coordinates) {
            showMessage(getActivity(),
                    coordinates != null ? formatLocation(coordinates) : "No location received");
        }
    }

    private static class SendDefaultMailTask extends ActivityAsyncTask<Void, Void, Void> {

        private Properties properties;

        private SendDefaultMailTask(Activity activity, Properties properties) {
            super(activity);
            this.properties = properties;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String user = properties.getProperty("default_sender");

            MailTransport transport = new MailTransport();
            transport.startSession(user, properties.getProperty("default_password"),
                    "smtp.gmail.com", "465");

            try {
                transport.send(
                        "test subject",
                        "test message from " + AndroidUtil.getDeviceName(),
                        user,
                        properties.getProperty("default_recipient")
                );
            } catch (Exception x) {
                log.error("FAILED: ", x);
            }
            return null;
        }
    }

    private static class SendLogTask extends LongAsyncTask<Void, Void, String> {

        private Properties properties;

        private SendLogTask(Activity activity, Properties properties) {
            super(activity);
            this.properties = properties;
        }

        @Override
        protected String doInBackground(Void... params) {
            List<File> attachment = new LinkedList<>();
            attachment.add(getActivity().getDatabasePath(Settings.DB_NAME));
            attachment.addAll(Arrays.asList(new File(getActivity().getFilesDir(), "log").listFiles()));

            try {
                String user = properties.getProperty("default_sender");
                MailTransport transport = new MailTransport();
                transport.startSession(user, properties.getProperty("default_password"),
                        "smtp.gmail.com", "465");
                transport.send(
                        "SMailer log",
                        "See attachment",
                        attachment,
                        user,
                        properties.getProperty("developer_email")
                );
            } catch (Exception x) {
                log.error("Send mail failed", x);
                return "Send mail failed";
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            super.onPostExecute(error);
            if (error != null) {
                showMessage(getActivity(), error);
            } else {
                Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private abstract class DefaultClickListener implements Preference.OnPreferenceClickListener {

        protected abstract void onClick(Preference preference);

        @Override
        public boolean onPreferenceClick(Preference preference) {
            onClick(preference);
            //getActivity().finish();
            return true;
        }
    }

}
