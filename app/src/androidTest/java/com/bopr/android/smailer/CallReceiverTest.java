package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.bopr.android.smailer.util.Util;

import org.junit.Test;

import static android.content.Intent.ACTION_NEW_OUTGOING_CALL;
import static android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED;
import static android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER;
import static android.telephony.TelephonyManager.EXTRA_STATE;
import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;
import static android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.EXTRA_STATE_RINGING;
import static com.bopr.android.smailer.MailerService.ACTION_CALL;
import static com.bopr.android.smailer.MailerService.EXTRA_END_TIME;
import static com.bopr.android.smailer.MailerService.EXTRA_INCOMING;
import static com.bopr.android.smailer.MailerService.EXTRA_MISSED;
import static com.bopr.android.smailer.MailerService.EXTRA_PHONE_NUMBER;
import static com.bopr.android.smailer.MailerService.EXTRA_START_TIME;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anySetOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CallReceiver} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallReceiverTest extends BaseTest {

    private SharedPreferences preferences;
    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        preferences = mock(SharedPreferences.class);

        context = mock(Context.class);
        when(context.getResources()).thenReturn(getContext().getResources());
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(preferences);
    }

    /**
     * Checks that receiver starts service on incoming call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveIncomingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_TRIGGERS), anySetOf(String.class))).thenReturn(Util.asSet(VAL_PREF_TRIGGER_IN_CALLS));

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

        Intent result = (Intent) invocations.get(0)[0];
        assertEquals(ACTION_CALL, result.getAction());
        assertTrue(result.getBooleanExtra(EXTRA_INCOMING, false));
        assertFalse(result.hasExtra(EXTRA_MISSED));
        assertTrue(result.getLongExtra(EXTRA_START_TIME, 0) != 0);
        assertTrue(result.getLongExtra(EXTRA_END_TIME, 0) != 0);
        assertEquals("123", result.getStringExtra(EXTRA_PHONE_NUMBER));
    }

    /**
     * Checks that receiver do not starts mail service on incoming call when corresponding setting is disabled.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveIncomingCallDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        CallReceiver receiver = new CallReceiver();

        /* ringing */
        Intent intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_RINGING);
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

        assertTrue(invocations.isEmpty());
    }

    /**
     * Checks that receiver starts service on outgoing call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveOutgoingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_TRIGGERS), anySetOf(String.class))).thenReturn(Util.asSet(VAL_PREF_TRIGGER_OUT_CALLS));

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

        Intent result = (Intent) invocations.get(0)[0];
        assertEquals(ACTION_CALL, result.getAction());
        assertFalse(result.getBooleanExtra(EXTRA_INCOMING, true));
        assertFalse(result.hasExtra(EXTRA_MISSED));
        assertTrue(result.getLongExtra(EXTRA_START_TIME, 0) != 0);
        assertTrue(result.getLongExtra(EXTRA_END_TIME, 0) != 0);
        assertEquals("123", result.getStringExtra(EXTRA_PHONE_NUMBER));
    }

    /**
     * Checks that receiver do not starts mail service on outgoing call when corresponding setting is disabled.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveOutgoingCallDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        CallReceiver receiver = new CallReceiver();

         /* ringing */
        Intent intent = new Intent(ACTION_NEW_OUTGOING_CALL);
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

        assertTrue(invocations.isEmpty());
    }

    /**
     * Checks that receiver starts service on missed call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveMissedCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_TRIGGERS), anySetOf(String.class))).thenReturn(Util.asSet(VAL_PREF_TRIGGER_MISSED_CALLS));

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

        Intent result = (Intent) invocations.get(0)[0];
        assertEquals(ACTION_CALL, result.getAction());
        assertFalse(result.hasExtra(EXTRA_INCOMING));
        assertTrue(result.getBooleanExtra(EXTRA_MISSED, false));
        assertTrue(result.getLongExtra(EXTRA_START_TIME, 0) != 0);
        assertFalse(result.hasExtra(EXTRA_END_TIME));
        assertEquals("123", result.getStringExtra(EXTRA_PHONE_NUMBER));
    }

    /**
     * Checks that receiver do not starts mail service on missed call when corresponding setting is disabled.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveMissedCallDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        CallReceiver receiver = new CallReceiver();

        /* ringing */
        Intent intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_RINGING);
        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());

        /* end call */
        intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, EXTRA_STATE_IDLE);
        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());
    }

}