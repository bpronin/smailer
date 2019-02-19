package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.os.Bundle;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.TagFormatter;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_BLACKLIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_TEXT_BLACKLIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_TEXT_WHITELIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_WHITELIST;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.parseCommaSeparated;

/**
 * Conditions settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RulesFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_rules);

        Preference phoneBlacklistPreference = findPreference(KEY_PREF_FILTER_BLACKLIST);
        phoneBlacklistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), PhoneBlacklistActivity.class));
                return true;
            }
        });
        phoneBlacklistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.unacceptable_phone_numbers,
                        R.string.none), preference, true);
                return true;
            }
        });

        Preference phoneWhitelistPreference = findPreference(KEY_PREF_FILTER_WHITELIST);
        phoneWhitelistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), PhoneWhitelistActivity.class));
                return true;
            }
        });
        phoneWhitelistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.acceptable_phone_numbers,
                        R.string._any), preference, true);
                return true;
            }
        });

        Preference textBlacklistPreference = findPreference(KEY_PREF_FILTER_TEXT_BLACKLIST);
        textBlacklistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), TextBlacklistActivity.class));
                return true;
            }
        });
        textBlacklistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.unacceptable_words,
                        R.string.none), preference, true);
                return true;
            }
        });

        Preference textWhitelistPreference = findPreference(KEY_PREF_FILTER_TEXT_WHITELIST);
        textWhitelistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), TextWhitelistActivity.class));
                return true;
            }
        });
        textWhitelistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.acceptable_words,
                        R.string._any), preference, true);
                return true;
            }
        });

    }

    private String formatListAndSize(String value, int pattern, int zeroSizeText) {
        TagFormatter formatter = formatter(pattern, requireContext());
        int size = parseCommaSeparated(value).size();
        if (size > 0) {
            return formatter.put("size", String.valueOf(size)).format();
        } else {
            return formatter.putRes("size", zeroSizeText).format();
        }
    }

}
