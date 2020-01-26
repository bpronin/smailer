package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;

import static android.content.Intent.ACTION_NEW_OUTGOING_CALL;
import static android.telephony.SmsMessage.FORMAT_3GPP;
import static android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED;
import static android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER;
import static android.telephony.TelephonyManager.EXTRA_STATE;
import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;
import static android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.EXTRA_STATE_RINGING;
import static com.bopr.android.smailer.CallReceiver.SMS_RECEIVED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CallReceiver} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallReceiverTest extends BaseTest {

    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context = mock(Context.class);
        when(context.getResources()).thenReturn(getContext().getResources());
    }

    /**
     * Checks that receiver starts service on incoming call.
     */
    @Test
    public void testReceiveIncomingCall() {
        MethodInvocationsCollector invocations = new MethodInvocationsCollector();

        doAnswer(invocations).when(context).startService(any(Intent.class));

        CallReceiver receiver = new CallReceiver();

        /* ringing */
        Intent intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_RINGING);
        intent.putExtra(EXTRA_INCOMING_NUMBER, "123");

        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());

        /* off hook */
        intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_OFFHOOK);

        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());

        /* end call */
        intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_IDLE);

        receiver.onReceive(context, intent);

        Intent result = invocations.getArgument(0, 0);
        PhoneEvent event = result.getParcelableExtra("event");

        assertNotNull(event);
        assertEquals("123", event.getPhone());
        assertTrue(event.isIncoming());
    }

    /**
     * Checks that receiver starts service on outgoing call.
     */
    @Test
    public void testReceiveOutgoingCall() {
        MethodInvocationsCollector invocations = new MethodInvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        CallReceiver receiver = new CallReceiver();

        /* ringing */
        Intent intent = new Intent(ACTION_NEW_OUTGOING_CALL);
        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, "123");

        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());

        /* off hook */
        intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_OFFHOOK);

        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());

        /* end call */
        intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_IDLE);

        receiver.onReceive(context, intent);

        Intent result = invocations.getArgument(0, 0);
        PhoneEvent event = result.getParcelableExtra("event");

        assertNotNull(event);
        assertEquals("123", event.getPhone());
        assertFalse(event.isIncoming());
    }

    /**
     * Checks that receiver starts service on missed call.
     */
    @Test
    public void testReceiveMissedCall() {
        MethodInvocationsCollector invocations = new MethodInvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        CallReceiver receiver = new CallReceiver();

        /* ringing */
        Intent intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_RINGING);
        intent.putExtra(EXTRA_INCOMING_NUMBER, "123");

        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());

        /* end call */
        intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_IDLE);

        receiver.onReceive(context, intent);

        Intent result = invocations.getArgument(0, 0);
        PhoneEvent event = result.getParcelableExtra("event");

        assertNotNull(event);
        assertEquals("123", event.getPhone());
        assertTrue(event.isMissed());
    }

    /**
     * Checks that receiver starts service on sms.
     */
    @Test
    public void testReceiveSms() {
        MethodInvocationsCollector invocations = new MethodInvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        CallReceiver receiver = new CallReceiver();

        Intent intent = new Intent(SMS_RECEIVED);
        intent.putExtra("format", FORMAT_3GPP);
        intent.putExtra("pdus", new Object[]{new byte[]{0, 32, 11, -111, 81, 85, 37, 81, 85, -10, 0,
                0, 2, 16, 98, 2, 16, -109, 41, 12, -44, 50, -98, 14, 106, -105, -25, -13, -16, -71, 12}});

        receiver.onReceive(context, intent);

        Intent result = invocations.getArgument(0, 0);
        PhoneEvent event = result.getParcelableExtra("event");

        assertNotNull(event);
        assertEquals("+15555215556", event.getPhone());
        assertEquals("Text message", event.getText());
    }

}