package com.bopr.android.smailer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;

import com.bopr.android.smailer.ContentObserverService;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GoogleAuthorizationHelper;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ResendWorker;
import com.bopr.android.smailer.util.AndroidUtil;

import static com.bopr.android.smailer.Settings.KEY_PREF_DEVICE_ALIAS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_HISTORY;
import static com.bopr.android.smailer.Settings.KEY_PREF_OUTGOING_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_RULES;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.util.AndroidUtil.isValidEmailAddressList;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.isEmpty;
import static com.google.api.services.drive.DriveScopes.DRIVE_APPDATA;
import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;
import static java.lang.String.valueOf;

/**
 * Main settings fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainFragment extends BasePreferenceFragment {

    private Preference recipientsPreference;
    private Preference accountPreference;
    private SettingsListener settingsListener;
    private Database database;
    private BroadcastReceiver databaseListener;
    private Preference historyPreference;
    private GoogleAuthorizationHelper authorizator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authorizator = new GoogleAuthorizationHelper(this, KEY_PREF_SENDER_ACCOUNT, GMAIL_SEND,
                DRIVE_APPDATA);

        settingsListener = new SettingsListener();
        settings.registerOnSharedPreferenceChangeListener(settingsListener);

        database = new Database(getContext());
        databaseListener = database.registerListener(new DatabaseListener());
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        addPreferencesFromResource(R.xml.pref_main);

        recipientsPreference = findPreference(KEY_PREF_RECIPIENTS_ADDRESS);
        accountPreference = findPreference(KEY_PREF_OUTGOING_SERVER);
        historyPreference = findPreference(KEY_PREF_HISTORY);

        PreferenceClickListener preferenceClickListener = new PreferenceClickListener();
        recipientsPreference.setOnPreferenceClickListener(preferenceClickListener);
        accountPreference.setOnPreferenceClickListener(preferenceClickListener);
        historyPreference.setOnPreferenceClickListener(preferenceClickListener);
//        findPreference(KEY_PREF_OPTIONS).setOnPreferenceClickListener(preferenceClickListener);
        findPreference(KEY_PREF_RULES).setOnPreferenceClickListener(preferenceClickListener);

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
        authorizator.dismiss();
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        database.unregisterListener(databaseListener);
        database.close();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authorizator.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateAccountPreference();
        updateRecipientsPreference();
        updateHistoryPreference();
    }

    private void updateAccountPreference() {
        String value = settings.getString(KEY_PREF_SENDER_ACCOUNT, "");
        if (isEmpty(value)) {
            updateSummary(accountPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(accountPreference, value, STYLE_DEFAULT);
        }
    }

    private void updateRecipientsPreference() {
        String value = settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, null);
        if (isEmpty(value)) {
            updateSummary(recipientsPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(recipientsPreference, value.replaceAll(",", ", "),
                    isValidEmailAddressList(value) ? STYLE_DEFAULT : STYLE_UNDERWIVED);
        }
    }

    private void updateHistoryPreference() {
        long count = database.getUnreadEventsCount();
        if (count > 0) {
            String text = formatter(requireContext())
                    .pattern(R.string.count_new)
                    .put("count", valueOf(count))
                    .format();
            updateSummary(historyPreference, text, STYLE_DEFAULT);
        } else {
            updateSummary(historyPreference, null, STYLE_DEFAULT);
        }
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

    private class PreferenceClickListener implements OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_PREF_OUTGOING_SERVER:
                    authorizator.selectAccount();
                    break;
                case KEY_PREF_RECIPIENTS_ADDRESS:
                    startActivity(new Intent(getContext(), RecipientsActivity.class));
                    break;
//                case KEY_PREF_OPTIONS:
//                    startActivity(new Intent(getContext(), OptionsActivity.class));
//                    break;
                case KEY_PREF_RULES:
                    startActivity(new Intent(getContext(), RulesActivity.class));
                    break;
                case KEY_PREF_HISTORY:
                    startActivity(new Intent(getContext(), HistoryActivity.class));
                    break;
            }
            return true;
        }
    }

    private class SettingsListener extends BaseSettingsListener {

        private SettingsListener() {
            super(requireContext());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case KEY_PREF_SENDER_ACCOUNT:
                    updateAccountPreference();
                    break;
                case KEY_PREF_RECIPIENTS_ADDRESS:
                    updateRecipientsPreference();
                    break;
                case KEY_PREF_EMAIL_TRIGGERS:
                    ContentObserverService.enable(requireContext());
                    break;
                case KEY_PREF_RESEND_UNSENT:
                    ResendWorker.enable(requireContext());
                    break;
            }

            super.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

    private class DatabaseListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateHistoryPreference();
        }
    }
}
