package com.bopr.android.smailer.settings;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.Mailer;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.MailTransport;

import java.io.File;
import java.util.Map;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_PROTOCOL;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SERVICE_ENABLED;

/**
 * Main activity's fragment.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "bopr.SettingsFragment";
    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 100;

    private SwitchPreference enabledPreference;
    private EditTextPreference recipientsPreference;
    private EditTextPreference accountPreference;
    private EditTextPreference passwordPreference;
    private EditTextPreference protocolPreference;
    private EditTextPreference hostPreference;
    private EditTextPreference portPreference;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(Settings.PREFERENCES_STORAGE_NAME);
        preferences = preferenceManager.getSharedPreferences();

        enabledPreference = (SwitchPreference) findPreference(KEY_PREF_SERVICE_ENABLED);
        enabledPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean enabled = (boolean) value;
                checkSmsPermission(enabled);
                updateEnabledPreference(enabled);
                return true;
            }
        });

        accountPreference = (EditTextPreference) findPreference(KEY_PREF_SENDER_ACCOUNT);
        accountPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateAccountPreference((String) value);
                return true;
            }
        });

        passwordPreference = (EditTextPreference) findPreference(KEY_PREF_SENDER_PASSWORD);
        passwordPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updatePasswordPreference((String) value);
                return true;
            }
        });

        recipientsPreference = (EditTextPreference) findPreference(KEY_PREF_RECIPIENT_EMAIL_ADDRESS);
        recipientsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateRecipientsPreference((String) value);
                return true;
            }
        });

        protocolPreference = (EditTextPreference) findPreference(KEY_PREF_EMAIL_PROTOCOL);
        protocolPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateProtocolPreference((String) value);
                return true;
            }
        });

        hostPreference = (EditTextPreference) findPreference(KEY_PREF_EMAIL_HOST);
        hostPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateHostPreference((String) value);
                return true;
            }
        });

        portPreference = (EditTextPreference) findPreference(KEY_PREF_EMAIL_PORT);
        portPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updatePortPreference((String) value);
                return true;
            }
        });

        bouncePreferencesChangeListener(getPreferenceScreen());

        addDebugItems();
    }

    private void updateEnabledPreference(boolean value) {
        enabledPreference.setSummary(value
                ? R.string.pref_description_service_on
                : R.string.pref_description_service_off);
    }

    private void updateAccountPreference(String value) {
        updateSummary(value, accountPreference);
    }

    private void updatePasswordPreference(String value) {
        passwordPreference.setSummary(value != null && !value.isEmpty()
                ? R.string.password_asterisk : R.string.not_set);
    }

    private void updateRecipientsPreference(String value) {
        EditTextPreference preference = this.recipientsPreference;
        updateSummary(value, preference);
    }

    private void updateProtocolPreference(String value) {
        updateSummary(value, protocolPreference);
        //TODO: update options summary
    }

    private void updateHostPreference(String value) {
        updateSummary(value, hostPreference);
        //TODO: update options summary
    }

    private void updatePortPreference(String value) {
        updateSummary(value, portPreference);
        //TODO: update options summary
    }

    private void updateSummary(String value, EditTextPreference preference) {
        if (value == null || value.isEmpty()) {
            preference.setSummary(R.string.not_set);
        } else {
            preference.setSummary(value);
        }
    }

    private void bouncePreferencesChangeListener(PreferenceGroup group) {
        Map<String, ?> map = preferences.getAll();
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);

            OnPreferenceChangeListener listener = preference.getOnPreferenceChangeListener();
            if (listener != null) {
                Object value = map.get(preference.getKey());
                listener.onPreferenceChange(preference, value);
            }

            if (preference instanceof PreferenceGroup) {
                refreshPreferences((PreferenceGroup) preference);

            }
        }
    }

    private void checkSmsPermission(boolean enabled) {
        if (enabled && smsPermissionDenied()) {
            requestSmsPermission();
        }
    }

    private boolean smsPermissionDenied() {
        return ContextCompat.checkSelfPermission(getActivity(), RECEIVE_SMS) != PERMISSION_GRANTED;
    }

    public void requestSmsPermission() {
        if (smsPermissionDenied()) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{RECEIVE_SMS},
                    PERMISSIONS_REQUEST_RECEIVE_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.message_service_disabled_by_permission, Toast.LENGTH_SHORT).show();
//                enabledPreference.getOnPreferenceChangeListener().onPreferenceChange(enabledPreference, false);
                enabledPreference.setChecked(false);
                updateEnabledPreference(false);
            }
        }
    }

    private void addDebugItems() {
        findPreference("sendDefaultMail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        String user = getResources().getString(R.string.default_sender);

                        MailTransport transport = new MailTransport(
                                user,
                                getResources().getString(R.string.default_password),
                                "smtp",
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
                return true;
            }
        });

        findPreference("sendMail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        MailMessage message = new MailMessage("+79052345678", "Hello there!", System.currentTimeMillis());
                        Mailer.getInstance().send(getActivity(), message);
                        return null;
                    }
                }.execute();
                return true;
            }
        });

        findPreference("setDefaultPreferences").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Resources r = getResources();

                preferences
                        .edit()
                        .putBoolean(KEY_PREF_SERVICE_ENABLED, true)
                        .putString(KEY_PREF_SENDER_ACCOUNT, r.getString(R.string.default_sender))
//                        .putString(KEY_PREF_SENDER_PASSWORD, EncryptUtil.encrypt(getActivity(), r.getString(R.string.default_password)))
                        .putString(KEY_PREF_SENDER_PASSWORD, r.getString(R.string.default_password))
                        .putString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, r.getString(R.string.default_recipient))
                        .putString(KEY_PREF_EMAIL_PROTOCOL, "smtp")
                        .putString(KEY_PREF_EMAIL_HOST, "smtp.gmail.com")
                        .putString(KEY_PREF_EMAIL_PORT, "465")
                        .apply();
                refreshPreferences(getPreferenceScreen());
                return true;
            }
        });

        findPreference("clearPreferences").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                clearPreferences();
                refreshPreferences(getPreferenceScreen());
                return true;
            }
        });

        findPreference("requireReceiveSmsPermission").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!smsPermissionDenied()) {
                    getActivity().enforceCallingOrSelfPermission(
                            Manifest.permission.RECEIVE_SMS, "Testing SMS permission");
                } else {
                    Toast.makeText(getActivity(), "SMS PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
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

        findPreference("close").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().finish();
                return true;
            }
        });

    }

    private void clearPreferences() {
        preferences.edit().clear().apply();

        File dir = new File(getActivity().getFilesDir().getParent() + "/shared_prefs/");
        String[] files = dir.list();
        for (String file : files) {
            SharedPreferences preferences = getActivity().getSharedPreferences(file.replace(".xml", ""), Context.MODE_PRIVATE);
            preferences.edit().clear().apply();
        }

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    //ok
//                }

        for (String file : files) {
            new File(dir, file).delete();
        }
    }

    private void refreshPreferences(PreferenceGroup group) {
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                refreshPreferences((PreferenceGroup) preference);
            } else if (preference instanceof EditTextPreference) {
                String value = preferences.getString(preference.getKey(), "");
                preference.getOnPreferenceChangeListener().onPreferenceChange(preference, value);
                ((EditTextPreference) preference).setText(value);
            } else if (preference instanceof SwitchPreference) {
                Boolean value = preferences.getBoolean(preference.getKey(), false);
                preference.getOnPreferenceChangeListener().onPreferenceChange(preference, value);
                ((SwitchPreference) preference).setChecked(value);
            }
        }
    }

}
