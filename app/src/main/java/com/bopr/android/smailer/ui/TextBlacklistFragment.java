package com.bopr.android.smailer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.PhoneEventFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_BLACKLIST;

/**
 * Text blacklist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TextBlacklistFragment extends TextFilterListFragment {

    private SettingsListener settingsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsListener = settings.registerChangeListener(new SettingsListener());
    }

    @Override
    public void onDestroy() {
        settings.unregisterChangeListener(settingsListener);
        super.onDestroy();
    }

    @NonNull
    @Override
    Set<String> getItemsList(@NonNull PhoneEventFilter filter) {
        return filter.getTextBlacklist();
    }

    @Override
    void setItemsList(@NonNull PhoneEventFilter filter, @NonNull List<String> list) {
        filter.setTextBlacklist(new HashSet<>(list));
    }

    private class SettingsListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(PREF_FILTER_TEXT_BLACKLIST)) {
                reloadItems();
            }

        }
    }

}
