package com.bopr.android.smailer.ui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.widget.Toast;

import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.MailerProperties;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.Util;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_TEST_MAIL_SERVER;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Outgoing server settings activity's fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ServerFragment extends BasePreferenceFragment {

    private EditTextPreference accountPreference;
    private EditTextPreference passwordPreference;
    private EditTextPreference hostPreference;
    private EditTextPreference portPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_server);

        accountPreference = (EditTextPreference) findPreference(KEY_PREF_SENDER_ACCOUNT);
        accountPreference.getEditText().addTextChangedListener(new EmailTextValidator(accountPreference.getEditText()));
        accountPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateAccountPreference((String) value);
                return true;
            }
        });

        passwordPreference = (EditTextPreference) findPreference(KEY_PREF_SENDER_PASSWORD);
        passwordPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updatePasswordPreference((String) value);
                return true;
            }
        });

        hostPreference = (EditTextPreference) findPreference(KEY_PREF_EMAIL_HOST);
        hostPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateHostPreference((String) value);
                return true;
            }
        });

        portPreference = (EditTextPreference) findPreference(KEY_PREF_EMAIL_PORT);
        portPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updatePortPreference((String) value);
                return true;
            }
        });

        findPreference(KEY_PREF_TEST_MAIL_SERVER).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                checkSettings();
                return true;
            }
        });
    }

    private void updateAccountPreference(String value) {
        if (isEmpty(value)) {
            updateNotSpecifiedSummary(accountPreference);
        } else {
            updateSummary(value, accountPreference, EmailTextValidator.isValidValue(value));
        }
    }

    private void updatePasswordPreference(String value) {
        if (isEmpty(value)) {
            updateNotSpecifiedSummary(passwordPreference);
        } else {
            updateSummary(R.string.pref_description_password_asterisk, passwordPreference, true);
        }
    }

    private void updateHostPreference(String value) {
        if (Util.isEmpty(value)) {
            updateNotSpecifiedSummary(hostPreference);
        } else {
            updateSummary(value, hostPreference, true);
        }
    }

    private void updatePortPreference(String value) {
        if (Util.isEmpty(value)) {
            updateNotSpecifiedSummary(portPreference);
        } else {
            updateSummary(value, portPreference, true);
        }
    }

    protected void checkSettings() {
        new AsyncTask<Void, Void, Integer>() {

            public ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(getActivity(), null, getActivity().getString(R.string.server_testing), true);
            }

            @Override
            protected Integer doInBackground(Void... params) {
                MailTransport transport = new MailTransport();
                MailerProperties pp = new MailerProperties(getSharedPreferences());
                Cryptor cryptor = new Cryptor(getActivity());
                transport.init(pp.getUser(), cryptor.decrypt(pp.getPassword()), pp.getHost(), pp.getPort());
                return transport.checkConnection();
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                dialog.dismiss();

                int message;
                if (result == MailTransport.CHECK_RESULT_NOT_CONNECTED) {
                    message = R.string.notification_error_connect;
                } else if (result == MailTransport.CHECK_RESULT_AUTHENTICATION) {
                    message = R.string.notification_error_authentication;
                } else {
                    message = R.string.server_test_success;
                }

                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

}
