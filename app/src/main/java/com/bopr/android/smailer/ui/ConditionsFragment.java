package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import com.bopr.android.smailer.R;

import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.Settings.*;
import static com.bopr.android.smailer.util.TagFormatter.formatFrom;
import static com.bopr.android.smailer.util.Util.parseCommaSeparated;

/**
 * Conditions settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ConditionsFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_conditions);

        Preference blacklistPreference = findPreference(KEY_PREF_FILTER_BLACKLIST);
        blacklistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), BlacklistActivity.class));
                return true;
            }
        });
        blacklistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatFrom(R.string.title_unacceptable_phone_numbers, getActivity())
                                .put("size", parseCommaSeparated((String) value).size())
                                .format(),
                        preference, true);
                return true;
            }
        });

        Preference whitelistPreference = findPreference(KEY_PREF_FILTER_WHITELIST);
        whitelistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), WhitelistActivity.class));
                return true;
            }
        });
        whitelistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatFrom(R.string.title_acceptable_phone_numbers, getActivity())
                                .put("size", parseCommaSeparated((String) value).size())
                                .format(),
                        preference, true);
                return true;
            }
        });

        findPreference(KEY_PREF_FILTER_USE_WHITE_LIST).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean useWhitelist = (Boolean) value;
                updateSummary(useWhitelist ? R.string.title_white_list_used : R.string.title_black_list_used, preference, true);
                return true;
            }

        });
    }

}
