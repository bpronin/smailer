package com.bopr.android.smailer.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.Settings

/**
 * Text blacklist activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TextBlacklistFragment : TextFilterListFragment() {

    private lateinit var settingsListener: SettingsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsListener = SettingsListener(requireContext())
        settings.registerOnSharedPreferenceChangeListener(settingsListener)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
        super.onDestroy()
    }

    override fun getItemsList(filter: PhoneEventFilter): Set<String> {
        return filter.textBlacklist
    }

    override fun setItemsList(filter: PhoneEventFilter, list: List<String>) {
        filter.textBlacklist = list.toMutableSet()
    }

    private inner class SettingsListener(context: Context) : BaseSettingsListener(context) {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == Settings.PREF_FILTER_TEXT_BLACKLIST) {
                loadItems()
            }
            super.onSharedPreferenceChanged(sharedPreferences, key)
        }
    }
}