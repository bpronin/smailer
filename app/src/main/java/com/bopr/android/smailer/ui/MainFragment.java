package com.bopr.android.smailer.ui;

import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.widget.ListView;

import com.bopr.android.smailer.AuthorizationHelper;
import com.bopr.android.smailer.ContentObserverService;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GmailTransport;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ResendWorker;
import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.validator.EmailListTextValidator;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_HISTORY;
import static com.bopr.android.smailer.Settings.KEY_PREF_MORE;
import static com.bopr.android.smailer.Settings.KEY_PREF_OUTGOING_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_RULES;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.util.Util.isEmpty;
import static java.lang.String.valueOf;

/**
 * Main settings fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainFragment extends BasePreferenceFragment {

    private Preference recipientsPreference;
    private Preference serverPreference;
    private OnSharedPreferenceChangeListener settingsListener;
    private Preference.OnPreferenceClickListener preferenceClickListener;
    private BackupManager backupManager;
    private Database database;
    private BroadcastReceiver databaseListener;
    private Preference historyPreference;
    private boolean asListView;
    private AuthorizationHelper authorizator;

    public MainFragment() {
        setAsListView(false);
        setPreferenceClickListener(new PreferenceClickListener());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backupManager = new BackupManager(getContext());
        authorizator = new AuthorizationHelper(this, GmailTransport.SCOPES);

        settingsListener = new SettingsListener();
        settings.registerOnSharedPreferenceChangeListener(settingsListener);

        database = new Database(getContext());
        databaseListener = new DatabaseListener();
        database.registerListener(databaseListener);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        addPreferencesFromResource(R.xml.pref_main);

        recipientsPreference = findPreference(KEY_PREF_RECIPIENTS_ADDRESS);
        serverPreference = findPreference(KEY_PREF_OUTGOING_SERVER);
        historyPreference = findPreference(KEY_PREF_HISTORY);

        recipientsPreference.setOnPreferenceClickListener(preferenceClickListener);
        serverPreference.setOnPreferenceClickListener(preferenceClickListener);
        historyPreference.setOnPreferenceClickListener(preferenceClickListener);
        findPreference(KEY_PREF_MORE).setOnPreferenceClickListener(preferenceClickListener);
        findPreference(KEY_PREF_RULES).setOnPreferenceClickListener(preferenceClickListener);
    }

    @Override
    public void onDestroy() {
        authorizator.dismiss();
        database.unregisterListener(databaseListener);
        database.close();
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (asListView) {
            @SuppressWarnings("ConstantConditions")
            ListView listView = getView().findViewById(android.R.id.list);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setItemChecked(listView.getSelectedItemPosition(), true);
        }
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

    void setAsListView(boolean asListView) {
        this.asListView = asListView;
    }

    void setPreferenceClickListener(Preference.OnPreferenceClickListener listener) {
        this.preferenceClickListener = listener;
    }

    private void updateAccountPreference() {
        if (!asListView) {
            String value = settings.getString(KEY_PREF_SENDER_ACCOUNT, "");
            if (isEmpty(value)) {
                updateSummary(serverPreference, getString(R.string.not_specified), STYLE_ACCENTED);
            } else {
                updateSummary(serverPreference, value, EmailTextValidator.isValidValue(value) ? STYLE_DEFAULT : STYLE_UNDERWIVED);
            }
        }
    }

    private void updateRecipientsPreference() {
        if (!asListView) {
            String value = settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, null);
            if (isEmpty(value)) {
                updateSummary(recipientsPreference, getString(R.string.not_specified), STYLE_ACCENTED);
            } else {
                updateSummary(recipientsPreference, value.replaceAll(",", ", "), EmailListTextValidator.isValidValue(value) ? STYLE_DEFAULT : STYLE_UNDERWIVED);
            }
        }
    }

    private void updateHistoryPreference() {
        long count = database.getUnreadEventsCount();
        if (count > 0) {
            String text = TagFormatter.formatter(R.string.count_new, requireContext())
                    .put("count", valueOf(count))
                    .format();
            updateSummary(historyPreference, text, STYLE_DEFAULT);
        } else {
            updateSummary(historyPreference, null, STYLE_DEFAULT);
        }
    }

    private class PreferenceClickListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_PREF_OUTGOING_SERVER:
                    authorizator.selectAccount();
                    break;
                case KEY_PREF_RECIPIENTS_ADDRESS:
                    startActivity(new Intent(getContext(), RecipientsActivity.class));
                    break;
                case KEY_PREF_MORE:
                    startActivity(new Intent(getContext(), MoreActivity.class));
                    break;
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

    private class SettingsListener implements OnSharedPreferenceChangeListener {

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

            backupManager.dataChanged();
        }
    }

    private class DatabaseListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateHistoryPreference();
        }
    }
}
