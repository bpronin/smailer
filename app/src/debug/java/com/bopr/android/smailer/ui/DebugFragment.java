package com.bopr.android.smailer.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.LocationProvider;
import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.MailerService;
import com.bopr.android.smailer.PermissionsChecker;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_LOCALE;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_AVAILABLE_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.getDeviceName;

/**
 * For debug purposes.
 */
public class DebugFragment extends DefaultPreferenceFragment {

    private static final String TAG = "DebugFragment";
    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 100;

    private LocationProvider locationProvider;
    private Cryptor cryptor;
    private Database database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_debug);

        database = new Database(getActivity());
        locationProvider = new LocationProvider(getActivity(), database);
        cryptor = new Cryptor(getActivity());

        findPreference("sendDefaultMail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onSendDefaultMail();
                return true;
            }
        });

        findPreference("sendMail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onSendMail();
                return true;
            }
        });

        findPreference("setDefaultPreferences").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onRestorePreferences();
                return true;
            }
        });

        findPreference("clearPreferences").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onClearPreferences();
                return true;
            }
        });

        findPreference("requireReceiveSmsPermission").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onRequireReceiveSmsPermission();
                return true;
            }
        });

        findPreference("requestReceiveSmsPermission").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestSmsPermission();
                return true;
            }
        });

        findPreference("get_location").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onGetLocation();
                return true;
            }
        });

        findPreference("get_contact").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onGetContact();
                return true;
            }
        });

        findPreference("save_log").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onSaveLog();
                return true;
            }
        });

        findPreference("close").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().finish();
                return true;
            }
        });

        findPreference("show_password").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onShowPassword();
                return true;
            }
        });

        findPreference("populate_log").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onPopulateLog();
                return true;
            }
        });

        findPreference("clear_log").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                onClearLog();
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        locationProvider.start();
    }

    @Override
    public void onStop() {
        locationProvider.stop();
        super.onStop();
    }

    @NonNull
    private Properties getDebugProperties() {
        Properties properties = new Properties();
        try {
            InputStream stream = getActivity().getAssets().open("debug.properties");
            properties.load(stream);
        } catch (IOException x) {
            Log.e(TAG, "Cannot read debug properties", x);
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
                .putString(KEY_PREF_AVAILABLE_RECIPIENTS_ADDRESS, properties.getProperty("default_recipient"))
                .putString(KEY_PREF_RECIPIENTS_ADDRESS, properties.getProperty("default_recipient"))
                .putString(KEY_PREF_EMAIL_HOST, DEFAULT_HOST)
                .putString(KEY_PREF_EMAIL_PORT, DEFAULT_PORT)
                .putStringSet(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
                .putStringSet(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
                .putString(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE)
                .apply();
        refreshPreferences(getPreferenceScreen());
    }

    private void onGetContact() {
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        new AlertDialog.Builder(getActivity())
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
                return locationProvider.getLocation(3000);
            }

            @Override
            protected void onPostExecute(GeoCoordinates coordinates) {
                Toast.makeText(getActivity(),
                        coordinates != null ? Util.formatLocation(coordinates)
                                : "No location received",
                        Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void onClearPreferences() {
        getSharedPreferences().edit().clear().apply();
        refreshPreferences(getPreferenceScreen());
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
                            "test message from " + getDeviceName(),
                            user,
                            properties.getProperty("default_recipient")
                    );
                } catch (Exception x) {
                    Log.e(TAG, "FAILED: ", x);
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
        MailerService.startForIncomingCall(getActivity(), "+79052345678", start, start + 10000);
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
//            Process process = Runtime.getRuntime().exec("logcat com.bopr.android.smailer:D -d");
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
            Log.e(TAG, "Save log failed", x);
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

}
