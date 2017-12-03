package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.Util;

import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.Settings.KEY_PREF_DEVICE_ALIAS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_BLACK_LIST;

/**
 * Message filters settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class FiltersFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_filters);

        Preference blacklistPreference = findPreference(KEY_PREF_FILTER_BLACK_LIST);
        blacklistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), BlacklistActivity.class));
                return true;
            }
        });

//        findPreference(KEY_PREF_EMAIL_LOCALE).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object value) {
//                updateLocalePreference((ListPreference) preference, (String) value);
//                return true;
//            }
//        });
    }

//    private void updateLocalePreference(ListPreference preference, String value) {
//        int index = preference.findIndexOfValue(value);
//        if (index < 0) {
//            updateNotSpecifiedSummary(preference);
//        } else {
//            CharSequence cs = preference.getEntries()[index];
//            updateSummary(cs.toString(), preference, true);
//        }
//    }

}
