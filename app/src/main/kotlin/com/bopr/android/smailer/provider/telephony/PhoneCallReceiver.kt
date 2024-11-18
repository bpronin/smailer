package com.bopr.android.smailer.provider.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED
import android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER
import android.telephony.TelephonyManager.EXTRA_STATE
import android.telephony.TelephonyManager.EXTRA_STATE_IDLE
import android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK
import android.telephony.TelephonyManager.EXTRA_STATE_RINGING
import com.bopr.android.smailer.provider.telephony.PhoneCallProcessor.Companion.processPhoneCall
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.Logger
import java.lang.System.currentTimeMillis

/**
 * Receives phone call and sms intents and starts messengers service.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: $intent")

        when (intent.action) {
            ACTION_PHONE_STATE_CHANGED ->
                onCallStateChanged(context, intent)

            SMS_RECEIVED ->
                onSmsReceived(context, intent)
        }
    }

    /**
     * Deals with actual events.
     * Incoming call: goes from IDLE to RINGING when it rings, to OFF HOOK when it's answered, to IDLE when its hung up.
     * Outgoing call: goes from IDLE to OFF HOOK when it dials out, to IDLE when hung up.
     *
     * @param context context
     * @param intent  call state intent
     */
    private fun onCallStateChanged(context: Context, intent: Intent) {
        /* see https://stackoverflow.com/questions/52009874/getting-the-caller-id-in-android-9 */
        @Suppress("DEPRECATION")
        callNumber = intent.getStringExtra(EXTRA_INCOMING_NUMBER)
        if (callNumber == null) return

        val callState = intent.getStringExtra(EXTRA_STATE)
        when (callState) {
            EXTRA_STATE_RINGING -> {
                callStartTime = currentTimeMillis()
                isIncomingCall = true

                log.debug("Ringing call from: $callNumber")
            }

            EXTRA_STATE_OFFHOOK -> {
                if (prevCallState == EXTRA_STATE_RINGING) {
                    log.debug("Started incoming call from: $callNumber")
                } else {
                    callStartTime = currentTimeMillis()
                    isIncomingCall = false

                    log.debug("Started outgoing call to: $callNumber")
                }
            }

            EXTRA_STATE_IDLE -> {
                callEndTime = currentTimeMillis()
                isMissedCall = prevCallState == EXTRA_STATE_RINGING
                when {
                    isMissedCall == true -> {
                        log.debug("Processing missed call from: $callNumber")

                        processCall(context)
                    }

                    isIncomingCall == true -> {
                        log.debug("Processing incoming call from: $callNumber")

                        processCall(context)
                    }

                    isIncomingCall == false -> {
                        log.debug("Processing outgoing call to: $callNumber")

                        processCall(context)
                    }
                }

                callNumber = null
                isIncomingCall = null
                isMissedCall = null
                callStartTime = null
                callEndTime = null
                prevCallState = null
            }
        }
        prevCallState = callState
    }

    /**
     * Processes sms intent.
     */
    private fun onSmsReceived(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        if (messages.isNotEmpty()) {
            /* NOTE: time zone on emulator may be incorrect */
            context.processPhoneCall(
                PhoneCallInfo(
                    phone = messages[0].displayOriginatingAddress,
                    isIncoming = true,
                    startTime = messages[0].timestampMillis,
                    endTime = messages[0].timestampMillis,
                    text = messages.joinToString<SmsMessage>(
                        separator = "",
                        transform = { it.displayMessageBody }),
                    acceptor = DEVICE_NAME
                )
            )
        }
    }

    /**
     * Processes last call
     */
    private fun processCall(context: Context) {
        context.processPhoneCall(
            PhoneCallInfo(
                phone = callNumber!!,
                isIncoming = isIncomingCall!!,
                isMissed = isMissedCall!!,
                startTime = callStartTime!!,
                endTime = callEndTime!!,
                acceptor = DEVICE_NAME
            )
        )
    }

    companion object {

        private val log = Logger("PhoneEventReceiver")

        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"

        private var callNumber: String? = null
        private var prevCallState: String? = null
        private var callStartTime: Long? = null
        private var callEndTime: Long? = null
        private var isIncomingCall: Boolean? = null
        private var isMissedCall: Boolean? = null
    }
}
