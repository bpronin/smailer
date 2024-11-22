package com.bopr.android.smailer.ui

import android.content.SharedPreferences.*
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.Settings.Companion.sharedPreferencesName
import com.bopr.android.smailer.util.refreshView

/**
 * Base [PreferenceFragmentCompat] with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BasePreferenceFragment(@XmlRes private val layoutRes: Int) :
    PreferenceFragmentCompat() {

    private lateinit var settingsListener: OnSharedPreferenceChangeListener

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(layoutRes)
        preferenceManager.sharedPreferencesName = sharedPreferencesName
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onStart() {
        super.onStart()

        /* when settings have changed from somewhere else but this fragment, we need to
           forcibly refresh all views to reflect the changes */
        preferenceScreen.refreshView()

        settingsListener = settings.registerListener(::onSettingsChanged)
    }

    override fun onStop() {
        settings.unregisterListener(settingsListener)
        super.onStop()
    }

    open fun onSettingsChanged(settings: Settings, key: String) {
        findPreference<Preference>(key)?.refreshView()
    }

    companion object {
        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }
}

