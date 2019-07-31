package com.bopr.android.smailer.ui;

import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;

import static com.bopr.android.smailer.Settings.KEY_PREF_DEVICE_ALIAS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Options settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@Deprecated
public class OptionsFragment extends BasePreferenceFragment {

    private BaseSettingsListener settingsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsListener = new BaseSettingsListener(requireContext());
        settings.registerOnSharedPreferenceChangeListener(settingsListener);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_options);

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

    @Override
    public void onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        super.onDestroy();
    }

    private void updateLocalePreference(ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        if (index < 0) {
            updateSummary(preference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            CharSequence cs = preference.getEntries()[index];
            updateSummary(preference, cs.toString(), STYLE_DEFAULT);
        }
    }

    private void updateAlasPreference(EditTextPreference preference, String value) {
        if (isEmpty(value)) {
            updateSummary(preference, AndroidUtil.getDeviceName(), STYLE_DEFAULT);
        } else {
            updateSummary(preference, value, STYLE_DEFAULT);
        }
    }

}
