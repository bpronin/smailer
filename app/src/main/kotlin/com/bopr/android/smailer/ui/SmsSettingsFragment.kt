package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.preference.Preference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.util.GeoLocation.Companion.requestGeoLocation
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.SummaryStyle.*
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.sendSmsMessage
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.updateSummary

/**
 * Email settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class SmsSettingsFragment : BasePreferenceFragment(R.xml.pref_sms_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreference(PREF_SMS_MESSENGER_RECIPIENTS).setOnChangeListener {
            it.apply {
                val items = settings.getStringList(key)
                if (items.isEmpty()) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else if (items.size == 1) {
                    updateSummary(items.first(), SUMMARY_STYLE_DEFAULT)
                } else {
                    updateSummary(
                        getString(R.string.addresses, items.size),
                        SUMMARY_STYLE_DEFAULT
                    )
                }
            }
        }

        requirePreference(PREF_SMS_MESSENGER_ENABLED).setOnChangeListener {
            it.apply {
                setTitle(onOffText(settings.getBoolean(key)))
            }
        }

        requirePreference(PREF_SEND_TEST_SMS).setOnClickListener {
            onSendTestMessage(it)
        }
    }

    private fun onSendTestMessage(preference: Preference) {
        val progress = PreferenceProgress(preference).apply { start() }
        val time = System.currentTimeMillis()

        requireContext().requestGeoLocation { location ->

            settings.getStringList(PREF_SMS_MESSENGER_RECIPIENTS).forEach {
                requireContext().sendSmsMessage(it, "Test message")
            }
            progress.stop()
        }
    }

    companion object {

        private const val PREF_SEND_TEST_SMS = "pref_send_test_sms"
    }
}
