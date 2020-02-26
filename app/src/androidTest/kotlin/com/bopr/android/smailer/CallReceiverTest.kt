package com.bopr.android.smailer

import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.telephony.TelephonyManager.*
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

/**
 * [CallReceiver] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallReceiverTest : BaseTest() {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mock {
            on { resources }.doReturn(targetContext.resources)
        }
    }

    /**
     * Checks that receiver starts service on incoming call.
     */
    @Test
    fun testReceiveIncomingCall() {
        val receiver = CallReceiver()

        /* ringing */
        var intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_RINGING)
        @Suppress("DEPRECATION")
        intent.putExtra(EXTRA_INCOMING_NUMBER, "123")

        receiver.onReceive(context, intent)

        /* not yet */
        verify(context, never()).startService(anyOrNull())

        /* off hook */
        intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_OFFHOOK)

        receiver.onReceive(context, intent)

        /* not yet */
        verify(context, never()).startService(anyOrNull())

        /* end call */
        intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_IDLE)

        receiver.onReceive(context, intent)

        verify(context).startService(argThat {
            val event: PhoneEvent = getParcelableExtra("event")!!
            event.isIncoming
                    && event.phone == "123"
                    && event.acceptor == deviceName()
        })
    }

    /**
     * Checks that receiver starts service on outgoing call.
     */
    @Test
    fun testReceiveOutgoingCall() {
        val receiver = CallReceiver()

        /* ringing */
        @Suppress("DEPRECATION")
        var intent = Intent(Intent.ACTION_NEW_OUTGOING_CALL)
        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, "123")

        receiver.onReceive(context, intent)

        /* not yet */
        verify(context, never()).startService(anyOrNull())

        /* off hook */
        intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_OFFHOOK)

        receiver.onReceive(context, intent)

        /* not yet */
        verify(context, never()).startService(anyOrNull())

        /* end call */
        intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_IDLE)

        receiver.onReceive(context, intent)

        verify(context).startService(argThat {
            val event: PhoneEvent = getParcelableExtra("event")!!
            !event.isIncoming
                    && event.phone == "123"
                    && event.acceptor == deviceName()
        })
    }

    /**
     * Checks that receiver starts service on missed call.
     */
    @Test
    fun testReceiveMissedCall() {
        val receiver = CallReceiver()

        /* ringing */
        var intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_RINGING)
        @Suppress("DEPRECATION")
        intent.putExtra(EXTRA_INCOMING_NUMBER, "123")

        receiver.onReceive(context, intent)

        /* not yet */
        verify(context, never()).startService(anyOrNull())

        /* end call */
        intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_IDLE)

        receiver.onReceive(context, intent)

        verify(context).startService(argThat {
            val event: PhoneEvent = getParcelableExtra("event")!!
            event.isMissed
                    && event.phone == "123"
                    && event.acceptor == deviceName()
        })
    }

    /**
     * Checks that receiver starts service on sms.
     */
    @Test
    fun testReceiveSms() {
        val receiver = CallReceiver()

        val intent = Intent(CallReceiver.SMS_RECEIVED)
        intent.putExtra("format", SmsMessage.FORMAT_3GPP)
        intent.putExtra("pdus", arrayOf<Any>(byteArrayOf(0, 32, 11, -111, 81, 85, 37, 81, 85, -10, 0,
                0, 2, 16, 98, 2, 16, -109, 41, 12, -44, 50, -98, 14, 106, -105, -25, -13, -16, -71, 12)))

        receiver.onReceive(context, intent)

        verify(context).startService(argThat {
            val event: PhoneEvent = getParcelableExtra("event")!!
            event.isSms
                    && event.phone == "+15555215556"
                    && event.text == "Text message"
                    && event.acceptor == deviceName()
        })
    }
}