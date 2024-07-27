package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEGRAM
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_CHAT_ID
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.processor.EventProcessor
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.ui.TelegramSettingsActivity
import com.bopr.android.smailer.util.telegramErrorText

/**
 * Telegram transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramEventProcessor(context: Context) : EventProcessor(context) {

    private val settings = Settings(context)
    private val formatters = TelegramMessageFormatterFactory(context)
    private val notifications by lazyOf(NotificationsHelper(context))
    private lateinit var session: TelegramSession

    override fun isEnabled(): Boolean {
        return settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)
    }

    override fun prepare(): Boolean {
        session = TelegramSession(
            context = context, token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
        )
        return true
    }

    override fun process(event: Event) {
        val formatter = formatters.createFormatter(event.payload)

        session.sendMessage(oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID),
            message = formatter.formatMessage(),
            onSuccess = { chatId ->
                settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }
            },
            onError = { error ->
                notifications.notifyError(
                    NTF_TELEGRAM,
                    context.getString(telegramErrorText(error)),
                    TelegramSettingsActivity::class
                )
            })
    }

}
