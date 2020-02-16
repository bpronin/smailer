package com.bopr.android.smailer.ui;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.GeoLocator;
import com.bopr.android.smailer.GoogleAuthorizationHelper;
import com.bopr.android.smailer.GoogleDrive;
import com.bopr.android.smailer.GoogleMail;
import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.Notifications;
import com.bopr.android.smailer.PendingCallProcessorService;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.remote.RemoteControlService;
import com.bopr.android.smailer.sync.SyncAdapter;
import com.bopr.android.smailer.sync.SyncManager;
import com.bopr.android.smailer.util.ContentUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.app.PendingIntent.getBroadcast;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE;
import static android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE;
import static android.telephony.SmsManager.RESULT_ERROR_NULL_PDU;
import static android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF;
import static com.bopr.android.smailer.CallProcessorService.startCallProcessingService;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ENABLED;
import static com.bopr.android.smailer.Settings.PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.VAL_PREF_DEFAULT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_HEADER;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static com.bopr.android.smailer.ui.BatteryOptimizationHelper.isIgnoreBatteryOptimizationRequired;
import static com.bopr.android.smailer.ui.BatteryOptimizationHelper.requireIgnoreBatteryOptimization;
import static com.bopr.android.smailer.util.AndroidUtil.deviceName;
import static com.bopr.android.smailer.util.AndroidUtil.primaryAccount;
import static com.bopr.android.smailer.util.TextUtil.commaJoin;
import static com.bopr.android.smailer.util.TextUtil.formatCoordinates;
import static com.bopr.android.smailer.util.TextUtil.quoteRegex;
import static com.bopr.android.smailer.util.UiUtil.alertDialogView;
import static com.bopr.android.smailer.util.UiUtil.showToast;
import static com.bopr.android.smailer.util.Util.asSet;
import static com.bopr.android.smailer.util.Util.requireNonNull;
import static com.google.api.services.drive.DriveScopes.DRIVE_APPDATA;
import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;
import static com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * For debug purposes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DebugFragment extends BasePreferenceFragment {

    private static Logger log = LoggerFactory.getLogger("DebugFragment");

    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 100;

    private Context context;
    private GeoLocator locator;
    private Database database;
    private GoogleAuthorizationHelper authorizator;
    private Notifications notifications;
    private BroadcastReceiver sentStatusReceiver;
    private BroadcastReceiver deliveredStatusReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        database = new Database(context);
        locator = new GeoLocator(context, database);
        authorizator = new GoogleAuthorizationHelper(this, PREF_SENDER_ACCOUNT, MAIL_GOOGLE_COM,
                DRIVE_APPDATA);
        notifications = new Notifications(requireContext());

        sentStatusReceiver = new SentStatusReceiver();
        deliveredStatusReceiver = new DeliveryStatusReceiver();
        context.registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        context.registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }

    @Override
    public void onDestroy() {
        context.unregisterReceiver(sentStatusReceiver);
        context.unregisterReceiver(deliveredStatusReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        /* do not use fragment's context. see: https://developer.android.com/guide/topics/ui/settings/programmatic-hierarchy*/
        context = getPreferenceManager().getContext();

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        addCategory(screen, "Call processing",

                createPreference("Process single event", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onProcessSingleEvent();
                    }
                }),

                createPreference("Process pending events", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onStartProcessPendingEvents();
                    }
                }),

                createPreference("Process service mail", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onProcessServiceMail();
                    }
                }),

                createPreference("Send debug mail", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSendDebugMail();
                    }
                }),

                createPreference("Send SMS", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSendSms();
                    }
                })

        );

        addCategory(screen, "Google drive",

                createPreference("Download from drive", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onGoogleDriveDownload();
                    }
                }),

                createPreference("Upload to drive", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onGoogleDriveUpload();
                    }
                }),

                createPreference("Clear remote data", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onGoogleDriveClear();
                    }
                }),

                createPreference("Sync data", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onGoogleDriveSync();
                    }
                })

        );

        addCategory(screen, "Settings",

                createPreference("Populate settings", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onSetDebugPreferences();
                    }
                }),

                createPreference("Load default settings", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onLoadDefaultPreferences();
                    }
                }),

                createPreference("Clear settings", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onClearPreferences();
                    }
                }),

                createPreference("Require battery optimisation disabled", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        if (isIgnoreBatteryOptimizationRequired(context)) {
                            showToast(context, "Battery optimization already ignored");
                        } else {
                            requireIgnoreBatteryOptimization(context);
                        }
                    }
                })
        );

        addCategory(screen, "Database",

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
                        database.markAllAsRead(false);
                        database.notifyChanged();
                        showToast(context, "Done");
                    }
                }),

                createPreference("Mark all as read", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.markAllAsRead(true);
                        database.notifyChanged();
                        showToast(context, "Done");
                    }
                }),

                createPreference("Clear calls log", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.clearEvents();
                        database.notifyChanged();
                        showToast(context, "Done");
                    }
                }),

                createPreference("Destroy database", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        database.destroy();
                        showToast(context, "Done");
                    }
                })

        );

        addCategory(screen, "Permissions",

                createPreference("Request gmail api permission", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        onRequestGooglePermission();
                    }
                }),

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

        addCategory(screen, "Logging",

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
                })
        );

        addCategory(screen, "Notifications",

                createPreference("Show error. Open connection option", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        notifications.showMailError(R.string.no_internet_connection,
                                Notifications.ACTION_SHOW_CONNECTION_OPTIONS);
                    }
                }),

                createPreference("Show error. Open application", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        notifications.showMailError(R.string.no_recipients_specified,
                                Notifications.ACTION_SHOW_RULES);
                    }
                }),

                createPreference("Hide last error", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        notifications.hideAllErrors();
                    }
                }),

                createPreference("Show mail success", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        notifications.showMessage(R.string.email_send, Notifications.ACTION_SHOW_MAIN);
                    }
                }),

                createPreference("Show remote action", new DefaultClickListener() {

                    @Override
                    protected void onClick(Preference preference) {
                        notifications.showRemoteAction(R.string.text_remotely_added_to_blacklist,
                                "spam text");
                    }
                })

        );

        addCategory(screen, "Other",

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authorizator.onActivityResult(requestCode, resultCode, data);
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
        }
    }

    @NonNull
    private Properties loadDebugProperties() {
        Properties properties = new Properties();
        try {
            InputStream stream = context.getAssets().open("debug.properties");
            properties.load(stream);
            stream.close();
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

    private void addCategory(PreferenceScreen screen, String title, Preference... preferences) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);

        category.setTitle(title);
        for (Preference preference : preferences) {
            category.addPreference(preference);
        }
    }

    private void onSetDebugPreferences() {
        Properties properties = loadDebugProperties();

        settings.edit()
                .putString(PREF_SENDER_ACCOUNT, primaryAccount(context).name)
                .putString(PREF_REMOTE_CONTROL_ACCOUNT, properties.getProperty("remote_control_account"))
                .putString(PREF_RECIPIENTS_ADDRESS, properties.getProperty("default_recipient"))
                .putStringSet(PREF_EMAIL_TRIGGERS, asSet(
                        VAL_PREF_TRIGGER_IN_SMS,
                        VAL_PREF_TRIGGER_IN_CALLS,
                        VAL_PREF_TRIGGER_MISSED_CALLS,
                        VAL_PREF_TRIGGER_OUT_CALLS,
                        VAL_PREF_TRIGGER_OUT_SMS))
                .putStringSet(PREF_EMAIL_CONTENT, asSet(
                        VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
                        VAL_PREF_EMAIL_CONTENT_HEADER,
                        VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME))
                .putString(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)
                .putBoolean(PREF_NOTIFY_SEND_SUCCESS, true)
                .putBoolean(PREF_RESEND_UNSENT, true)
                .putString(PREF_FILTER_PHONE_BLACKLIST, commaJoin(asSet("+123456789", "+9876543*")))
                .putString(PREF_FILTER_TEXT_BLACKLIST, commaJoin(asSet("Bad text", quoteRegex("Expression"))))
                .apply();

        refreshPreferenceViews();
        showToast(context, "Done");
    }

    private void onGetContact() {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        new AlertDialog.Builder(context)
                .setTitle("Phone number")
                .setView(alertDialogView(input))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phone = input.getText().toString();
                        String contact = ContentUtils.getContactName(context, phone);
                        String text = contact != null ? (phone + ": " + contact) : "Contact not found";

                        showToast(context, text);
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

        refreshPreferenceViews();
        showToast(context, "Done");
    }

    private void onLoadDefaultPreferences() {
        settings.loadDefaults();

        refreshPreferenceViews();
        showToast(context, "Done");
    }

    private void onSendDebugMail() {
        new SendDebugMailTask(getActivity(), loadDebugProperties()).execute();
    }

    private void onProcessServiceMail() {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_ENABLED, false)) {
            RemoteControlService.startRemoteControlService(context);
            showToast(context, "Done");
        } else {
            showToast(context, "Feature disabled");
        }
    }

    private void onRequestGooglePermission() {
        authorizator.selectAccount();
    }

    private void onProcessSingleEvent() {
        long start = System.currentTimeMillis();

        PhoneEvent event = new PhoneEvent();
        event.setRecipient(deviceName());
        event.setPhone("5556");
        event.setText("SMS TEXT");
        event.setIncoming(true);
        event.setStartTime(start);
        event.setEndTime(start + 10000);

        startCallProcessingService(context, event);
        showToast(context, "Done");
    }

    private void onStartProcessPendingEvents() {
        PendingCallProcessorService.startPendingCallProcessorService(context);
        showToast(context, "Done");
    }

    private void onRequireReceiveSmsPermission() {
        if (ContextCompat.checkSelfPermission(context, RECEIVE_SMS) == PERMISSION_GRANTED) {
            context.enforceCallingOrSelfPermission(Manifest.permission.RECEIVE_SMS, "Testing SMS permission");
        } else {
            showToast(context, "SMS PERMISSION DENIED");
        }
    }

    private void onRequestSmsPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{RECEIVE_SMS},
                PERMISSIONS_REQUEST_RECEIVE_SMS);
    }

    private void onClearLogs() {
        File dir = new File(context.getFilesDir(), "log");
        File[] logs = requireNonNull(dir.listFiles());
        for (File file : logs) {
            if (!file.delete()) {
                DebugFragment.log.warn("Cannot delete file");
            }
        }
        showToast(context, "Removed " + logs.length + " log files");
    }

    private void onAddHistoryItem() {
        database.putEvent(new PhoneEvent("+79052345670", true, System.currentTimeMillis(), null, false,
                "Debug message", null, null, PhoneEvent.STATE_PENDING, deviceName()));
        database.notifyChanged();
        showToast(context, "Done");
    }

    private void onPopulateHistory() {
        long time = System.currentTimeMillis();
        String recipient = deviceName();
        database.putEvent(new PhoneEvent("+79052345671", true, time, null, false, "Debug message", null, null, PhoneEvent.STATE_PENDING, recipient));
        database.putEvent(new PhoneEvent("+79052345672", false, time += 1000, null, false, "Debug message", null, null, PhoneEvent.STATE_PROCESSED, recipient));
        database.putEvent(new PhoneEvent("+79052345673", true, time += 1000, time + 10000, false, null, null, null, PhoneEvent.STATE_IGNORED, recipient));
        database.putEvent(new PhoneEvent("+79052345674", false, time += 1000, time + 10000, false, null, null, null, PhoneEvent.STATE_PENDING, recipient));
        database.putEvent(new PhoneEvent("+79052345675", true, time += 1000, time + 10000, true, null, null, null, PhoneEvent.STATE_PENDING, recipient));
        database.putEvent(new PhoneEvent("+79052345671", true, time += 1000, null, false, "Debug message", null, "Test exception +79052345671", PhoneEvent.STATE_PENDING, recipient));
        database.putEvent(new PhoneEvent("+79052345672", false, time += 1000, null, false, "Debug message", null, "Test exception +79052345672", PhoneEvent.STATE_PENDING, recipient));
        database.putEvent(new PhoneEvent("+79052345673", true, time += 1000, time + 10000, false, null, null, "Test exception +79052345673", PhoneEvent.STATE_PENDING, recipient));
        database.putEvent(new PhoneEvent("+79052345674", false, time += 1000, time + 10000, false, null, null, "Test exception +79052345674", PhoneEvent.STATE_PENDING, recipient));
        database.putEvent(new PhoneEvent("+79052345675", true, time += 1000, time + 10000, true, null, null, "Test exception +79052345675", PhoneEvent.STATE_PENDING, recipient));
        database.notifyChanged();

        showToast(context, "Done");
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
                log.debug("Concurrent package:" + activityInfo.packageName + " priority: " +
                        resolveInfo.priority);
            }
        }
        showMessage(context, b.toString());
    }

    private void onGoogleDriveClear() {
        Tasks.call(newSingleThreadExecutor(), new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                GoogleDrive drive = new GoogleDrive(context, senderAccount());
                drive.clear();
                return null;
            }
        });

        showToast(context, "Done");
    }

    private void onGoogleDriveSync() {
        Tasks.call(newSingleThreadExecutor(), new Callable<Void>() {

            @Override
            public Void call() {
//                new SyncAdapter(context, false).sync(context, selectedAccount(context));
                SyncManager.syncNow(context);
                return null;
            }
        });

        showToast(context, "Done");
    }

    private void onGoogleDriveDownload() {
        Tasks.call(newSingleThreadExecutor(), new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                GoogleDrive drive = new GoogleDrive(context, senderAccount());
                new SyncAdapter(context, false).download(drive);
                return null;
            }
        });

        showToast(context, "Done");
    }

    private void onGoogleDriveUpload() {
        Tasks.call(newSingleThreadExecutor(), new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                GoogleDrive drive = new GoogleDrive(context, senderAccount());
                new SyncAdapter(context, false).upload(drive);
                return null;
            }
        });

        showToast(context, "Done");
    }

