package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.NotificationData
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEGRAM
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_CHAT_ID
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.SENT_BY_TELEGRAM
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.ui.TelegramSettingsActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.telegramErrorText

/**
 * Telegram messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class TelegramMessenger(private val context: Context) : Messenger(context, SENT_BY_TELEGRAM) {

    private val settings = context.settings
    private val formatters = TelegramFormatterFactory(context)
    private var session: TelegramSession? = null

    override suspend fun prepare(): Boolean {
        if (settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)) {
            session = TelegramSession(context, settings.getString(PREF_TELEGRAM_BOT_TOKEN))
            log.debug("Session created")
            return true
        }
        return false
    }

    override suspend fun doSend(
        event: Event, onSuccess: () -> Unit, onError: (Throwable) -> Unit
    ) {
        log.debug("Sending")
        session?.apply {
            val formatter = formatters.createFormatter(event)
            sendMessage(
                oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID),
                message = formatter.formatMessage(),
                messageFormat = formatter.format,
                onSuccess = { chatId ->
                    log.debug("Sent")
                    settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }
                    onSuccess()
                },
                onError = {
                    log.warn("Send failed", it)
                    onError(it)
                })
        }
    }

    override fun getSuccessNotification() = NotificationData(
        title = context.getString(R.string.telegram_successfully_send),
        target = MainActivity::class
    )

    override fun getErrorNotification(error: Throwable) = NotificationData(
        id = NTF_TELEGRAM,
        text = context.getString(telegramErrorText(error as TelegramException)),
        target = TelegramSettingsActivity::class
    )

    companion object {
        private val log = Logger("TelegramMessenger")
    }
}
