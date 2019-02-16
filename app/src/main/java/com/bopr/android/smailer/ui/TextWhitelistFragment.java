package com.bopr.android.smailer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.Settings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Text whitelist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class TextWhitelistFragment extends TextFilterListFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Settings.KEY_PREF_FILTER_TEXT_WHITELIST)) {
                    loadItems();
                }
            }
        };
        settings.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onDestroy();
    }

    @Override
    Set<String> getItemsList(PhoneEventFilter filter) {
        return filter.getTextWhitelist();
    }

    @Override
    void setItemsList(PhoneEventFilter filter, List<String> list) {
        filter.setTextWhitelist(new HashSet<>(list));
    }
}
