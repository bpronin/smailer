package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEGRAM
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_CHAT_ID
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.messenger.Message
import com.bopr.android.smailer.ui.TelegramSettingsActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.telegramErrorText

/**
 * Telegram messenger.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramMessenger(private val context: Context) : Messenger {

    private val settings = Settings(context)
    private val formatters = TelegramFormatterFactory(context)
    private val notifications by lazyOf(NotificationsHelper(context))
    private lateinit var session: TelegramSession

    override fun isEnabled(): Boolean {
        return settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)
    }

    override fun initialize(): Boolean {
        log.debug("Preparing")

        session = TelegramSession(
            context = context,
            token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
        )

        return true
    }

    override fun sendMessage(
        message: Message,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        log.debug("Sending")

        session.sendMessage(
            oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID),
            message = formatters.createFormatter(message.payload).formatMessage(),
            onSuccess = { chatId ->
                log.debug("Sent")

                settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }
                onSuccess()
            },
            onError = { error ->
                log.warn("Send failed", error)

                notifications.notifyError(
                    NTF_TELEGRAM,
                    context.getString(telegramErrorText(error)),
                    TelegramSettingsActivity::class
                )
                onError(error)
            })
    }

    companion object {

        private val log = Logger("TelegramMessenger")
    }
}
