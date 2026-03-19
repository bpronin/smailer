package com.bopr.android.smailer.messenger.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationData
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEPHONY
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.SENT_BY_SMS
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.provider.battery.BatteryData
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.ui.SmsSettingsActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.sendSmsMessage

/**
 * SMS messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@Mockable
internal class SmsMessenger(private val context: Context) : Messenger(context, SENT_BY_SMS) {

    override val isEnabled get() = context.settings.getBoolean(PREF_SMS_MESSENGER_ENABLED)

    override suspend fun doInitialize() {}

    override suspend fun doSend(event: Event) {
        log.debug("Sending")

        val recipients = context.settings.getStringList(PREF_SMS_MESSENGER_RECIPIENTS)
        recipients.forEach {
            context.sendSmsMessage(it, formatMessage(event))
        }

        log.info("Sent")
    }

    private fun formatMessage(event: Event): String {
        return when (val payload = event.payload) {
            is PhoneCallData -> formatPhoneCallMessage(payload)
            is BatteryData -> formatBatteryEventMessage(payload)
            else -> throw IllegalArgumentException("No formatter for $payload")
        }
    }

    private fun formatPhoneCallMessage(info: PhoneCallData?): String {
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

    private fun formatBatteryEventMessage(info: BatteryData): String {
        return info.level
    }

    override fun getSuccessNotification() = NotificationData(
        title = context.getString(R.string.sms_successfully_send),
        target = MainActivity::class
    )

    override fun getErrorNotification(error: Throwable) = NotificationData(
        id = NTF_TELEPHONY,
        text = context.getString(R.string.unable_send_sms),
        target = SmsSettingsActivity::class
    )

    companion object {
        private val log = Logger("SmsMessenger")
    }
}