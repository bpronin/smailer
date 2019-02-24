package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bopr.android.smailer.AuthorizationHelper;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.RemoteControlWorker;
import com.bopr.android.smailer.util.AndroidUtil;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import static com.bopr.android.smailer.GmailTransport.SCOPE_ALL;
import static com.bopr.android.smailer.Settings.KEY_PREF_DEVICE_ALIAS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL;
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Options settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class OptionsFragment extends BasePreferenceFragment {

    private AuthorizationHelper authorizator;
    private Preference accountPreference;
    private SettingsListener settingsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorizator = new AuthorizationHelper(this, SCOPE_ALL, KEY_PREF_REMOTE_CONTROL_ACCOUNT);

        settingsListener = new SettingsListener();
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

        // TODO: 24.02.2019 add help icon for remote control
        accountPreference = findPreference(KEY_PREF_REMOTE_CONTROL_ACCOUNT);
        accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                authorizator.selectAccount();
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateAccountPreference();
    }

    @Override
    public void onDestroy() {
        authorizator.dismiss();
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authorizator.onActivityResult(requestCode, resultCode, data);
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

    private void updateAccountPreference() {
        String value = settings.getString(KEY_PREF_REMOTE_CONTROL_ACCOUNT, "");
        if (isEmpty(value)) {
            /* cannot use null as "same as sender" due to gmail permission difference */
            updateSummary(accountPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(accountPreference, value, STYLE_DEFAULT);
        }
    }

    private class SettingsListener extends BaseSettingsListener {

        private SettingsListener() {
            super(requireContext());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case KEY_PREF_REMOTE_CONTROL_ACCOUNT:
                    updateAccountPreference();
                    break;
                case KEY_PREF_REMOTE_CONTROL:
                    RemoteControlWorker.enable(requireContext());
                    break;
            }

            super.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

}
