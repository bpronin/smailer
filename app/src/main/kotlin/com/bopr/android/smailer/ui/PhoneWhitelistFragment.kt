package com.bopr.android.smailer.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.Settings

/**
 * Phone number whitelist activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneWhitelistFragment : PhoneFilterListFragment() {

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
        return filter.phoneWhitelist
    }

    override fun setItemsList(filter: PhoneEventFilter, list: List<String>) {
        filter.phoneWhitelist = list.toMutableSet()
    }

    private inner class SettingsListener(context: Context) : BaseSettingsListener(context) {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == Settings.PREF_FILTER_PHONE_WHITELIST) {
                loadItems()
            }
            super.onSharedPreferenceChanged(sharedPreferences, key)
        }
    }
}