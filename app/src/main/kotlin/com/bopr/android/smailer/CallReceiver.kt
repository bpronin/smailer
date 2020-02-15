package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_NEW_OUTGOING_CALL
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.TelephonyManager.*
import com.bopr.android.smailer.CallProcessorService.Companion.startCallProcessingService
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

/**
 * Receives phone call and sms intents and starts mailer service.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: $intent")

        when (intent.action) {
            ACTION_NEW_OUTGOING_CALL ->
                lastCallNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
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
        val callState = intent.getStringExtra(EXTRA_STATE)
        if (callState != null && lastCallState != callState) {
            when (callState) {
                EXTRA_STATE_RINGING -> {
                    isIncomingCall = true
                    callStartTime = currentTimeMillis()
                    lastCallNumber = intent.getStringExtra(EXTRA_INCOMING_NUMBER) // TODO: 06.02.2020 deprecated

                    log.debug("Call received")
                }
                EXTRA_STATE_OFFHOOK -> {
                    isIncomingCall = lastCallState == EXTRA_STATE_RINGING
                    callStartTime = currentTimeMillis()

                    log.debug("Started ${if (isIncomingCall) "incoming" else "outgoing"} call")
                }
                EXTRA_STATE_IDLE -> {
                    when {
                        lastCallState == EXTRA_STATE_RINGING -> {
                            log.debug("Processing missed call")

                            processCall(context, incoming = true, missed = true)
                        }
                        isIncomingCall -> {
                            log.debug("Processing incoming call")

                            processCall(context, incoming = true, missed = false)
                        }
                        else -> {
                            log.debug("Processing outgoing call")

                            processCall(context, incoming = false, missed = false)
                        }
                    }
                    lastCallNumber = null
                }
            }
            lastCallState = callState
        }
    }

    /**
     * Processes sms intent.
     */
    private fun onSmsReceived(context: Context, intent: Intent) {
        val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } else {
            parseMessageLegacy(intent)
        }

        if (messages.isNotEmpty()) {
            val time = messages[0].timestampMillis  /* NOTE: time zone on emulator may be incorrect */
            val event = PhoneEvent(isIncoming = true,
                    phone = messages[0].displayOriginatingAddress,
                    acceptor = deviceName(),
                    startTime = time,
                    endTime = time,
                    text = messages.joinToString(separator = "", transform = { m -> m.displayMessageBody })
            )

            startCallProcessingService(context, event)
        }
    }

    private fun processCall(context: Context, incoming: Boolean, missed: Boolean) {
        startCallProcessingService(context, PhoneEvent(
                acceptor = deviceName(),
                phone = lastCallNumber!!,
                startTime = callStartTime,
                endTime = currentTimeMillis(),
                isIncoming = incoming,
                isMissed = missed
        ))
    }

    private fun parseMessageLegacy(intent: Intent): Array<SmsMessage?> {
        val pdus = intent.getSerializableExtra("pdus") as Array<*>
        val messages = arrayOfNulls<SmsMessage>(pdus.size)
        for (i in pdus.indices) {
            val pdu = pdus[i] as ByteArray
            @Suppress("DEPRECATION")
            messages[i] = SmsMessage.createFromPdu(pdu)
        }
        return messages
    }

    companion object {
        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        private val log = LoggerFactory.getLogger("CallReceiver")

        private var lastCallNumber: String? = null
        private var lastCallState = EXTRA_STATE_IDLE
        private var callStartTime: Long = 0
        private var isIncomingCall = false
    }
}