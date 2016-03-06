package com.bopr.android.smailer.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.bopr.android.smailer.R;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.Preference.OnPreferenceChangeListener;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_CONTENT_CONTACT_NAME;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.util.PermissionUtil.isLocationPermissionDenied;
import static com.bopr.android.smailer.util.PermissionUtil.isReadContactPermissionDenied;
import static com.bopr.android.smailer.util.PermissionUtil.requestLocationPermission;
import static com.bopr.android.smailer.util.PermissionUtil.requestReadContactPermission;

/**
 * More settings activity's fragment.
 */
public class EmailContentSettingsFragment extends DefaultPreferenceFragment {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    private static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 102;

    private CheckBoxPreference contactNamePreference;
    private CheckBoxPreference locationPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_email_content);

        contactNamePreference = (CheckBoxPreference) findPreference(KEY_PREF_EMAIL_CONTENT_CONTACT_NAME);
        contactNamePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                checkReadContactPermission((Boolean) value);
                return true;
            }
        });

        locationPreference = (CheckBoxPreference) findPreference(KEY_PREF_EMAIL_CONTENT_LOCATION);
        locationPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                checkLocationPermission((Boolean) value);
                return true;
            }
        });
    }

    private void checkReadContactPermission(boolean enabled) {
        if (enabled && isReadContactPermissionDenied(getActivity())) {
            requestReadContactPermission(getActivity(), PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    private void checkLocationPermission(boolean enabled) {
        if (enabled && isLocationPermissionDenied(getActivity())) {
            requestLocationPermission(getActivity(), PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.message_contact_name_disabled_by_permission,
                        Toast.LENGTH_LONG).show();
                contactNamePreference.setChecked(false);
            }
        } else if (requestCode == PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            if (grantResults[0] != PERMISSION_GRANTED || grantResults[1] != PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.message_location_disabled_by_permission,
                        Toast.LENGTH_LONG).show();
                locationPreference.setChecked(false);
            }
        }
    }

}
