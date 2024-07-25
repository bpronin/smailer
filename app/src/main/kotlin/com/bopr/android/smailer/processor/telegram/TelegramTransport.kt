package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_CHAT_ID
import com.bopr.android.smailer.processor.EventProcessor
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.ui.EventConsumersActivity
import com.bopr.android.smailer.util.telegramErrorText

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
            oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID),
            message = formatter.formatMessage(),
            onSuccess = { chatId ->
                settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }
                onSuccess()
            },
            onError = { error ->
                notifyError(error)
                onError(error)
            })
    }

    private fun notifyError(error: TelegramException) {
        notifications.notifyError(
            TELEGRAM_ERROR,
            context.getString(telegramErrorText(error)),
            EventConsumersActivity::class
        )
    }

    companion object {

        private const val TELEGRAM_ERROR = 1004
    }

}
