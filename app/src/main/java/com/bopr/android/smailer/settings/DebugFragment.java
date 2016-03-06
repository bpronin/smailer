package com.bopr.android.smailer.settings;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.Mailer;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.ContactUtil;
import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.LocationProvider;
import com.bopr.android.smailer.util.MailTransport;
import com.bopr.android.smailer.util.PermissionUtil;
import com.bopr.android.smailer.util.StringUtil;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SERVICE_ENABLED;

/**
 * For debug purposes.
 */
public class DebugFragment extends DefaultPreferenceFragment {

    private static final String TAG = "bopr.DebugFragment";
    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 100;

    private LocationProvider locationProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_debug);

        locationProvider = new LocationProvider(getActivity());

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

        findPreference("close").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().finish();
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

    private boolean smsPermissionDenied() {
        return ContextCompat.checkSelfPermission(getActivity(), RECEIVE_SMS) != PERMISSION_GRANTED;
    }

    public void requestSmsPermission() {
        Activity activity = getActivity();
        if (PermissionUtil.isSmsPermissionDenied(activity)) {
            PermissionUtil.requestSmsPermission(activity, PERMISSIONS_REQUEST_RECEIVE_SMS);
        }
    }

    private void onRestorePreferences() {
        Resources r = getResources();
        getSharedPreferences()
                .edit()
                .putBoolean(KEY_PREF_SERVICE_ENABLED, true)
                .putString(KEY_PREF_SENDER_ACCOUNT, r.getString(R.string.default_sender))
//                        .putString(KEY_PREF_SENDER_PASSWORD, EncryptUtil.encrypt(getActivity(), r.getString(R.string.default_password)))
                .putString(KEY_PREF_SENDER_PASSWORD, r.getString(R.string.default_password))
                .putString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, r.getString(R.string.default_recipient))
                .putString(KEY_PREF_EMAIL_HOST, "smtp.gmail.com")
                .putString(KEY_PREF_EMAIL_PORT, "465")
                .apply();
        refreshPreferences(getPreferenceScreen());
    }

    private void onGetContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Phone number");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String phone = input.getText().toString();
                String contact = ContactUtil.getContactName(getActivity(), phone);
                String text = contact != null ? (phone + ": " + contact) : "Contact not found";

                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void onGetLocation() {
        Location location = locationProvider.getLocation();
        Toast.makeText(getActivity(),
                location != null ? StringUtil.formatLocation(location) : "No location received",
                Toast.LENGTH_LONG).show();
    }

    private void onClearPreferences() {
        getSharedPreferences().edit().clear().apply();

//        File dir = new File(getActivity().getFilesDir().getParent() + "/shared_prefs/");
//        String[] files = dir.list();
//        for (String file : files) {
//            SharedPreferences preferences = getActivity().getSharedPreferences(file.replace(".xml", ""), Context.MODE_PRIVATE);
//            preferences.edit().clear().apply();
//        }
//
////                try {
////                    Thread.sleep(1000);
////                } catch (InterruptedException e) {
////                    //ok
////                }
//
//        for (String file : files) {
//            new File(dir, file).delete();
//        }
        refreshPreferences(getPreferenceScreen());
    }

    private void onSendDefaultMail() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                String user = getResources().getString(R.string.default_sender);

                MailTransport transport = new MailTransport(
                        user,
                        getResources().getString(R.string.default_password),
                        "smtp.gmail.com",
                        "465"
                );

                try {
                    transport.send(
                            "test subject",
                            "test message from " + DeviceUtil.getDeviceName(),
                            user,
                            getResources().getString(R.string.default_recipient)
                    );
                } catch (Exception x) {
                    Log.e(TAG, "FAILED: ", x);
                }
                return null;
            }
        }.execute();
    }

    private void onSendMail() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                MailMessage message = new MailMessage("+79052345678", "Hello there!", System.currentTimeMillis(), locationProvider.getLocation());
                new Mailer().send(getActivity(), message);
                return null;
            }
        }.execute();
    }

    private void onRequireReceiveSmsPermission() {
        if (!smsPermissionDenied()) {
            getActivity().enforceCallingOrSelfPermission(
                    Manifest.permission.RECEIVE_SMS, "Testing SMS permission");
        } else {
            Toast.makeText(getActivity(), "SMS PERMISSION DENIED", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_LONG).show();
            }
        }
    }

}
