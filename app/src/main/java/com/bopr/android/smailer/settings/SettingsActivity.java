package com.bopr.android.smailer.settings;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.SmsReceiver;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "bo.SettingsActivity";

    public static final String PREF_SERVICE_ENABLED = "service_enabled";
    public static final String PREF_SENDER_EMAIL_ADDRESS = "sender_email_address";
    public static final String PREF_SENDER_EMAIL_PASSWORD = "sender_email_password";
    public static final String PREF_RECIPIENT_EMAIL_ADDRESS = "recipient_email_address";

    private PreferenceChangeListener bindPreferenceSummaryToValueListener = new PreferenceChangeListener();
    private SharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferenceChangeListener();
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateSmsReceiver();
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        GeneralPreferenceFragment preferenceFragment = new GeneralPreferenceFragment();

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, preferenceFragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
         /* determine if the device has an extra-large screen. For example, 10" tablets are extra-large. */
        return (this.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);

        bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "")
        );
    }

    private void updateSmsReceiver() {
        ComponentName component = new ComponentName(this, SmsReceiver.class);
        boolean enabled = preferences.getBoolean(PREF_SERVICE_ENABLED, false);
        int state = (enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED);
        getPackageManager().setComponentEnabledSetting(component, state, DONT_KILL_APP);

        Log.i(TAG, "SMS broadcast receiver state is: " + (enabled ? "ENABLED" : "DISABLED"));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private class PreferenceChangeListener implements OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof EditTextPreference) {
                updateTextSummary(preference, stringValue);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        private void updateTextSummary(Preference preference, String stringValue) {
            EditText editText = ((EditTextPreference) preference).getEditText();
            boolean isPasswordField = editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            if (isPasswordField) {
                boolean isPasswordSet = stringValue != null && !stringValue.isEmpty();
                if (isPasswordSet) {
                    preference.setSummary("*****");
                } else {
                    preference.setSummary(R.string.not_set);
                }
            } else {
                preference.setSummary(stringValue);
            }
        }

    }

    private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "Shared preference changed: " + key);

            if (key.equals(PREF_SERVICE_ENABLED)) {
                updateSmsReceiver();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            SettingsActivity activity = (SettingsActivity) getActivity();
            activity.bindPreferenceSummaryToValue(findPreference(PREF_SENDER_EMAIL_ADDRESS));
            activity.bindPreferenceSummaryToValue(findPreference(PREF_SENDER_EMAIL_PASSWORD));
            activity.bindPreferenceSummaryToValue(findPreference(PREF_RECIPIENT_EMAIL_ADDRESS));
        }

    }

}
