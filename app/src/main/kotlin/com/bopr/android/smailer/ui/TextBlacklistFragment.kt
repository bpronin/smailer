package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST

/**
 * Text blacklist activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TextBlacklistFragment : TextFilterListFragment() {

    private lateinit var settingsListener: SettingsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsListener = SettingsListener()
        settings.registerOnSharedPreferenceChangeListener(settingsListener)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
        super.onDestroy()
    }

    override fun getItemsList(filter: PhoneEventFilter): Set<String> {
        return filter.textBlacklist
    }

    override fun setItemsList(filter: PhoneEventFilter, list: Collection<String>) {
        filter.textBlacklist = list.toMutableSet()
    }

    private inner class SettingsListener : SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == PREF_FILTER_TEXT_BLACKLIST) {
                refreshItems()
            }
        }
    }
}