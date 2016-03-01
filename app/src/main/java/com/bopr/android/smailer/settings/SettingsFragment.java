package com.bopr.android.smailer.settings;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.MailSender;
import com.bopr.android.smailer.MailSenderProperties;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.SmsReceiver;

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

    private SwitchPreference enabledPreference;
    private EditTextPreference recipientsPreference;
    private EditTextPreference accountPreference;
    private EditTextPreference passwordPreference;
    private SharedPreferences preferences;
    private SharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferenceChangeListener();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

//        preferences.edit().clear().apply();

        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

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

        enabledPreference = (SwitchPreference) findPreference(KEY_PREF_SERVICE_ENABLED);
        enabledPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateEnabledPreferenceSummary((boolean) value);
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

        updateEnabledPreferenceSummary(preferences.getBoolean(KEY_PREF_SERVICE_ENABLED, false));
        updateAccountPreference(preferences.getString(KEY_PREF_SENDER_ACCOUNT, ""));
        updatePasswordPreference(preferences.getString(KEY_PREF_SENDER_PASSWORD, ""));
        updateRecipientsPreference(preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, ""));

        updateSmsReceiver();
        updateMailer();

//        ///////////////////
//        findPreference("test").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                new AsyncTask<Void, Void, Void>() {
//
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        MailMessage message = new MailMessage("+79052309441", "Hello there!");
//                        MailSender.getInstance().send(getActivity(), message);
//                        return null;
//                    }
//                }.execute();
//                return true;
//            }
//        });
    }

    @Override
    public void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    private void updateSmsReceiver() {
        boolean enabled = preferences.getBoolean(KEY_PREF_SERVICE_ENABLED, false);
        SmsReceiver.enableComponent(getActivity(), enabled);
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

    private void updateEnabledPreferenceSummary(boolean value) {
        enabledPreference.setSummary(value
                ? R.string.pref_description_service_on
                : R.string.pref_description_service_off);
    }

    private void updateAccountPreference(String value) {
        accountPreference.setSummary(value);
    }

    private void updatePasswordPreference(String value) {
        passwordPreference.setSummary(value != null && !value.isEmpty()
                ? R.string.password_asterisk : R.string.not_set);
    }

    private void updateRecipientsPreference(String value) {
        recipientsPreference.setSummary(value);
    }

    /**
     * A preference value change listener that updates state of application components.
     */
    private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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

}
