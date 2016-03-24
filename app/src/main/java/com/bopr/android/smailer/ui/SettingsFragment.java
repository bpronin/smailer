package com.bopr.android.smailer.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bopr.android.smailer.PermissionsChecker;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.validator.EmailListTextValidator;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import java.util.HashSet;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.preference.Preference.OnPreferenceChangeListener;
import static android.preference.PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES;
import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_LOCALE;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_OUTGOING_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Main settings fragment.
 */
public class SettingsFragment extends DefaultPreferenceFragment {

    private Preference recipientsPreference;
    private EditTextPreference accountPreference;
    private EditTextPreference passwordPreference;
    private MultiSelectListPreference contentPreference;
    private MultiSelectListPreference triggersPreference;
    private ListPreference localePreference;
    private PermissionsChecker outCallsPermissionsChecker;
    private PermissionsChecker inSmsPermissionsChecker;
    private PermissionsChecker inCallsPermissionsChecker;
    private PermissionsChecker contactsPermissionsChecker;
    private PermissionsChecker locationPermissionsChecker;
    private Preference serverPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadDefaultPreferences();
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

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

        recipientsPreference = findPreference(KEY_PREF_RECIPIENT_EMAIL_ADDRESS);
        recipientsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), RecipientsSettingsActivity.class));
                return true;
            }
        });

        triggersPreference = (MultiSelectListPreference) findPreference(KEY_PREF_EMAIL_TRIGGERS);
        triggersPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            @SuppressWarnings("unchecked")
            public boolean onPreferenceChange(Preference preference, Object value) {
                outCallsPermissionsChecker.check(value);
                inSmsPermissionsChecker.check(value);
                inCallsPermissionsChecker.check(value);
                return true;
            }
        });

        contentPreference = (MultiSelectListPreference) findPreference(KEY_PREF_EMAIL_CONTENT);
        contentPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            @SuppressWarnings("unchecked")
            public boolean onPreferenceChange(Preference preference, Object value) {
                contactsPermissionsChecker.check(value);
                locationPermissionsChecker.check(value);
                return true;
            }
        });

        localePreference = (ListPreference) findPreference(KEY_PREF_EMAIL_LOCALE);
        localePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateLocalePreference((String) value);
                return true;
            }
        });

        serverPreference = findPreference(KEY_PREF_OUTGOING_SERVER);
        serverPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ServerSettingsActivity.class));
                return true;
            }
        });

        outCallsPermissionsChecker = createOutCallsPermissionChecker();
        inSmsPermissionsChecker = createInSmsPermissionChecker();
        inCallsPermissionsChecker = createInCallsPermissionChecker();
        contactsPermissionsChecker = createContactsPermissionChecker();
        locationPermissionsChecker = createLocationPermissionChecker();
    }

    private PermissionsChecker createContactsPermissionChecker() {
        PermissionsChecker checker = new PermissionsChecker<Set<String>>(getActivity()) {

            @Override
            protected boolean isPermissionRequired(Set<String> value) {
                return value != null && value.contains(VAL_PREF_EMAIL_CONTENT_CONTACT);
            }

            @Override
            protected void onPermissionsDenied() {
                Set<String> values = new HashSet<>(contentPreference.getValues());
                values.remove(VAL_PREF_EMAIL_CONTENT_CONTACT);
                triggersPreference.setValues(values);
            }
        };
        checker.setPermissions(READ_CONTACTS);
        checker.setDenyMessage(R.string.message_permission_denied_read_contacts);
        checker.setRationaleMessage(R.string.message_permission_rationale_read_contacts);

        return checker;
    }

    private PermissionsChecker createLocationPermissionChecker() {
        PermissionsChecker checker = new PermissionsChecker<Set<String>>(getActivity()) {

            @Override
            protected boolean isPermissionRequired(Set<String> value) {
                return value != null && value.contains(VAL_PREF_EMAIL_CONTENT_LOCATION);
            }

            @Override
            protected void onPermissionsDenied() {
                Set<String> values = new HashSet<>(contentPreference.getValues());
                values.remove(VAL_PREF_EMAIL_CONTENT_LOCATION);
                contentPreference.setValues(values);
            }
        };
        checker.setPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION);
        checker.setDenyMessage(R.string.message_permission_denied_location);
        checker.setRationaleMessage(R.string.message_permission_rationale_location);

        return checker;
    }

    private PermissionsChecker createInSmsPermissionChecker() {
        PermissionsChecker checker = new PermissionsChecker<Set<String>>(getActivity()) {

            @Override
            protected boolean isPermissionRequired(Set<String> value) {
                return value != null && value.contains(VAL_PREF_TRIGGER_IN_SMS);
            }

            @Override
            protected void onPermissionsDenied() {
                Set<String> values = new HashSet<>(triggersPreference.getValues());
                values.remove(VAL_PREF_TRIGGER_IN_SMS);
                triggersPreference.setValues(values);
            }
        };
        checker.setPermissions(RECEIVE_SMS);
        checker.setDenyMessage(R.string.message_permission_denied_receive_sms);
        checker.setRationaleMessage(R.string.message_permission_rationale_receive_sms);

        return checker;
    }

    private PermissionsChecker createInCallsPermissionChecker() {
        PermissionsChecker checker = new PermissionsChecker<Set<String>>(getActivity()) {

            @Override
            protected boolean isPermissionRequired(Set<String> value) {
                return value != null && (value.contains(VAL_PREF_TRIGGER_IN_CALLS)
                        || value.contains(VAL_PREF_TRIGGER_MISSED_CALLS));
            }

            @Override
            protected void onPermissionsDenied() {
                Set<String> values = new HashSet<>(triggersPreference.getValues());
                values.remove(VAL_PREF_TRIGGER_IN_CALLS);
                values.remove(VAL_PREF_TRIGGER_MISSED_CALLS);
                triggersPreference.setValues(values);
            }
        };
        checker.setPermissions(READ_PHONE_STATE);
        checker.setDenyMessage(R.string.message_permission_denied_phone_state);
        checker.setRationaleMessage(R.string.message_permission_rationale_phone_state);

        return checker;
    }

    private PermissionsChecker createOutCallsPermissionChecker() {
        PermissionsChecker checker = new PermissionsChecker<Set<String>>(getActivity()) {

            @Override
            protected boolean isPermissionRequired(Set<String> value) {
                return value != null && value.contains(VAL_PREF_TRIGGER_OUT_CALLS);
            }

            @Override
            protected void onPermissionsDenied() {
                Set<String> values = new HashSet<>(triggersPreference.getValues());
                values.remove(VAL_PREF_TRIGGER_OUT_CALLS);
                triggersPreference.setValues(values);
            }
        };
        checker.setPermissions(READ_PHONE_STATE, PROCESS_OUTGOING_CALLS);
        checker.setDenyMessage(R.string.message_permission_denied_outgoing_call);
        checker.setRationaleMessage(R.string.message_permission_rationale_outgoing_call);

        return checker;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateServerPreference();
        updateRecipientsPreference();
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
        } else if (item.getItemId() == R.id.action_log) {
            startActivity(new Intent(getActivity(), LogActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        outCallsPermissionsChecker.onPermissionsRequestResult(requestCode, grantResults);
        inSmsPermissionsChecker.onPermissionsRequestResult(requestCode, grantResults);
        inCallsPermissionsChecker.onPermissionsRequestResult(requestCode, grantResults);
        contactsPermissionsChecker.onPermissionsRequestResult(requestCode, grantResults);
        locationPermissionsChecker.onPermissionsRequestResult(requestCode, grantResults);
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

    private void updateRecipientsPreference() {
        SharedPreferences preferences = getSharedPreferences();
        String value = preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, null);
        if (isEmpty(value)) {
            updateSummary(R.string.pref_description_not_set, recipientsPreference, false);
        } else {
            updateSummary(value, recipientsPreference, EmailListTextValidator.isValidValue(value));
        }
    }

    private void updateServerPreference() {
        SharedPreferences preferences = getSharedPreferences();
        String host = preferences.getString(KEY_PREF_EMAIL_HOST, "");
        String port = preferences.getString(KEY_PREF_EMAIL_PORT, "");

        if (isEmpty(host) && isEmpty(port)) {
            updateSummary(R.string.pref_description_not_set, serverPreference, false);
        } else {
            String value = host + ":" + port;
            if (isEmpty(host) || isEmpty(port)) {
                updateSummary(value, serverPreference, false);
            } else {
                updateSummary(value, serverPreference, true);
            }
        }
    }

    private void updateLocalePreference(String value) {
        int index = localePreference.findIndexOfValue(value);
        if (index < 0) {
            updateSummary(R.string.pref_description_not_set, localePreference, false);
        }else {
            CharSequence cs = localePreference.getEntries()[index];
            updateSummary(cs.toString(), localePreference, true);
        }
    }

    /**
     * Sets default preferences values.
     * We are using multi-activity preferences so some values
     * won't be read at startup until we start activity that owns these preferences.
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
                    .putStringSet(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
                    .putStringSet(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
                    .putString(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE)
                    .apply();

            defaultValueSp
                    .edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true)
                    .apply();
        }

    }

    public void showAboutDialog() {
        FragmentManager fm = ((SettingsActivity) getActivity()).getSupportFragmentManager();
        new AboutDialogFragment().show(fm, "about_dialog");
    }

}
