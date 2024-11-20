package com.bopr.android.smailer.messenger.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEPHONY
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_SENT_BY_SMS
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.provider.battery.BatteryInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.ui.SmsSettingsActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.sendSmsMessage

/**
 * SMS messenger.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class SmsMessenger(private val context: Context) : Messenger {

    private val settings = Settings(context)
    private val notifications by lazy { NotificationsHelper(context) }

    override fun initialize(): Boolean {
        if (settings.getBoolean(PREF_SMS_MESSENGER_ENABLED)) {
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
        if (!settings.getBoolean(PREF_SMS_MESSENGER_ENABLED)
            || FLAG_SENT_BY_SMS in event.processFlags
        ) return

        log.debug("Sending").verb(event)

        settings.getStringList(PREF_SMS_MESSENGER_RECIPIENTS).forEach {
            try {
                log.debug("Sent")

                context.sendSmsMessage(it, formatMessage(event))
                event.processFlags += FLAG_SENT_BY_SMS
                notifySendSuccess()
                onSuccess()
            } catch (x: Exception) {
                log.warn("Send failed", x)

                event.processFlags -= FLAG_SENT_BY_SMS
                notifySendError()
                onError(x)
            }
        }
    }

    private fun formatMessage(event: Event): String {
        val payload = event.payload
        return when (payload) {
            is PhoneCallInfo -> formatPhoneCallMessage(payload)
            is BatteryInfo ->  formatBatteryEventMessage(payload)
            else -> throw IllegalArgumentException("No formatter for $payload")
        }
    }

    private fun formatPhoneCallMessage(info: PhoneCallInfo?): String {
        return when {
            info == null -> ""
            info.isSms -> info.text!!
            info.isMissed -> context.getString(R.string.you_had_missed_call)

            else -> context.getString(
                if (info.isIncoming)
                    R.string.you_had_incoming_call
                else
                    R.string.you_had_outgoing_call,
                formatDuration(info.callDuration)
            )
        }
    }

    private fun formatBatteryEventMessage(info: BatteryInfo): String {
        return info.text
    }

    private fun notifySendSuccess() {
        if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS))
            notifications.notifyInfo(
                title = context.getString(R.string.sms_successfully_send),
                target = MainActivity::class
            )
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