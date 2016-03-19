package com.bopr.android.smailer.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

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
        if (!valid) {
            Spannable summary = new SpannableString(value);
            WavyUnderlineSpan span = new WavyUnderlineSpan(ContextCompat.getColor(getActivity(), R.color.errorForeground));
            summary.setSpan(span, 0, summary.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preference.setSummary(summary);
        } else {
            preference.setSummary(value);
        }
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
