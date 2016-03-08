package com.bopr.android.smailer.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.bopr.android.smailer.R;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.settings.Settings.*;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_OUTGOING_SERVER;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.util.PermissionUtil.isSmsPermissionDenied;
import static com.bopr.android.smailer.util.PermissionUtil.requestSmsPermission;

/**
 * Main settings fragment.
 */
public class SettingsFragment extends DefaultPreferenceFragment {

    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 100;

    private SwitchPreference enabledPreference;
    private EditTextPreference recipientsPreference;
    private EditTextPreference accountPreference;
    private EditTextPreference passwordPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        enabledPreference = (SwitchPreference) findPreference(KEY_PREF_SERVICE_ENABLED);
        enabledPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean enabled = value != null && (boolean) value;
                checkSmsPermission(enabled);
                return true;
            }
        });

        accountPreference = (EditTextPreference) findPreference(KEY_PREF_SENDER_ACCOUNT);
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

        recipientsPreference = (EditTextPreference) findPreference(KEY_PREF_RECIPIENT_EMAIL_ADDRESS);
        recipientsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateRecipientsPreference((String) value);
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateServerPreference();
    }

    private void updateAccountPreference(String value) {
        updateSummary(value, accountPreference);
    }

    private void updatePasswordPreference(String value) {
        passwordPreference.setSummary(value != null && !value.isEmpty()
                ? R.string.pref_description_password_asterisk : R.string.pref_description_not_set);
    }

    private void updateRecipientsPreference(String value) {
        updateSummary(value, recipientsPreference);
    }

    private void updateServerPreference() {
        SharedPreferences preferences = getSharedPreferences();
        String host = preferences.getString(KEY_PREF_EMAIL_HOST, "");
        String port = preferences.getString(KEY_PREF_EMAIL_PORT, "");
        String value = null;
        if (!TextUtils.isEmpty(host) || !TextUtils.isEmpty(port)) {
            value = host + ":" + port;
        }
        updateSummary(value, findPreference(KEY_PREF_OUTGOING_SERVER));
    }

    private void checkSmsPermission(boolean enabled) {
        if (enabled && isSmsPermissionDenied(getActivity())) {
            requestSmsPermission(getActivity(), PERMISSIONS_REQUEST_RECEIVE_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.message_service_disabled_by_permission,
                        Toast.LENGTH_LONG).show();

                enabledPreference.setChecked(false);
            }
        }
    }

}
