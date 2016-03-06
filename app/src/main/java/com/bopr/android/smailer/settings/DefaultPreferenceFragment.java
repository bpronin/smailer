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

import java.util.Map;

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
     * Updates summary of {@link Preference}.
     *
     * @param value      value
     * @param preference preference
     */
    protected void updateSummary(String value, Preference preference) {
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
        Map<String, ?> map = preferences.getAll();
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                refreshPreferences((PreferenceGroup) preference);
            } else {
                Object value = map.get(preference.getKey());
                Preference.OnPreferenceChangeListener listener = preference.getOnPreferenceChangeListener();
                if (listener != null) {
                    listener.onPreferenceChange(preference, value);
                }

                if (preference instanceof EditTextPreference) {
                    ((EditTextPreference) preference).setText((String) value);
                } else if (preference instanceof SwitchPreference) {
                    ((SwitchPreference) preference).setChecked(value != null && (boolean) value);
                }
            }
        }
    }

}
