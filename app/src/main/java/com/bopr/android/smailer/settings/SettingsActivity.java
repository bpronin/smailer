package com.bopr.android.smailer.settings;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.bopr.android.smailer.MailSender;
import com.bopr.android.smailer.MailSenderProperties;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.SmsReceiver;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static com.bopr.android.smailer.settings.Settings.DEFAULT_EMAIL_SUBJECT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_SUBJECT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_EMAIL_ADDRESS;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_EMAIL_PASSWORD;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_NAME;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SERVICE_ENABLED;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "bo.SettingsActivity";

    private SharedPreferences preferences;
    private PreferenceChangeListener bindPreferenceSummaryToValueListener = new PreferenceChangeListener();
    private SharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferenceChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        updateSmsReceiver();

        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MainPreferenceFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);

        bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                preferences.getString(preference.getKey(), ""));
    }

    /**
     * Set the enabled setting for sms broadcast receiver according to current preference value.
     */
    private void updateSmsReceiver() {
        ComponentName component = new ComponentName(this, SmsReceiver.class);
        boolean enabled = preferences.getBoolean(KEY_PREF_SERVICE_ENABLED, false);
        int state = (enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED);
        getPackageManager().setComponentEnabledSetting(component, state, DONT_KILL_APP);

        Log.d(TAG, "SMS broadcast receiver state is: " + (enabled ? "ENABLED" : "DISABLED"));
    }

    private void updateMailSender() {
        MailSenderProperties properties = new MailSenderProperties();

        String user = preferences.getString(KEY_PREF_SENDER_EMAIL_ADDRESS, "");
        properties.setUser(user);
        properties.setPassword(preferences.getString(KEY_PREF_SENDER_EMAIL_PASSWORD, ""));
        properties.setRecipients(preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, ""));
        properties.setSender(preferences.getString(KEY_PREF_SENDER_NAME, user));
        properties.setSubject(preferences.getString(KEY_PREF_EMAIL_SUBJECT, DEFAULT_EMAIL_SUBJECT));

        MailSender.getInstance().setProperties(properties);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private class PreferenceChangeListener implements OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String s = value.toString();

            if (isPasswordField(preference)) {
                boolean passwordSet = s != null && !s.isEmpty();
                preference.setSummary(passwordSet ? R.string.password_asterisk : R.string.not_set);
            } else {
                preference.setSummary(s);
            }

            return true;
        }

        private boolean isPasswordField(Preference preference) {
            if (preference instanceof EditTextPreference) {
                EditText editText = ((EditTextPreference) preference).getEditText();
                return editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                return false;
            }
        }

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
                case KEY_PREF_SENDER_EMAIL_ADDRESS:
                case KEY_PREF_SENDER_EMAIL_PASSWORD:
                case KEY_PREF_RECIPIENT_EMAIL_ADDRESS:
                    updateMailSender();
                    break;
            }
        }

    }

    /**
     * Main activity's fragment.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            SettingsActivity activity = (SettingsActivity) getActivity();
            activity.bindPreferenceSummaryToValue(findPreference(KEY_PREF_SENDER_EMAIL_ADDRESS));
            activity.bindPreferenceSummaryToValue(findPreference(KEY_PREF_SENDER_EMAIL_PASSWORD));
            activity.bindPreferenceSummaryToValue(findPreference(KEY_PREF_RECIPIENT_EMAIL_ADDRESS));
        }

    }

}
