package com.bopr.android.smailer

import android.Manifest.permission.READ_CALL_LOG
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.telephony.TelephonyManager.*
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.util.deviceName
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [CallReceiver] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class CallReceiverTest : BaseTest() {

    @get:Rule
    val permissionRule: GrantPermissionRule = grant(READ_CALL_LOG, READ_PHONE_STATE)

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mock()
    }

    @Suppress("DEPRECATION")
    private fun calStateIntent(state: String, number: String?): Intent {
        val intent = Intent(ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(EXTRA_STATE, state)
        number?.run { intent.putExtra(EXTRA_INCOMING_NUMBER, number) }
        return intent
    }

    /**
     * Checks that receiver starts service on incoming call.
     */
    @Test
    fun testReceiveIncomingCall() {
        val receiver = CallReceiver()

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_RINGING, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_OFFHOOK, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_IDLE, "100"))
        verify(context).startService(argThat {
            getParcelableExtra<PhoneEvent>("event")!!.run {
                isIncoming && !isMissed && phone == "100"
                        && location == null && endTime != null && acceptor == deviceName()
                        && state == STATE_PENDING
            }
        })
    }

    @Test
    @TargetApi(Build.VERSION_CODES.Q)
    fun testReceiveIncomingCallQ() {
        val receiver = CallReceiver()

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_RINGING, null))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_RINGING, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_OFFHOOK, null))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_OFFHOOK, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_IDLE, null))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_IDLE, "100"))
        verify(context).startService(argThat {
            getParcelableExtra<PhoneEvent>("event")!!.run {
                isIncoming && !isMissed && phone == "100"
                        && location == null && endTime != null && acceptor == deviceName()
                        && state == STATE_PENDING
            }
        })
    }

    /**
     * Checks that receiver starts service on outgoing call.
     */
    @Test
    fun testReceiveOutgoingCall() {
        val receiver = CallReceiver()

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_OFFHOOK, "200"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_IDLE, "200"))
        verify(context).startService(argThat {
            getParcelableExtra<PhoneEvent>("event")!!.run {
                !isIncoming && !isMissed && phone == "200"
                        && location == null && endTime != null && acceptor == deviceName()
                        && state == STATE_PENDING
            }
        })
    }

    /**
     * Checks that receiver starts service on missed call.
     */
    @Test
    fun testReceiveMissedCall() {
        val receiver = CallReceiver()

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_RINGING, "300"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(EXTRA_STATE_IDLE, "300"))
        verify(context).startService(argThat {
            getParcelableExtra<PhoneEvent>("event")!!.run {
                isIncoming && isMissed && phone == "300"
                        && location == null && endTime != null && acceptor == deviceName()
                        && state == STATE_PENDING
            }
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
            getParcelableExtra<PhoneEvent>("event")!!.run {
                isSms && isIncoming && phone == "+15555215556" && text == "Text message"
                        && location == null && endTime != null && acceptor == deviceName()
                        && state == STATE_PENDING
            }
        })
    }
}