package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import com.bopr.android.smailer.util.Util;
import org.junit.Test;

import static com.bopr.android.smailer.MailerService.ACTION_CALL;
import static com.bopr.android.smailer.Settings.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anySetOf;
import static org.mockito.Mockito.*;

/**
 * {@link SmsReceiver} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiverTest extends BaseTest {

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
     * Checks that receiver starts mail service on incoming sms.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceive() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_TRIGGERS), anySetOf(String.class))).thenReturn(Util.asSet(VAL_PREF_TRIGGER_IN_SMS));

        SmsReceiver receiver = new SmsReceiver();

        Intent intent = new Intent(SmsReceiver.SMS_RECEIVED_ACTION);
        intent.putExtra("pdus", new Object[]{Base64.decode("ACADgSHzAABhQEASFTQhBcgym/0G", Base64.NO_WRAP)}); /* encoded "Hello" from 123 */
        intent.putExtra("format", "3gpp");

        receiver.onReceive(context, intent);
        Intent result = (Intent) invocations.get(0)[0];

        assertEquals(ACTION_CALL, result.getAction());
        assertEquals("123", result.getStringExtra(MailerService.EXTRA_PHONE_NUMBER));
        assertEquals("Hello", result.getStringExtra(MailerService.EXTRA_TEXT));
        assertEquals(1459795903000L, result.getLongExtra(MailerService.EXTRA_START_TIME, 0));
    }

}
