package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.util.SummaryStyle
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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            PREF_EMAIL_MESSENGER_ENABLED -> updateEmailPreference()

            PREF_TELEGRAM_MESSENGER_ENABLED -> updateTelegramPreference()
        }
    }

    private fun updateEmailPreference() {
        val enabled = settings.getBoolean(PREF_EMAIL_MESSENGER_ENABLED)
        requirePreference(PREF_EMAIL_SETTINGS).updateSummary(
            onOffText(enabled), if (enabled) SUMMARY_STYLE_DEFAULT else SUMMARY_STYLE_ACCENTED
        )
    }

    private fun updateTelegramPreference() {
        val enabled = settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)
        requirePreference(PREF_TELEGRAM_SETTINGS).updateSummary(
            onOffText(enabled), if (enabled) SUMMARY_STYLE_DEFAULT else SUMMARY_STYLE_ACCENTED
        )
    }

    companion object {

        private const val PREF_EMAIL_SETTINGS = "email_settings"
        private const val PREF_TELEGRAM_SETTINGS = "telegram_settings"
    }
}