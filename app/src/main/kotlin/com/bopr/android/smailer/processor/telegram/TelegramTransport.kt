package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.processor.EventProcessor
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.ui.EventConsumersActivity

/**
 * Telegram transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramTransport(context: Context) : EventProcessor(context) {

    private val settings = Settings(context)
    private val formatters = TelegramMessageFormatterFactory(context)
    private val notifications by lazyOf(NotificationsHelper(context))

    override fun process(
        event: Event,
        onSuccess: () -> Unit,
        onError: (error: Exception) -> Unit
    ) {
        val formatter = formatters.createFormatter(event.payload)

        TelegramSession(
            context = context,
            token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
        ).sendMessage(
            message = formatter.formatMessage(),
            onSuccess = onSuccess,
            onError = { error ->
                notifyError(error)
                onError(error)
            })
    }

    private fun notifyError(error: TelegramException) {
        val textRes = when (error.code) {
            TELEGRAM_REQUEST_FAILED,
            TELEGRAM_BAD_RESPONSE -> R.string.error_sending_test_message

            TELEGRAM_NO_TOKEN -> R.string.no_telegram_bot_token

            TELEGRAM_INVALID_TOKEN -> R.string.bad_telegram_bot_token

            TELEGRAM_NO_CHAT -> R.string.require_start_chat
        }

        notifications.notifyError(
            TELEGRAM_ERROR,
            context.getString(textRes),
            EventConsumersActivity::class
        )
    }

    companion object {

        private const val TELEGRAM_ERROR = 1004
    }

}
