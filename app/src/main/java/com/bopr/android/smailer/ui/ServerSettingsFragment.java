package com.bopr.android.smailer.ui;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

import com.bopr.android.smailer.Notifications;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.Util;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Outgoing server settings activity's fragment.
 */
public class ServerSettingsFragment extends DefaultPreferenceFragment {

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
    }

    private void updateAccountPreference(String value) {
        if (isEmpty(value)) {
            updateSummary(R.string.pref_description_not_set, accountPreference, false);
        } else {
            updateSummary(value, accountPreference, EmailTextValidator.isValidValue(value));
        }
    }

    private void updatePasswordPreference(String value) {
        if (isEmpty(value)) {
            updateSummary(R.string.pref_description_not_set, passwordPreference, false);
        } else {
            updateSummary(R.string.pref_description_password_asterisk, passwordPreference, true);
        }
    }

    private void updateHostPreference(String value) {
        if (Util.isEmpty(value)) {
            updateSummary(R.string.pref_description_not_set, hostPreference, false);
        } else {
            updateSummary(value, hostPreference, true);
        }
    }

    private void updatePortPreference(String value) {
        if (Util.isEmpty(value)) {
            updateSummary(R.string.pref_description_not_set, portPreference, false);
        } else {
            updateSummary(value, portPreference, true);
        }
    }

}
