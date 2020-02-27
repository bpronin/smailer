package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.util.AndroidUtil.deviceName

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
        val preference = findPreference<ListPreference>(PREF_EMAIL_LOCALE)!!
        val value = settings.getString(preference.key)

        val index = preference.findIndexOfValue(value)
        if (index < 0) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            updateSummary(preference, preference.entries[index], SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateDeviceNamePreferenceView() {
        val preference = requirePreference(PREF_DEVICE_ALIAS)
        val alias = settings.getString(preference.key)

        if (alias == null) {
            updateSummary(preference, deviceName(), SUMMARY_STYLE_DEFAULT)
        } else {
            updateSummary(preference, alias, SUMMARY_STYLE_DEFAULT)
        }
    }

}
