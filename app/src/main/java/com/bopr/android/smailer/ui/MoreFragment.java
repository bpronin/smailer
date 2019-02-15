package com.bopr.android.smailer.ui;

import android.os.Bundle;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.Util;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import static com.bopr.android.smailer.Settings.KEY_PREF_DEVICE_ALIAS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;

/**
 * More settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MoreFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_more);

        findPreference(KEY_PREF_EMAIL_LOCALE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateLocalePreference((ListPreference) preference, (String) value);
                return true;
            }
        });

        findPreference(KEY_PREF_DEVICE_ALIAS).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateAlasPreference((EditTextPreference) preference, (String) value);
                return true;
            }
        });
    }

    private void updateLocalePreference(ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        if (index < 0) {
            updateNotSpecifiedSummary(preference);
        } else {
            CharSequence cs = preference.getEntries()[index];
            updateSummary(cs.toString(), preference, true);
        }
    }

    private void updateAlasPreference(EditTextPreference preference, String value) {
        if (Util.isEmpty(value)) {
            updateSummary(AndroidUtil.getDeviceName(), preference, true);
        } else {
            updateSummary(value, preference, true);
        }
    }

}
