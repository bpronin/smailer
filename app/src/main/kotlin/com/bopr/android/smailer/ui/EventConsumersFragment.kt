package com.bopr.android.smailer.ui

import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.updateSummary


/**
 * Event consumers settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventConsumersFragment : BasePreferenceFragment(R.xml.pref_event_consumers) {

    override fun onStart() {
        super.onStart()

        updateEmailPreference()
        updateTelegramPreference()
        updateSmsPreference()
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        super.onSettingsChanged(settings, key)

        when (key) {
            PREF_EMAIL_MESSENGER_ENABLED -> updateEmailPreference()

            PREF_TELEGRAM_MESSENGER_ENABLED -> updateTelegramPreference()

            PREF_SMS_MESSENGER_RECIPIENTS -> updateSmsPreference()
        }
    }

    private fun updateSmsPreference() {
        updatePreference(PREF_SMS_MESSENGER_ENABLED, PREF_SMS_SETTINGS)
    }

    private fun updateEmailPreference() {
        updatePreference(PREF_EMAIL_MESSENGER_ENABLED,PREF_EMAIL_SETTINGS)
    }

    private fun updateTelegramPreference() {
        updatePreference(PREF_TELEGRAM_MESSENGER_ENABLED,PREF_TELEGRAM_SETTINGS)
    }

    private fun updatePreference(masterKey: String, key: String) {
        val enabled = settings.getBoolean(masterKey)
        requirePreference(key).updateSummary(
            onOffText(enabled), if (enabled) SUMMARY_STYLE_DEFAULT else SUMMARY_STYLE_ACCENTED
        )
    }

    companion object {

        private const val PREF_EMAIL_SETTINGS = "email_settings"
        private const val PREF_TELEGRAM_SETTINGS = "telegram_settings"
        private const val PREF_SMS_SETTINGS = "sms_settings"
    }
}