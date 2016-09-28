package com.bopr.android.smailer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;

import com.bopr.android.smailer.PreferencesPermissionsChecker;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.bopr.android.smailer.Settings.PREFERENCES_STORAGE_NAME;

/**
 * Base {@link PreferenceFragment} with default behaviour.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
// TODO: 06.04.2016 there is a bug in MultiListPreference. when dialog is showing and device rotated when ok pressed then selected value are lost
public class BasePreferenceFragment extends PreferenceFragment {

    private static Logger log = LoggerFactory.getLogger("BasePreferenceFragment");

    private SharedPreferences sharedPreferences;
    private PreferencesPermissionsChecker permissionChecker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(PREFERENCES_STORAGE_NAME);
        sharedPreferences = preferenceManager.getSharedPreferences();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshPreferences();

        permissionChecker = new PreferencesPermissionsChecker(getActivity(), getSharedPreferences()) {

            @Override
            protected void onPermissionsDenied(Collection<String> permissions) {
                super.onPermissionsDenied(permissions);
                refreshPreferences();
            }
        };
        permissionChecker.checkAll();
    }

    @Override
    public void onStop() {
        permissionChecker.destroy();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.handleRequestResult(requestCode, permissions, grantResults);
    }

    /**
     * Returns application's shared preferences.
     */
    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    /**
     * Sets "not specified" summary of {@link Preference}.
     *
     * @param preference preference
     */
    protected void updateNotSpecifiedSummary(Preference preference) {
        preference.setSummary(AndroidUtil.validatedColoredText(getActivity(),
                getString(R.string.pref_description_not_set), false));
    }

    /**
     * Updates summary of {@link Preference}.
     *
     * @param value      value
     * @param preference preference
     */
    protected void updateSummary(String value, Preference preference, boolean valid) {
        preference.setSummary(AndroidUtil.validatedUnderlinedText(getActivity(), value, valid));
    }

    /**
     * Updates summary of {@link Preference}.
     *
     * @param valueResource value resource ID
     * @param preference    preference
     */
    protected void updateSummary(int valueResource, Preference preference, boolean valid) {
        updateSummary(getString(valueResource), preference, valid);
    }

    /**
     * Reads fragment's {@link SharedPreferences} and updates preferences value.
     */
    protected void refreshPreferences() {
        doRefreshPreferences(getPreferenceScreen());
    }

    @SuppressWarnings("unchecked")
    private void doRefreshPreferences(PreferenceGroup group) {
        Map<String, ?> map = getSharedPreferences().getAll();
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                doRefreshPreferences((PreferenceGroup) preference);
            } else {
                Object value = map.get(preference.getKey());
                Preference.OnPreferenceChangeListener listener = preference.getOnPreferenceChangeListener();
                if (listener != null) {
                    listener.onPreferenceChange(preference, value);
                }

                if (preference instanceof EditTextPreference) {
                    ((EditTextPreference) preference).setText((String) value);
                } else if (preference instanceof SwitchPreference) {
                    ((SwitchPreference) preference).setChecked(value != null && (boolean) value);
                } else if (preference instanceof CheckBoxPreference) {
                    ((CheckBoxPreference) preference).setChecked(value != null && (boolean) value);
                } else if (preference instanceof ListPreference) {
                    ((ListPreference) preference).setValue((String) value);
                } else if (preference instanceof MultiSelectListPreference) {
                    Set<String> set = value == null ? Collections.<String>emptySet() : (Set<String>) value;
                    ((MultiSelectListPreference) preference).setValues(set);
                } else if (preference.getClass() != Preference.class) {
                    log.error( "Unregistered preference class: " + preference.getClass());
                }
            }
        }
    }

}
