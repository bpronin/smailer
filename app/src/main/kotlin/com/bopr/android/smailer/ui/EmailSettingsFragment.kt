package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.util.deviceName

/**
 * Email message settings settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EmailSettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_email)

        findPreference<EditTextPreference>(PREF_DEVICE_ALIAS)!!.setOnBindEditTextListener { editText ->
            editText.hint = deviceName()
        }
    }

    override fun onStart() {
        super.onStart()

        updateDeviceNamePreferenceView()
        updateLocalePreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_DEVICE_ALIAS ->
                updateDeviceNamePreferenceView()
            PREF_EMAIL_LOCALE ->
                updateLocalePreferenceView()
        }
    }

    private fun updateLocalePreferenceView() {
        val preference: ListPreference = findPreference(PREF_EMAIL_LOCALE)!!

        val index = preference.findIndexOfValue(settings.emailLocale)
        if (index < 0) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            updateSummary(preference, preference.entries[index], SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateDeviceNamePreferenceView() {
        updateSummary(requirePreference(PREF_DEVICE_ALIAS), settings.deviceAlias,
                SUMMARY_STYLE_DEFAULT)
    }

}
