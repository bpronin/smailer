package com.bopr.android.smailer.messenger.mail

import android.Manifest
import android.Manifest.permission
import android.Manifest.permission.*
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.*
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallReceiver
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [PhoneCallReceiver] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class PhoneCallReceiverTest : BaseTest() {

    @get:Rule
    val permissionRule = grant(
        READ_CALL_LOG,
        READ_PHONE_STATE
    )

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mock()
    }

    @Suppress("DEPRECATION")
    private fun calStateIntent(state: String, number: String?): Intent {
        val intent = Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        intent.putExtra(TelephonyManager.EXTRA_STATE, state)
        number?.run { intent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, number) }
        return intent
    }

    private fun Intent.getPhoneCallExtra(): PhoneCallInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra("event", PhoneCallInfo::class.java)
        } else {
            @Suppress("DEPRECATION")
            return getParcelableExtra("event")
        }
    }

    /**
     * Checks that receiver starts service on incoming call.
     */
    @Test
    fun testReceiveIncomingCall() {
        val receiver = PhoneCallReceiver()

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_RINGING, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_OFFHOOK, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, "100"))
        verify(context).startService(argThat {
            getPhoneCallExtra()!!.run {
                isIncoming && !isMissed && phone == "100" && endTime != null
            }
        })
    }

    @Test
    @TargetApi(Build.VERSION_CODES.Q)
    fun testReceiveIncomingCallQ() {
        val receiver = PhoneCallReceiver()

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_RINGING, null))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_RINGING, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_OFFHOOK, null))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_OFFHOOK, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, null))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, "100"))
        verify(context).startService(argThat {
            getPhoneCallExtra()!!.run {
                isIncoming && !isMissed && phone == "100" && endTime != null
            }
        })
    }

    /**
     * Checks that receiver starts service on outgoing call.
     */
    @Test
    fun testReceiveOutgoingCall() {
        val receiver = PhoneCallReceiver()

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_OFFHOOK, "200"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, "200"))
        verify(context).startService(argThat {
            getPhoneCallExtra()!!.run {
                !isIncoming && !isMissed && phone == "200" && endTime != null
            }
        })
    }

    /**
     * Checks that receiver starts service on missed call.
     */
    @Test
    fun testReceiveMissedCall() {
        val receiver = PhoneCallReceiver()

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_RINGING, "300"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, "300"))
        verify(context).startService(argThat {
            getPhoneCallExtra()!!.run {
                isIncoming && isMissed && phone == "300" && endTime != null
            }
        })
    }

    /**
     * Checks that receiver starts service on sms.
     */
    @Test
    fun testReceiveSms() {
        val receiver = PhoneCallReceiver()

        val intent = Intent(PhoneCallReceiver.SMS_RECEIVED)
        intent.putExtra("format", SmsMessage.FORMAT_3GPP)
        intent.putExtra(
            "pdus", arrayOf<Any>(
                byteArrayOf(
                    0, 32, 11, -111, 81, 85, 37, 81, 85, -10, 0, 0, 2, 16, 98, 2, 16, -109,
                    41, 12, -44, 50, -98, 14, 106, -105, -25, -13, -16, -71, 12
                )
            )
        )

        receiver.onReceive(context, intent)

        verify(context).startService(argThat {
            getPhoneCallExtra()!!.run {
                isSms && isIncoming && phone == "+15555215556" && text == "Text message"
                        && endTime != null
            }
        })
    }
}