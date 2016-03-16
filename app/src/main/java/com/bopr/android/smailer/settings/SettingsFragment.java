package com.bopr.android.smailer.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.StringUtil;
import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.validator.EmailListTextValidator;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.Preference.OnPreferenceChangeListener;
import static android.preference.PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES;
import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.DEFAULT_SOURCES;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_SOURCE;
import static com.bopr.android.smailer.Settings.KEY_PREF_OUTGOING_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Permissions.isSmsPermissionDenied;
import static com.bopr.android.smailer.Permissions.requestSmsPermission;

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
        loadDefaultPreferences();
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

        recipientsPreference = (EditTextPreference) findPreference(KEY_PREF_RECIPIENT_EMAIL_ADDRESS);
        recipientsPreference.getEditText().addTextChangedListener(new EmailListTextValidator(recipientsPreference.getEditText()));
        recipientsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateRecipientsPreference((String) value);
                return true;
            }
        });

        findPreference(KEY_PREF_OUTGOING_SERVER).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ServerSettingsActivity.class));
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateServerPreference();
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

    private void updateAccountPreference(String value) {
        updateSummary(value, accountPreference);
    }

    private void updatePasswordPreference(String value) {
        if (value != null && !value.isEmpty()) {
            passwordPreference.setSummary(R.string.pref_description_password_asterisk);
        } else {
            passwordPreference.setSummary(getNotSpecifiedSummary());
        }
    }

    private void updateRecipientsPreference(String value) {
        updateSummary(value, recipientsPreference);
    }

    private void updateServerPreference() {
        SharedPreferences preferences = getSharedPreferences();
        String host = preferences.getString(KEY_PREF_EMAIL_HOST, "");
        String port = preferences.getString(KEY_PREF_EMAIL_PORT, "");
        String value = null;
        if (!StringUtil.isEmpty(host) || !StringUtil.isEmpty(port)) {
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
                                           @NonNull int[] grantResults
    ) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.message_service_disabled_by_permission,
                        Toast.LENGTH_LONG).show();

                enabledPreference.setChecked(false);
            }
        }
    }

    /**
     * Sets default preferences values.
     * We are using multi-activity preferences so some values
     * won't be read at startup until we start activity that owns these prefs.
     */
    private void loadDefaultPreferences() {
        final SharedPreferences defaultValueSp = getActivity().getSharedPreferences(
                KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);

        if (!defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false)) {
            getSharedPreferences()
                    .edit()
                    .putBoolean(KEY_PREF_SERVICE_ENABLED, true)
                    .putString(KEY_PREF_EMAIL_HOST, DEFAULT_HOST)
                    .putString(KEY_PREF_EMAIL_PORT, DEFAULT_PORT)
                    .putStringSet(KEY_PREF_EMAIL_SOURCE, DEFAULT_SOURCES)
                    .putStringSet(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
                    .apply();

            defaultValueSp
                    .edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true)
                    .apply();
        }

    }

    private void showAboutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.action_about)
                .setMessage(TagFormatter.from("{label} {version}", getResources())
                        .putResource("label", R.string.about_dialog_title_version)
                        .put("version", Settings.getReleaseVersion(getActivity()))
                        .format())
                .create();

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        // todo: add "rate us on the play store"
        // todo: add "open source libs"
    }

}
