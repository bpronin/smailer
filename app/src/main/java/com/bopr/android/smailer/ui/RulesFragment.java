package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.TagFormatter.TagPattern;

import java.util.Set;

import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_WHITELIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_WHITELIST;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.TextUtil.commaSplit;

/**
 * Conditions settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RulesFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        addPreferencesFromResource(R.xml.pref_rules);

        requirePreference(PREF_EMAIL_TRIGGERS).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (value == null || ((Set) value).isEmpty()) {
                    updateSummary(preference, getString(R.string.no_triggers_specified), SUMMARY_STYLE_ACCENTED);
                } else {
                    updateSummary(preference, getString(R.string.events_causing_sending_mail), SUMMARY_STYLE_DEFAULT);
                }
                return true;
            }
        });

        Preference phoneBlacklistPreference = requirePreference(PREF_FILTER_PHONE_BLACKLIST);
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
                updateSummary(preference, formatListAndSize((String) value, R.string.unacceptable_phone_numbers,
                        R.string._none), SUMMARY_STYLE_DEFAULT);
                return true;
            }
        });

        Preference phoneWhitelistPreference = requirePreference(PREF_FILTER_PHONE_WHITELIST);
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
                updateSummary(preference, formatListAndSize((String) value, R.string.acceptable_phone_numbers,
                        R.string._any), SUMMARY_STYLE_DEFAULT);
                return true;
            }
        });

        Preference textBlacklistPreference = requirePreference(PREF_FILTER_TEXT_BLACKLIST);
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
                updateSummary(preference, formatListAndSize((String) value, R.string.unacceptable_words,
                        R.string._none), SUMMARY_STYLE_DEFAULT);
                return true;
            }
        });

        Preference textWhitelistPreference = requirePreference(PREF_FILTER_TEXT_WHITELIST);
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
                updateSummary(preference, formatListAndSize((String) value, R.string.acceptable_words,
                        R.string._any), SUMMARY_STYLE_DEFAULT);
                return true;
            }
        });

    }

    private String formatListAndSize(String value, int patternRes, int emptyTextRes) {
        TagPattern pattern = formatter(requireContext()).pattern(patternRes);
        int size = commaSplit(value).size();
        if (size > 0) {
            pattern.put("size", String.valueOf(size));
        } else {
            pattern.put("size", emptyTextRes);
        }
        return pattern.format();
    }

}