/*
    private void onGoogleDriveList() {
        Tasks.call(newSingleThreadExecutor(), new Callable<String>() {

            @Override
            public String call() throws Exception {
                GoogleDrive drive = new GoogleDrive(context, senderAccount());
                List<com.google.api.services.drive.model.File> files = drive.list();
                return null;
            }
        });

    }
*/

    @SuppressLint("SetTextI18n")
    private void onSendSms() {
        final EditText input = new EditText(context);
        input.setText("5556");
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        new AlertDialog.Builder(context)
                .setTitle("Phone number")
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phone = input.getText().toString();
                        PendingIntent sentIntent = getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
                        PendingIntent deliveredIntent = getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);

                        try {
                            SmsManager.getDefault().sendTextMessage(phone, null, "Debug message", sentIntent, deliveredIntent);
                        } catch (Throwable x) {
                            log.error("Failed: ", x);
                            showToast(context, "Failed");
                        }
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

    private Account senderAccount() {
        String name = settings.getString(PREF_SENDER_ACCOUNT, null);
        return new GoogleAccountManager(context).getAccountByName(name);
    }

    private static void showMessage(Context context, String message) {
        new AlertDialog.Builder(context)
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

    private static class GetLocationTask extends LongAsyncTask<Void, Void, GeoCoordinates> {


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
            super.onPostExecute(coordinates);
            showMessage(getActivity(), coordinates != null ?
                    formatCoordinates(coordinates) : "No location received");
        }
    }

    private static class SendDebugMailTask extends LongAsyncTask<Void, Void, String> {

        private Properties properties;

        private SendDebugMailTask(Activity activity, Properties properties) {
            super(activity);
            this.properties = properties;
        }

        @Override
        protected String doInBackground(Void... params) {
            GoogleMail transport = new GoogleMail(getActivity());
            try {
                transport.startSession(primaryAccount(getActivity()).name, GMAIL_SEND);

                MailMessage message = new MailMessage();
                message.setSubject("test subject");
                message.setBody("test message from " + deviceName());
                message.setRecipients(requireNonNull(properties.getProperty("default_recipient")));

                transport.send(message);
            } catch (Exception x) {
                log.error("FAILED: ", x);
                return "Sending mail failed: " + x.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                showToast(getActivity(), "Done");
            } else {
                showMessage(getActivity(), result);
            }
        }

    }

    private static class SendLogTask extends LongAsyncTask<Void, Void, Exception> {

        private Properties properties;

        private SendLogTask(Activity activity, Properties properties) {
            super(activity);
            this.properties = properties;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            List<File> attachment = new LinkedList<>();
            attachment.add(getActivity().getDatabasePath(Database.DATABASE_NAME));
            attachment.add(getLogcatLog());

            File[] files = new File(getActivity().getFilesDir(), "log").listFiles();
            attachment.addAll(asList(requireNonNull(files)));

            try {
                GoogleMail transport = new GoogleMail(getActivity());
                transport.startSession(primaryAccount(getActivity()).name, GMAIL_SEND);

                for (File file : attachment) {
                    MailMessage message = new MailMessage();
                    message.setSubject("[SMailer] log: " + file.getName());
                    message.setBody("Device: " + deviceName() + "<br>File: " + file.getName());
                    message.setAttachment(ImmutableSet.of(file));
                    message.setRecipients(properties.getProperty("developer_email"));

                    transport.send(message);
                }
            } catch (Exception x) {
                log.error("Send mail failed", x);
                return x;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception error) {
            super.onPostExecute(error);
            if (error != null) {
                showMessage(getActivity(), error.toString());
            } else {
                showToast(getActivity(), "Done");
            }
        }

        private File getLogcatLog() {
            File file = new File(getActivity().getFilesDir(), "logcat.log");
            try {
                Process process = Runtime.getRuntime().exec("logcat -d");
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                PrintWriter out = new PrintWriter(new FileOutputStream(file));
                String line;
                while ((line = in.readLine()) != null) {
                    out.println(line);
                }
                in.close();
                out.close();
            } catch (IOException x) {
                log.error("Cannot get logcat ", x);
            }
            return file;
        }
    }

    private abstract class DefaultClickListener implements Preference.OnPreferenceClickListener {

        protected abstract void onClick(Preference preference);

        @Override
        public boolean onPreferenceClick(Preference preference) {
            onClick(preference);
            return true;
        }
    }

    private class SentStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message;
            switch (getResultCode()) {
                case RESULT_OK:
                    message = "Message sent successfully";
                    break;
                case RESULT_ERROR_GENERIC_FAILURE:
                    message = "Generic failure error";
                    break;
                case RESULT_ERROR_NO_SERVICE:
                    message = "No service available";
                    break;
                case RESULT_ERROR_NULL_PDU:
                    message = "Null PDU";
                    break;
                case RESULT_ERROR_RADIO_OFF:
                    message = "Radio is off";
                    break;
                default:
                    message = "Unknown error";
                    break;
            }
            showToast(context, message);
            log.debug(message);
        }
    }

    private class DeliveryStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message;
            switch (getResultCode()) {
                case RESULT_OK:
                    message = "Message delivered successfully";
                    break;
                case RESULT_CANCELED:
                    message = "Delivery cancelled";
                    break;
                default:
                    message = "Message not delivered";
                    break;
            }
            showToast(context, message);
            log.debug(message);
        }

    }

}
