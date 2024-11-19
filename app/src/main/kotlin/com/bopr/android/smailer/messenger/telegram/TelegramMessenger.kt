package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEGRAM
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_CHAT_ID
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.messenger.Message
import com.bopr.android.smailer.messenger.Message.Companion.FLAG_SENT_BY_TELEGRAM
import com.bopr.android.smailer.messenger.Messenger
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
    private var session: TelegramSession? = null


    override fun initialize(): Boolean {
        if (settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)) {
            session = TelegramSession(context, settings.getString(PREF_TELEGRAM_BOT_TOKEN))

            log.debug("Initialized")

            return true
        }

        return false
    }

    override fun sendMessage(
        message: Message,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (FLAG_SENT_BY_TELEGRAM in message.processedFlags) return

        session?.run {
            log.debug("Sending").verb(message)

            sendMessage(
                oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID),
                message = formatters.createFormatter(message.payload).formatMessage(),
                onSuccess = { chatId ->
                    log.debug("Sent")

                    message.processedFlags += FLAG_SENT_BY_TELEGRAM
                    settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }
                    onSuccess()
                },
                onError = {
                    log.warn("Send failed", it)

                    message.processedFlags -= FLAG_SENT_BY_TELEGRAM
                    notifyError(it)
                    onError(it)
                })
        }
    }

    private fun notifyError(exception: TelegramException) {
        notifications.notifyError(
            NTF_TELEGRAM,
            context.getString(telegramErrorText(exception)),
            TelegramSettingsActivity::class
        )
    }

    companion object {

        private val log = Logger("TelegramMessenger")
    }
}
