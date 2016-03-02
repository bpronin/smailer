package com.bopr.android.smailer.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.MailSender;
import com.bopr.android.smailer.MailSenderProperties;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.SmsReceiver;
import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.mail.GMailSender;

import java.util.Date;
import java.util.Map;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.settings.Settings.DEFAULT_EMAIL_HOST;
import static com.bopr.android.smailer.settings.Settings.DEFAULT_EMAIL_PORT;
import static com.bopr.android.smailer.settings.Settings.DEFAULT_EMAIL_PROTOCOL;
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
    private SharedPreferences preferences;
    private EditTextPreference protocolPreference;
    private EditTextPreference hostPreference;
    private EditTextPreference portPreference;
    private SharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferenceChangeListener();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        preferences = getPreferenceManager().getSharedPreferences();

        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

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
//        protocolPreference.setDefaultValue(DEFAULT_EMAIL_PROTOCOL);
        protocolPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateProtocolPreference((String) value);
                return true;
            }
        });

        hostPreference = (EditTextPreference) findPreference(KEY_PREF_EMAIL_HOST);
//        hostPreference.setDefaultValue(DEFAULT_EMAIL_HOST);
        hostPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateHostPreference((String) value);
                return true;
            }
        });

        portPreference = (EditTextPreference) findPreference(KEY_PREF_EMAIL_PORT);
//        portPreference.setDefaultValue(Settings.DEFAULT_EMAIL_PORT);
        portPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updatePortPreference((String) value);
                return true;
            }
        });

        bouncePreferencesChangeListener(getPreferenceScreen());

        updateSmsReceiver();
        updateMailer();

        addDebugItems();
    }

    @Override
    public void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    private void updateSmsReceiver() {
        boolean enabled = preferences.getBoolean(KEY_PREF_SERVICE_ENABLED, false);
        SmsReceiver.enableComponent(getActivity(), enabled || !smsPermissionDenied());
    }

    private void updateMailer() {
        MailSenderProperties properties = new MailSenderProperties();

        String account = preferences.getString(KEY_PREF_SENDER_ACCOUNT, "");
        properties.setUser(account);
        properties.setPassword(preferences.getString(KEY_PREF_SENDER_PASSWORD, ""));
        properties.setRecipients(preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, ""));
        properties.setProtocol(preferences.getString(KEY_PREF_EMAIL_PROTOCOL, DEFAULT_EMAIL_PROTOCOL));
        properties.setHost(preferences.getString(KEY_PREF_EMAIL_HOST, DEFAULT_EMAIL_HOST));
        properties.setPort(preferences.getString(KEY_PREF_EMAIL_PORT, DEFAULT_EMAIL_PORT));

        MailSender.getInstance().setProperties(properties);
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

    /**
     * A preference value change listener that updates state of application components.
     */
    private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            Log.d(TAG, "Shared preference changed: " + key);

            switch (key) {
                case KEY_PREF_SERVICE_ENABLED:
                    updateSmsReceiver();
                    break;
                case KEY_PREF_SENDER_ACCOUNT:
                case KEY_PREF_SENDER_PASSWORD:
                case KEY_PREF_RECIPIENT_EMAIL_ADDRESS:
                case KEY_PREF_EMAIL_HOST:
                case KEY_PREF_EMAIL_PROTOCOL:
                case KEY_PREF_EMAIL_PORT:
                    updateMailer();
                    break;
            }
        }

    }

    // TODO: DEBUG STUFF BELOW. REMOVE ALL

    private void addDebugItems() {
        findPreference("sendDefaultMail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        String user = getResources().getString(R.string.default_sender);

                        try {
                            new GMailSender(
                                    user,
                                    getResources().getString(R.string.default_password),
                                    DEFAULT_EMAIL_PROTOCOL,
                                    DEFAULT_EMAIL_HOST,
                                    DEFAULT_EMAIL_PORT
                            ).sendMail(
                                    "subject",
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
                        MailMessage message = new MailMessage("+79052345678", "Hello there!", new Date());
                        MailSender.getInstance().send(getActivity(), message);
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
                        .putString(KEY_PREF_EMAIL_PROTOCOL, DEFAULT_EMAIL_PROTOCOL)
                        .putString(KEY_PREF_EMAIL_HOST, DEFAULT_EMAIL_HOST)
                        .putString(KEY_PREF_EMAIL_PORT, DEFAULT_EMAIL_PORT)
                        .apply();
                refreshPreferences(getPreferenceScreen());
                return true;
            }
        });

        findPreference("clearPreferences").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                preferences
                        .edit()
                        .clear()
                        .apply();
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
