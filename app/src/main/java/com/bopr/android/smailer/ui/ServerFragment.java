package com.bopr.android.smailer.ui;

import android.os.Bundle;
import android.text.TextUtils;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ui.preference.EmailPreference;
import com.bopr.android.smailer.util.Util;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
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

        accountPreference = (EmailPreference) findPreference(KEY_PREF_SENDER_ACCOUNT);
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
    }

    private void updateAccountPreference(String value) {
        if (isEmpty(value)) {
            updateSummary(accountPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(accountPreference, value, EmailTextValidator.isValidValue(value) ? STYLE_DEFAULT : STYLE_UNDERLINED);

            if (!TextUtils.equals(accountPreference.getText(), value)) {
                updateHostByAccount(value);
            }
        }
    }

    private void updatePasswordPreference(String value) {
        if (isEmpty(value)) {
            updateSummary(passwordPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(passwordPreference, getString(R.string.password_asterisks), STYLE_DEFAULT);
        }
    }

    private void updateHostPreference(String value) {
        if (Util.isEmpty(value)) {
            updateSummary(hostPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(hostPreference, value, STYLE_DEFAULT);
        }
    }

    private void updatePortPreference(String value) {
        if (Util.isEmpty(value)) {
            updateSummary(portPreference, getString(R.string.not_specified), STYLE_ACCENTED);
        } else {
            updateSummary(portPreference, value, STYLE_DEFAULT);
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

}
