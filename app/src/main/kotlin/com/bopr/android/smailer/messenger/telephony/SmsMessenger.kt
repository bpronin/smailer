package com.bopr.android.smailer.messenger.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEPHONY
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.messenger.Message
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.ui.SmsSettingsActivity
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.sendSmsMessage
import com.bopr.android.smailer.util.Logger

/**
 * SMS messenger.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class SmsMessenger(private val context: Context) : Messenger {

    private val settings = Settings(context)
    private val notifications by lazy { NotificationsHelper(context) }

    override fun isEnabled(): Boolean {
        return settings.getBoolean(PREF_SMS_MESSENGER_ENABLED)
    }

    override fun initialize(): Boolean {
        return true
    }

    override fun sendMessage(
        message: Message,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        settings.getStringList(PREF_SMS_MESSENGER_RECIPIENTS).forEach {
            try {
                context.sendSmsMessage(it, formatMessage(message))
                onSuccess()
            } catch (x: Exception) {
                notifySendError()
                onError(x)
            }
        }
    }

    private fun formatMessage(message: Message): String {
        return when (message.payload) {
            is PhoneCallInfo ->
                formatPhoneCallMessage(message.payload)
//            is BatteryEvent ->
//              formatBatteryEventMessage(event.payload)
            else ->
                throw IllegalArgumentException("No formatter for ${message.payload::class}")
        }
    }

    private fun formatPhoneCallMessage(info: PhoneCallInfo): String {
        return when {
            info.isSms ->
                info.text!!

            info.isMissed ->
                context.getString(R.string.you_had_missed_call)

            else ->
                context.getString(
                    if (info.isIncoming)
                        R.string.you_had_incoming_call
                    else
                        R.string.you_had_outgoing_call,
                    formatDuration(info.callDuration)
                )
        }
    }

    private fun notifySendError() {
        notifications.notifyError(
            NTF_TELEPHONY,
            context.getString(R.string.unable_send_sms),
            SmsSettingsActivity::class
        )
    }

    companion object {

        private val log = Logger("SmsMessenger")
    }
}