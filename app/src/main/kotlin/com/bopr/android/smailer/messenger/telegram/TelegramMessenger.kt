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
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.ui.TelegramSettingsActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.getLocalizedText

/**
 * Telegram messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class TelegramMessenger(private val context: Context) : Messenger(context, SENT_BY_TELEGRAM) {

    private val settings = context.settings
    private val formatters = TelegramFormatterFactory(context)
    private lateinit var client: TelegramClient

    override val isEnabled get() = settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)

    override suspend fun doInitialize() {
        val token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
        if (token.isNullOrEmpty()) {
            log.warn("No token")
            throw TelegramException(TELEGRAM_NO_TOKEN, "No token")
        }
        client = TelegramClient(token)
        log.debug("Initialized")
    }

    override suspend fun doSend(event: Event) {
        log.debug("Sending")

        val chatId = client.send(
            message = formatters.createFormatter(event).formatMessage(),
            oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID)
        )
        settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }

        log.info("Sent")
    }

    override fun getSuccessNotification() = NotificationData(
        title = context.getString(R.string.telegram_successfully_send),
        target = MainActivity::class
    )

    override fun getErrorNotification(error: Throwable): NotificationData? {
        when (error) {
            is TelegramException -> {
                return NotificationData(
                    id = NTF_TELEGRAM,
                    text = context.getString(error.getLocalizedText()),
                    target = TelegramSettingsActivity::class
                )
            }
            else -> {
                log.warn("Unhandled error: $error")
                return null
            }
        }
    }

    companion object {
        private val log = Logger("TelegramMessenger")
    }
}
