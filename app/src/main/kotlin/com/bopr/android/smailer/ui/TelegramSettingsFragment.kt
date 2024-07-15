package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.external.Telegram
import com.bopr.android.smailer.external.TelegramException
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.showToast

/**
 * Event consumers settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramSettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_telegram_settings)

//        requirePreference(PREF_TELEGRAM_BOT_TOKEN).setOnPreferenceClickListener {
//            todo: create dedicated dialog
//            true
//        }

        requirePreference("sent_test_telegram_message").setOnPreferenceClickListener {
            onSendTestTelegramMessage()
            true
        }
    }

    override fun onStart() {
        super.onStart()

        updateTelegramPreferenceView()
        updateTelegramBotTokenPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_TELEGRAM_BOT_TOKEN ->
                updateTelegramBotTokenPreferenceView()

            PREF_TELEGRAM_MESSENGER_ENABLED ->
                updateTelegramPreferenceView()
        }
    }

    private fun onSendTestTelegramMessage() {
        Telegram(requireContext()).sendMessage(
            "This is a test message",
            onSuccess = {
                showToast(R.string.test_message_sent)
            },
            onError = { error ->
                if (error is TelegramException) {
                    when (error.code) {
                        TELEGRAM_REQUEST_FAILED,
                        TELEGRAM_BAD_RESPONSE -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.error_sending_test_message
                        )

                        TELEGRAM_NO_TOKEN -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.no_telegram_bot_token
                        )

                        TELEGRAM_INVALID_TOKEN -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.bad_telegram_bot_token
                        )

                        TELEGRAM_NO_CHAT -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.require_start_chat
                        )
                    }
                } else throw error
            }
        )
    }

    private fun updateTelegramPreferenceView() {
        requirePreference(PREF_TELEGRAM_MESSENGER_ENABLED).apply {
            setTitle(onOffText(settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)))
        }
    }

    private fun updateTelegramBotTokenPreferenceView() {
        requirePreference(PREF_TELEGRAM_BOT_TOKEN).apply {
            val token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
            if (token.isNullOrEmpty()) {
                updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
            } else {
                updateSummary(R.string.specified, SUMMARY_STYLE_DEFAULT)
            }
        }
    }
}