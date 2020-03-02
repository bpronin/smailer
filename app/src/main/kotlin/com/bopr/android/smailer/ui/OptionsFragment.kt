package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.preference.Preference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.BATTERY_OPTIMIZATION_DIALOG_TAG
import com.bopr.android.smailer.util.showToast

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
        Settings(requireContext()).update {
            remove(BATTERY_OPTIMIZATION_DIALOG_TAG)
        }
        showToast(R.string.operation_complete)
    }

}