package com.bopr.android.smailer.processor.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_TELEPHONY
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.processor.EventProcessor
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.ui.SmsSettingsActivity
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.sendSmsMessage
import org.slf4j.LoggerFactory

/**
 * SMS transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class SmsEventProcessor(context: Context) : EventProcessor(context) {

    private val settings = Settings(context)
    private val notifications by lazy { NotificationsHelper(context) }

    override fun isEnabled(): Boolean {
        return settings.getBoolean(PREF_SMS_MESSENGER_ENABLED)
    }

    override fun prepare(): Boolean {
        return true
    }

    override fun process(event: Event) {
        settings.getStringList(PREF_SMS_MESSENGER_RECIPIENTS).forEach {
            try {
                context.sendSmsMessage(it, formatMessage(event))
            } catch (x: Exception) {
                notifySendError()
            }
        }
    }

    private fun formatMessage(event: Event): String {
        return when (event.payload) {
            is PhoneEventData ->
                formatPhoneEventMessage(event.payload)
//            is BatteryEvent ->
//              formatBatteryEventMessage(event.payload)
            else ->
                throw IllegalArgumentException("No formatter for ${event.payload::class}")
        }
    }

    private fun formatPhoneEventMessage(event: PhoneEventData): String {
        return when {
            event.isSms ->
                event.text!!

            event.isMissed ->
                context.getString(R.string.you_had_missed_call)

            else ->
                context.getString(
                    if (event.isIncoming)
                        R.string.you_had_incoming_call
                    else
                        R.string.you_had_outgoing_call,
                    formatDuration(event.callDuration)
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

        private val log = LoggerFactory.getLogger("SmsEventProcessor")
    }
}