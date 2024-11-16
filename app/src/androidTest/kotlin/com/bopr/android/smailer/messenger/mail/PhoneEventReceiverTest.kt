package com.bopr.android.smailer.messenger.mail

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.provider.telephony.PhoneEventReceiver
import com.bopr.android.smailer.util.DEVICE_NAME
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [PhoneEventReceiver] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class PhoneEventReceiverTest : BaseTest() {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE
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

    private fun Intent.getPhoneEventExtra(name: String): PhoneEventData? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, PhoneEventData::class.java)
        } else {
            @Suppress("DEPRECATION")
            return getParcelableExtra(name)
        }
    }

    /**
     * Checks that receiver starts service on incoming call.
     */
    @Test
    fun testReceiveIncomingCall() {
        val receiver = PhoneEventReceiver()

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_RINGING, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_OFFHOOK, "100"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, "100"))
        verify(context).startService(argThat {
            getPhoneEventExtra("event")!!.run {
                isIncoming && !isMissed && phone == "100"
                        && location == null && endTime != null && acceptor == DEVICE_NAME
                        && processState == STATE_PENDING
            }
        })
    }

    @Test
    @TargetApi(Build.VERSION_CODES.Q)
    fun testReceiveIncomingCallQ() {
        val receiver = PhoneEventReceiver()

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
            getPhoneEventExtra("event")!!.run {
                isIncoming && !isMissed && phone == "100"
                        && location == null && endTime != null && acceptor == DEVICE_NAME
                        && processState == STATE_PENDING
            }
        })
    }

    /**
     * Checks that receiver starts service on outgoing call.
     */
    @Test
    fun testReceiveOutgoingCall() {
        val receiver = PhoneEventReceiver()

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_OFFHOOK, "200"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, "200"))
        verify(context).startService(argThat {
            getPhoneEventExtra("event")!!.run {
                !isIncoming && !isMissed && phone == "200"
                        && location == null && endTime != null && acceptor == DEVICE_NAME
                        && processState == STATE_PENDING
            }
        })
    }

    /**
     * Checks that receiver starts service on missed call.
     */
    @Test
    fun testReceiveMissedCall() {
        val receiver = PhoneEventReceiver()

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_RINGING, "300"))
        verify(context, never()).startService(anyOrNull())

        receiver.onReceive(context, calStateIntent(TelephonyManager.EXTRA_STATE_IDLE, "300"))
        verify(context).startService(argThat {
            getPhoneEventExtra("event")!!.run {
                isIncoming && isMissed && phone == "300"
                        && location == null && endTime != null && acceptor == DEVICE_NAME
                        && processState == STATE_PENDING
            }
        })
    }

    /**
     * Checks that receiver starts service on sms.
     */
    @Test
    fun testReceiveSms() {
        val receiver = PhoneEventReceiver()

        val intent = Intent(PhoneEventReceiver.SMS_RECEIVED)
        intent.putExtra("format", SmsMessage.FORMAT_3GPP)
        intent.putExtra(
            "pdus", arrayOf<Any>(
                byteArrayOf(
                    0,
                    32,
                    11,
                    -111,
                    81,
                    85,
                    37,
                    81,
                    85,
                    -10,
                    0,
                    0,
                    2,
                    16,
                    98,
                    2,
                    16,
                    -109,
                    41,
                    12,
                    -44,
                    50,
                    -98,
                    14,
                    106,
                    -105,
                    -25,
                    -13,
                    -16,
                    -71,
                    12
                )
            )
        )

        receiver.onReceive(context, intent)

        verify(context).startService(argThat {
            getPhoneEventExtra("event")!!.run {
                isSms && isIncoming && phone == "+15555215556" && text == "Text message"
                        && location == null && endTime != null && acceptor == DEVICE_NAME
                        && processState == STATE_PENDING
            }
        })
    }
}