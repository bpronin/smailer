package com.bopr.android.smailer.ui

import android.os.Bundle
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.processor.telegram.TelegramSession
import com.bopr.android.smailer.util.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.showToast
import com.bopr.android.smailer.util.updateSummary

/**
 * Event consumers settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramSettingsFragment : BasePreferenceFragment(R.xml.pref_telegram_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreference(PREF_TELEGRAM_MESSENGER_ENABLED).setOnChangeListener {
            it.apply {
                setTitle(onOffText(settings.getBoolean(it.key)))
            }
        }

        requirePreference(PREF_TELEGRAM_BOT_TOKEN).setOnChangeListener {
            it.apply {
                val token = settings.getString(it.key)
                if (token.isNullOrEmpty()) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(R.string.specified, SUMMARY_STYLE_DEFAULT)
                }
            }
        }

        requirePreference(SEND_TEST_TELEGRAM_MESSAGE).setOnClickListener {
            onSendTestTelegramMessage()
        }
    }

    private fun onSendTestTelegramMessage() {
        TelegramSession(
            context = requireContext(),
            token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
        ).sendMessage(
            message = "This is a test message",
            onSuccess = {
                showToast(R.string.test_message_sent)
            },
            onError = { error ->
                showInfoDialog(
                    titleResId = R.string.test_message_failed,
                    messageResId = when (error.code) {
                        TELEGRAM_REQUEST_FAILED,
                        TELEGRAM_BAD_RESPONSE ->
                            R.string.error_sending_test_message

                        TELEGRAM_NO_TOKEN ->
                            R.string.no_telegram_bot_token

                        TELEGRAM_INVALID_TOKEN ->
                            R.string.bad_telegram_bot_token

                        TELEGRAM_NO_CHAT ->
                            R.string.require_start_chat
                    }
                )
            }
        )
    }

    companion object {

        private const val SEND_TEST_TELEGRAM_MESSAGE = "send_test_telegram_message"
    }
}