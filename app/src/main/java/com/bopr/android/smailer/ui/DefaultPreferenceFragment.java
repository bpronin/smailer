package com.bopr.android.smailer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import com.bopr.android.smailer.util.AndroidUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.bopr.android.smailer.Settings.PREFERENCES_STORAGE_NAME;

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
        preferenceManager.setSharedPreferencesName(PREFERENCES_STORAGE_NAME);
        sharedPreferences = preferenceManager.getSharedPreferences();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshPreferences(getPreferenceScreen());
    }

    /**
     * Returns application's shared preferences.
     */
    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    /**
     * Updates summary of {@link Preference}.
     *
     * @param value      value
     * @param preference preference
     */
    protected void updateSummary(String value, Preference preference, boolean valid) {
        preference.setSummary(AndroidUtil.validatedText(getActivity(), value, valid));
    }

    /**
     * Updates summary of {@link Preference}.
     *
     * @param valueResource value resource ID
     * @param preference    preference
     */
    protected void updateSummary(int valueResource, Preference preference, boolean valid) {
        updateSummary(getString(valueResource), preference, valid);
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
                } else if (preference instanceof MultiSelectListPreference) {
                    //noinspection unchecked
                    Set<String> set = value == null ? Collections.<String>emptySet() : (Set<String>) value;
                    ((MultiSelectListPreference) preference).setValues(set);
                }
            }
        }
    }

}
