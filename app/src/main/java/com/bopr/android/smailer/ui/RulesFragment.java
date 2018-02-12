package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
public class RulesFragment extends BasePreferenceFragment {

    private OnSharedPreferenceChangeListener preferenceChangeListener;
    private Preference phoneBlacklistPreference;
    private Preference phoneWhitelistPreference;
    private Preference textBlacklistPreference;
    private Preference textWhitelistPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_conditions);

        phoneBlacklistPreference = findPreference(KEY_PREF_FILTER_BLACKLIST);
        phoneBlacklistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), NumberBlacklistActivity.class));
                return true;
            }
        });
        phoneBlacklistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.title_unacceptable_phone_numbers,
                        R.string.title_none), preference, true);
                return true;
            }
        });

        phoneWhitelistPreference = findPreference(KEY_PREF_FILTER_WHITELIST);
        phoneWhitelistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), NumberWhitelistActivity.class));
                return true;
            }
        });
        phoneWhitelistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.title_acceptable_phone_numbers,
                        R.string.title_any), preference, true);
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

        textBlacklistPreference = findPreference(KEY_PREF_FILTER_TEXT_BLACKLIST);
        textBlacklistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), TextBlacklistActivity.class));
                return true;
            }
        });
        textBlacklistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.title_unacceptable_text,
                        R.string.title_none), preference, true);
                return true;
            }
        });

        textWhitelistPreference = findPreference(KEY_PREF_FILTER_TEXT_WHITELIST);
        textWhitelistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), TextWhitelistActivity.class));
                return true;
            }
        });
        textWhitelistPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateSummary(formatListAndSize((String) value, R.string.title_acceptable_text,
                        R.string.title_any), preference, true);
                return true;
            }
        });

        findPreference(KEY_PREF_FILTER_TEXT_USE_WHITE_LIST).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean useWhitelist = (Boolean) value;
                updateSummary(useWhitelist ? R.string.title_white_list_used : R.string.title_black_list_used, preference, true);
                return true;
            }

        });

        preferenceChangeListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateControls();
            }
        };
        getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateControls();
    }

    @Override
    public void onDestroy() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onDestroy();
    }

    private String formatListAndSize(String value, int pattern, int zeroSizeText) {
        int size = parseCommaSeparated(value).size();
        if (size > 0) {
            return formatFrom(pattern, getActivity()).put("size", size).format();
        } else {
            return formatFrom(pattern, getActivity()).putResource("size", zeroSizeText).format();
        }
    }

    private void updateControls() {
        boolean phoneUseWhiteList = getSharedPreferences().getBoolean(KEY_PREF_FILTER_USE_WHITE_LIST, true);
        phoneWhitelistPreference.setEnabled(phoneUseWhiteList);
        phoneBlacklistPreference.setEnabled(!phoneUseWhiteList);

        boolean textUseWhiteList = getSharedPreferences().getBoolean(KEY_PREF_FILTER_TEXT_USE_WHITE_LIST, true);
        textWhitelistPreference.setEnabled(textUseWhiteList);
        textBlacklistPreference.setEnabled(!textUseWhiteList);
    }

}