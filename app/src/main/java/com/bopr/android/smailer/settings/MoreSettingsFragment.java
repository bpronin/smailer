package com.bopr.android.smailer.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

import com.bopr.android.smailer.R;

import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_PORT;

/**
 * More settings activity's fragment.
 */
public class MoreSettingsFragment extends DefaultPreferenceFragment {

    private static final String TAG = "bopr.MoreSettingsFragment";

    private EditTextPreference hostPreference;
    private EditTextPreference portPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_more);

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
    }

    private void updateHostPreference(String value) {
        updateSummary(value, hostPreference);
    }

    private void updatePortPreference(String value) {
        updateSummary(value, portPreference);
    }

}
