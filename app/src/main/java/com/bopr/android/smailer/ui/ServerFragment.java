package com.bopr.android.smailer.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.widget.Toast;
import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.MailerProperties;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.Util;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import javax.mail.MessagingException;

import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.MailTransport.*;
import static com.bopr.android.smailer.Settings.*;
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
//                checkSettings();
                sendTestMessage();
                return true;
            }
        });
    }

    private void updateAccountPreference(String value) {
        if (isEmpty(value)) {
            updateNotSpecifiedSummary(accountPreference);
        } else {
            updateSummary(value, accountPreference, EmailTextValidator.isValidValue(value));
            if (!TextUtils.equals(accountPreference.getText(), value)) {
                updateHostByAccount(value);
            }
        }
    }

    private void updatePasswordPreference(String value) {
        if (isEmpty(value)) {
            updateNotSpecifiedSummary(passwordPreference);
        } else {
            updateSummary(R.string.title_password_asterisks, passwordPreference, true);
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

    private void updateHostByAccount(String account) {
        String[] ss = account.split("@");
        if (ss.length > 1) {
            String host = "smtp." + ss[1];
            hostPreference.setText(host);
            updateHostPreference(host);
        }
    }

    protected void sendTestMessage() {
        new SendTestMailTask(getActivity(), getSharedPreferences()).execute();
    }

    private static class SendTestMailTask extends LongAsyncTask<Void, Void, Integer> {

        private SharedPreferences preferences;

        private SendTestMailTask(Activity context, SharedPreferences preferences) {
            super(context);
            this.preferences = preferences;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            MailTransport transport = new MailTransport();
            Cryptor cryptor = new Cryptor(getContext());
            MailerProperties pp = new MailerProperties(preferences);
            transport.init(pp.getUser(), cryptor.decrypt(pp.getPassword()), pp.getHost(), pp.getPort());
            int result = transport.checkConnection();
            if (result == CHECK_RESULT_OK) {
                try {
                    transport.send("[" + getContext().getString(R.string.app_name) + "] TEST",
                            "This is the test message", pp.getUser(), pp.getRecipients());
                } catch (MessagingException e) {
                    result = CHECK_RESULT_NOT_CONNECTED;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            int message;
            if (result == CHECK_RESULT_NOT_CONNECTED) {
                message = R.string.notification_error_connect;
            } else if (result == CHECK_RESULT_AUTHENTICATION) {
                message = R.string.notification_error_authentication;
            } else {
                message = R.string.message_server_test_success;
            }

            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

}
