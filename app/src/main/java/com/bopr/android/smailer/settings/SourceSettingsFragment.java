package com.bopr.android.smailer.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.widget.Toast;

import com.bopr.android.smailer.R;

import static android.preference.Preference.OnPreferenceChangeListener;
//import static com.bopr.android.smailer.settings.Settings.KEY_PREF_IN_CALL_ENABLED;
//import static com.bopr.android.smailer.settings.Settings.KEY_PREF_IN_SMS_ENABLED;
//import static com.bopr.android.smailer.settings.Settings.KEY_PREF_MISSED_CALL_ENABLED;
//import static com.bopr.android.smailer.settings.Settings.KEY_PREF_OUT_CALL_ENABLED;

/**
 * More settings activity's fragment.
 */
public class SourceSettingsFragment extends DefaultPreferenceFragment {

    private CheckBoxPreference inSmsPreference;
    private CheckBoxPreference inCallPreference;
    private CheckBoxPreference outCallPreference;
    private CheckBoxPreference missedCallPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_source_content);

//        inSmsPreference = (CheckBoxPreference) findPreference(KEY_PREF_IN_SMS_ENABLED);
//        inSmsPreference.setOnPreferenceChangeListener(
//                new OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object newValue) {
//                        return checkAtLeastOneChecked();
//                    }
//                }
//        );
//
//        inCallPreference = (CheckBoxPreference) findPreference(KEY_PREF_IN_CALL_ENABLED);
//        inCallPreference.setOnPreferenceChangeListener(
//                new OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object newValue) {
//                        return checkAtLeastOneChecked();
//                    }
//                }
//        );
//
//        outCallPreference = (CheckBoxPreference) findPreference(KEY_PREF_OUT_CALL_ENABLED);
//        outCallPreference.setOnPreferenceChangeListener(
//                new OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object newValue) {
//                        return checkAtLeastOneChecked();
//                    }
//                }
//        );
//
//        missedCallPreference = (CheckBoxPreference) findPreference(KEY_PREF_MISSED_CALL_ENABLED);
//        missedCallPreference.setOnPreferenceChangeListener(
//                new OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object newValue) {
//                        return checkAtLeastOneChecked();
//                    }
//                }
//        );
    }

    private boolean checkAtLeastOneChecked() {
        boolean anyChecked = inSmsPreference.isChecked() || inCallPreference.isChecked()
                || outCallPreference.isChecked() || missedCallPreference.isChecked();
        if (!anyChecked){
            Toast.makeText(getActivity(), R.string.message_at_least_one_source_checked,
                    Toast.LENGTH_SHORT).show();
        }
        return anyChecked;
    }

}
