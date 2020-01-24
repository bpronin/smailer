package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.bopr.android.smailer.GoogleAuthorizationHelper;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.RemoteControlWorker;

import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ENABLED;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_NOTIFICATIONS;
import static com.bopr.android.smailer.util.Util.isEmpty;
import static com.bopr.android.smailer.util.Util.requireNonNull;
import static com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM;

public class RemoteControlFragment extends BasePreferenceFragment {

    private GoogleAuthorizationHelper authorizator;
    private Preference accountPreference;
    private SettingsListener settingsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorizator = new GoogleAuthorizationHelper(this, PREF_REMOTE_CONTROL_ACCOUNT, MAIL_GOOGLE_COM);
        settingsListener = new SettingsListener();
        settings.registerOnSharedPreferenceChangeListener(settingsListener);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_remote);

        // TODO: 24.02.2019 add help icon for remote control
        accountPreference = requirePreference(PREF_REMOTE_CONTROL_ACCOUNT);
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
        updatePreferences();
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

    private void updateAccountPreference() {
        String value = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT, "");
        if (isEmpty(value)) {
            /* cannot use null as "same as sender" due to gmail permission difference */
            updateSummary(accountPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(accountPreference, value, STYLE_DEFAULT);
        }
    }

    private void updatePreferences() {
        boolean enabled = settings.getBoolean(PREF_REMOTE_CONTROL_ENABLED, false);
        accountPreference.setEnabled(enabled);
        requirePreference(PREF_REMOTE_CONTROL_NOTIFICATIONS).setEnabled(enabled);
        requirePreference(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS).setEnabled(enabled);
    }

    @NonNull
    private Preference requirePreference(String name) {
        return requireNonNull(findPreference(name));
    }

    private class SettingsListener extends BaseSettingsListener {

        private SettingsListener() {
            super(requireContext());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PREF_REMOTE_CONTROL_ACCOUNT:
                    updateAccountPreference();
                    break;
                case PREF_REMOTE_CONTROL_ENABLED:
                    updatePreferences();
                    RemoteControlWorker.enable(requireContext());
                    break;
            }

            super.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

}
