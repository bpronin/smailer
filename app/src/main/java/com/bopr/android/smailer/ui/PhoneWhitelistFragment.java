package com.bopr.android.smailer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.Settings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Phone number whitelist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PhoneWhitelistFragment extends PhoneFilterListFragment {

    private SettingsListener settingsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsListener = new SettingsListener();
        settings.registerOnSharedPreferenceChangeListener(settingsListener);
    }

    @Override
    public void onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        super.onDestroy();
    }

    @Override
    protected Set<String> getItemsList(PhoneEventFilter filter) {
        return filter.getPhoneWhitelist();
    }

    @Override
    protected void setItemsList(PhoneEventFilter filter, List<String> list) {
        filter.setPhoneWhitelist(new HashSet<>(list));
    }

    private class SettingsListener extends BaseSettingsListener {

        private SettingsListener() {
            super(requireContext());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Settings.PREF_FILTER_PHONE_WHITELIST)) {
                loadItems();
            }
            super.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

}
