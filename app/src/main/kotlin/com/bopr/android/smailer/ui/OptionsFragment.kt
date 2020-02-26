package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.preference.Preference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.UiUtil.showToast

/**
 * Options fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class OptionsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_options)

        requirePreference("reset_dialogs").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            onResetDialogs()
            true
        }
    }

    private fun onResetDialogs() {
        Settings(requireContext())
                .edit()
                .remove(BatteryOptimizationHelper.BATTERY_OPTIMIZATION_DIALOG_TAG)
                .apply()
        showToast(R.string.operation_complete)
    }

}