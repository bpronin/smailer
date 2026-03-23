package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.ui.InfoDialog.Companion.showInfoDialog
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.sendSmsMessage
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.updateSummary
import kotlinx.coroutines.launch

/**
 * SMS messenger settings fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class SmsSettingsFragment : BasePreferenceFragment(R.xml.pref_sms_settings) {

    private val testSettingsProgress by lazy {
        PreferenceProgress(requirePreference(PREF_SEND_TEST_SMS))
    }

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
            onSendTestMessage()
        }
    }

    private fun onSendTestMessage() {
        if (testSettingsProgress.running) return

        testSettingsProgress.start()
        lifecycleScope.launch {
//            val location = requireContext().getGeoLocation()
            val recipients = settings.getStringList(PREF_SMS_MESSENGER_RECIPIENTS)
            try {
                recipients.forEach {
                    requireContext().sendSmsMessage(it, "Test message")
                }
            } catch (_: Exception) {
                showInfoDialog(R.string.test_message_failed, R.string.location_request_failed)
            } finally {
                testSettingsProgress.stop()
            }
        }
    }

    companion object {

        private const val PREF_SEND_TEST_SMS = "send_test_sms"
    }
}
