package com.bopr.android.smailer.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import com.bopr.android.smailer.R;

/**
 * Class DefaultPreferenceFragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DefaultPreferenceFragment extends PreferenceFragment {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(Settings.PREFERENCES_STORAGE_NAME);
        sharedPreferences = preferenceManager.getSharedPreferences();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshPreferences(getPreferenceScreen());
    }

    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    /**
     * Updates summary of {@link EditTextPreference}.
     *
     * @param value      value
     * @param preference preference
     */
    protected void updateSummary(String value, EditTextPreference preference) {
        if (value == null || value.isEmpty()) {
            preference.setSummary(R.string.not_set);
        } else {
            preference.setSummary(value);
        }
    }

    /**
     * Reads fragment's {@link SharedPreferences} and updates preferences value.
     *
     * @param group preferences group
     */
    protected void refreshPreferences(PreferenceGroup group) {
        SharedPreferences preferences = getSharedPreferences();
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
