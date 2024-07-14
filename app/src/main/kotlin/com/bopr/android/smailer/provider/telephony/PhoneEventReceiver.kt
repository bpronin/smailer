package com.bopr.android.smailer.provider.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED
import android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER
import android.telephony.TelephonyManager.EXTRA_STATE
import android.telephony.TelephonyManager.EXTRA_STATE_IDLE
import android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK
import android.telephony.TelephonyManager.EXTRA_STATE_RINGING
import com.bopr.android.smailer.provider.telephony.PhoneEventProcessorWorker.Companion.startPhoneEventProcessing
import com.bopr.android.smailer.util.deviceName
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

/**
 * Receives phone call and sms intents and starts mailer service.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: {}", intent)

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
            val time = messages[0].timestampMillis

            context.startPhoneEventProcessing(
                PhoneEventInfo(
                    phone = messages[0].displayOriginatingAddress,
                    isIncoming = true,
                    startTime = time,
                    endTime = time,
                    text = messages.joinToString(
                        separator = "",
                        transform = { m -> m.displayMessageBody }),
                    acceptor = deviceName()
                )
            )
        }
    }

    /**
     * Processes last call
     */
    private fun processCall(context: Context) {
        context.startPhoneEventProcessing(
            PhoneEventInfo(
                phone = callNumber!!,
                isIncoming = isIncomingCall!!,
                isMissed = isMissedCall!!,
                startTime = callStartTime!!,
                endTime = callEndTime!!,
                acceptor = deviceName()
            )
        )
    }

    companion object {

        private val log = LoggerFactory.getLogger("PhoneEventReceiver")

        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"

        private var callNumber: String? = null
        private var prevCallState: String? = null
        private var callStartTime: Long? = null
        private var callEndTime: Long? = null
        private var isIncomingCall: Boolean? = null
        private var isMissedCall: Boolean? = null
    }
}