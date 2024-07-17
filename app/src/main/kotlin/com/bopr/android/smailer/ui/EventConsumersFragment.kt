package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.updateSummary

/**
 * Event consumers settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventConsumersFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_event_consumers)
    }

    override fun onStart() {
        super.onStart()

        updateEmailPreferenceView()
        updateTelegramPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_EMAIL_MESSENGER_ENABLED ->
                updateEmailPreferenceView()

            PREF_TELEGRAM_MESSENGER_ENABLED ->
                updateTelegramPreferenceView()
        }
    }

    private fun updateEmailPreferenceView() {
        requirePreference("email_settings").updateSummary(
            onOffText(settings.getBoolean(PREF_EMAIL_MESSENGER_ENABLED)),
        )
    }

    private fun updateTelegramPreferenceView() {
        requirePreference("telegram_settings").updateSummary(
            onOffText(settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED))
        )
    }

}