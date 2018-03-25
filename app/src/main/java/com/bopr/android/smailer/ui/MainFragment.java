package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.bopr.android.smailer.OutgoingSmsService;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ResendService;
import com.bopr.android.smailer.util.validator.EmailListTextValidator;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import static com.bopr.android.smailer.Settings.*;
import static com.bopr.android.smailer.util.Util.anyIsEmpty;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Main settings fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainFragment extends BasePreferenceFragment {

    private Preference recipientsPreference;
    private Preference serverPreference;
    private OnSharedPreferenceChangeListener preferenceChangeListener;
    private Preference.OnPreferenceClickListener preferenceClickListener;

    public MainFragment() {
        setPreferenceClickListener(new PreferenceClickListener());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadDefaultPreferences(getActivity());

        addPreferencesFromResource(R.xml.pref_main);
        setHasOptionsMenu(true);

        recipientsPreference = findPreference(KEY_PREF_RECIPIENTS_ADDRESS);
        serverPreference = findPreference(KEY_PREF_OUTGOING_SERVER);

        recipientsPreference.setOnPreferenceClickListener(preferenceClickListener);
        serverPreference.setOnPreferenceClickListener(preferenceClickListener);
        findPreference(KEY_PREF_MORE).setOnPreferenceClickListener(preferenceClickListener);
        findPreference(KEY_PREF_RULES).setOnPreferenceClickListener(preferenceClickListener);
        findPreference(KEY_PREF_LOG).setOnPreferenceClickListener(preferenceClickListener);

        preferenceChangeListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateServerPreference();
                updateRecipientsPreference();
                OutgoingSmsService.toggleService(getActivity());
                ResendService.toggleService(getActivity());
            }
        };
        getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateServerPreference();
        updateRecipientsPreference();
        OutgoingSmsService.toggleService(getActivity());
        ResendService.toggleService(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            showAboutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setPreferenceClickListener(Preference.OnPreferenceClickListener listener) {
        this.preferenceClickListener = listener;
    }

    private void updateServerPreference() {
        SharedPreferences preferences = getSharedPreferences();
        String sender = preferences.getString(KEY_PREF_SENDER_ACCOUNT, "");
        String host = preferences.getString(KEY_PREF_EMAIL_HOST, "");
        String port = preferences.getString(KEY_PREF_EMAIL_PORT, "");

        if (anyIsEmpty(sender, host, port)) {
            updateNotSpecifiedSummary(serverPreference);
        } else {
            updateSummary(sender, serverPreference, EmailTextValidator.isValidValue(sender));
        }
    }

    private void updateRecipientsPreference() {
        String value = getSharedPreferences().getString(KEY_PREF_RECIPIENTS_ADDRESS, null);
        if (isEmpty(value)) {
            updateNotSpecifiedSummary(recipientsPreference);
        } else {
            updateSummary(value.replaceAll(",", ", "), recipientsPreference, EmailListTextValidator.isValidValue(value));
        }
    }

    public void showAboutDialog() {
        new AboutDialogFragment().showDialog((FragmentActivity) getActivity());
    }

    private class PreferenceClickListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_PREF_OUTGOING_SERVER:
                    startActivity(new Intent(getActivity(), ServerActivity.class));
                    break;
                case KEY_PREF_RECIPIENTS_ADDRESS:
                    startActivity(new Intent(getActivity(), RecipientsActivity.class));
                    break;
                case KEY_PREF_MORE:
                    startActivity(new Intent(getActivity(), MoreActivity.class));
                    break;
                case KEY_PREF_RULES:
                    startActivity(new Intent(getActivity(), RulesActivity.class));
                    break;
                case KEY_PREF_LOG:
                    startActivity(new Intent(getActivity(), LogActivity.class));
                    break;
            }
            return true;
        }
    }
}
