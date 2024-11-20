package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEGRAM
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_CHAT_ID
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_SENT_BY_TELEGRAM
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.ui.MainActivity
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
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (FLAG_SENT_BY_TELEGRAM in event.processFlags) return

        session?.run {
            log.debug("Sending").verb(event)

            sendMessage(
                oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID),
                message = formatters.createFormatter(event).formatMessage(),
                onSuccess = { chatId ->
                    log.debug("Sent")

                    settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }
                    event.processFlags += FLAG_SENT_BY_TELEGRAM
                    notifySendSuccess()
                    onSuccess()
                },
                onError = {
                    log.warn("Send failed", it)

                    event.processFlags -= FLAG_SENT_BY_TELEGRAM
                    notifySendError(it)
                    onError(it)
                })
        }
    }

    private fun notifySendSuccess() {
        if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS))
            notifications.notifyInfo(
                title = context.getString(R.string.telegram_successfully_send),
                target = MainActivity::class
            )
    }

    private fun notifySendError(error: TelegramException) {
        notifications.notifyError(
            NTF_TELEGRAM,
            context.getString(telegramErrorText(error)),
            TelegramSettingsActivity::class
        )
    }

    companion object {

        private val log = Logger("TelegramMessenger")
    }
}
