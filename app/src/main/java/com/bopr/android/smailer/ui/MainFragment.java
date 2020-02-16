package com.bopr.android.smailer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;

import com.bopr.android.smailer.ContentObserverService;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GoogleAuthorizationHelper;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ResendWorker;

import static com.bopr.android.smailer.Database.registerDatabaseListener;
import static com.bopr.android.smailer.Database.unregisterDatabaseListener;
import static com.bopr.android.smailer.Settings.PREF_DEVICE_ALIAS;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_HISTORY;
import static com.bopr.android.smailer.Settings.PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.PREF_RULES;
import static com.bopr.android.smailer.Settings.PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.ui.BatteryOptimizationHelper.requireIgnoreBatteryOptimization;
import static com.bopr.android.smailer.util.AndroidUtil.deviceName;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.TextUtil.isNullOrBlank;
import static com.bopr.android.smailer.util.TextUtil.isNullOrEmpty;
import static com.bopr.android.smailer.util.TextUtil.isValidEmailAddressList;
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
    private Database database;
    private BroadcastReceiver databaseListener;
    private Preference historyPreference;
    private GoogleAuthorizationHelper authorizator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authorizator = new GoogleAuthorizationHelper(this, PREF_SENDER_ACCOUNT, GMAIL_SEND,
                DRIVE_APPDATA);

        database = new Database(requireContext());
        databaseListener = registerDatabaseListener(requireContext(), new DatabaseListener());

        permissionsHelper.checkAll();
        requireIgnoreBatteryOptimization(requireContext());
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        addPreferencesFromResource(R.xml.pref_main);

        recipientsPreference = requirePreference(PREF_RECIPIENTS_ADDRESS);
        accountPreference = requirePreference(PREF_SENDER_ACCOUNT);
        historyPreference = requirePreference(PREF_HISTORY);

        PreferenceClickListener preferenceClickListener = new PreferenceClickListener();
        recipientsPreference.setOnPreferenceClickListener(preferenceClickListener);
        accountPreference.setOnPreferenceClickListener(preferenceClickListener);
        historyPreference.setOnPreferenceClickListener(preferenceClickListener);
        requirePreference(PREF_RULES).setOnPreferenceClickListener(preferenceClickListener);

        requirePreference(PREF_EMAIL_LOCALE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateLocalePreferenceSummary((ListPreference) preference, (String) value);
                return true;
            }
        });

        EditTextPreference deviceNamePreference = requirePreference(PREF_DEVICE_ALIAS);
        deviceNamePreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {

            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setHint(deviceName());
            }
        });
        deviceNamePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateDeviceNamePreferenceSummary((EditTextPreference) preference, (String) value);
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        database.close();
        unregisterDatabaseListener(requireContext(), databaseListener);
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
        updateAccountPreferenceSummary();
        updateRecipientsPreferenceSummary();
        updateHistoryPreferenceSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_SENDER_ACCOUNT:
                updateAccountPreferenceSummary();
                break;
            case PREF_RECIPIENTS_ADDRESS:
                updateRecipientsPreferenceSummary();
                break;
            case PREF_EMAIL_TRIGGERS:
                ContentObserverService.Companion.enable(requireContext());
                break;
            case PREF_RESEND_UNSENT:
                ResendWorker.Companion.enable(requireContext());
                break;
        }

        super.onSharedPreferenceChanged(sharedPreferences, key);
    }

    private void updateAccountPreferenceSummary() {
        String value = settings.getString(PREF_SENDER_ACCOUNT, "");
        if (isNullOrBlank(value)) {
            updateSummary(accountPreference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED);
        } else if (!authorizator.isAccountExists(value)) {
            updateSummary(accountPreference, value, SUMMARY_STYLE_UNDERWIVED);
        } else {
            updateSummary(accountPreference, value, SUMMARY_STYLE_DEFAULT);
        }
    }

    private void updateRecipientsPreferenceSummary() {
        String value = settings.getString(PREF_RECIPIENTS_ADDRESS, "");
        if (isNullOrBlank(value)) {
            updateSummary(recipientsPreference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED);
        } else {
            updateSummary(recipientsPreference, value.replaceAll(",", ", "),
                    isValidEmailAddressList(value) ? SUMMARY_STYLE_DEFAULT : SUMMARY_STYLE_UNDERWIVED);
        }
    }

    private void updateHistoryPreferenceSummary() {
        long count = database.getUnreadEventsCount();
        if (count > 0) {
            String text = formatter(requireContext())
                    .pattern(R.string.count_new)
                    .put("count", valueOf(count))
                    .format();
            updateSummary(historyPreference, text, SUMMARY_STYLE_DEFAULT);
        } else {
            updateSummary(historyPreference, null, SUMMARY_STYLE_DEFAULT);
        }
    }

    private void updateLocalePreferenceSummary(ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        if (index < 0) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED);
        } else {
            updateSummary(preference, preference.getEntries()[index], SUMMARY_STYLE_DEFAULT);
        }
    }

    private void updateDeviceNamePreferenceSummary(EditTextPreference preference, String value) {
        if (isNullOrEmpty(value)) {
            updateSummary(preference, deviceName(), SUMMARY_STYLE_DEFAULT);
        } else {
            updateSummary(preference, value, SUMMARY_STYLE_DEFAULT);
        }
    }

    private class PreferenceClickListener implements OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case PREF_SENDER_ACCOUNT:
                    authorizator.selectAccount();
                    break;
                case PREF_RECIPIENTS_ADDRESS:
                    startActivity(new Intent(getContext(), RecipientsActivity.class));
                    break;
                case PREF_RULES:
                    startActivity(new Intent(getContext(), RulesActivity.class));
                    break;
                case PREF_HISTORY:
                    startActivity(new Intent(getContext(), HistoryActivity.class));
                    break;
            }
            return true;
        }
    }

    private class DatabaseListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateHistoryPreferenceSummary();
        }
    }
}
