package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.preference.Preference
import com.bopr.android.smailer.CallProcessor
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.BATTERY_OPTIMIZATION_DIALOG_TAG
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.runLongTask
import com.bopr.android.smailer.util.showToast
import java.lang.System.currentTimeMillis

/**
 * Options fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class OptionsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_options)

        requirePreference("reset_dialogs").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                onResetDialogs()
                true
            }

        requirePreference("sent_test_email").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                onSendTestEmail(it)
                true
            }
    }

    private fun onResetDialogs() {
        Settings(requireContext()).update {
            remove(BATTERY_OPTIMIZATION_DIALOG_TAG)
        }
        showToast(R.string.operation_complete)
    }

    private fun onSendTestEmail(preference: Preference) {
        preference.runLongTask(
            onPerform = {
                CallProcessor(requireContext()).process(
                    PhoneEvent(
                        phone = getString(R.string.app_name),
                        isIncoming = true,
                        startTime = currentTimeMillis(),
                        endTime = currentTimeMillis(),
                        text = "Sample message",
                        acceptor = deviceName()
                    )
                )
            },
            onComplete = { _, _ ->
                /* NOTE: if we live the page while processing context becomes null */
                context?.showToast(R.string.operation_complete)
            })
    }

}