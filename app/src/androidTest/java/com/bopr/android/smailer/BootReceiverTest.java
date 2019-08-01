package com.bopr.android.smailer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.bopr.android.smailer.util.Util;

import org.junit.Test;

import java.util.Collections;

import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link BootReceiver} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class BootReceiverTest extends BaseTest {

    private Context context;
    private SharedPreferences preferences;

    @Override
    @SuppressWarnings("ResourceType")
    public void setUp() throws Exception {
        super.setUp();

        preferences = mock(SharedPreferences.class);
        context = mock(Context.class);
        when(context.getResources()).thenReturn(getContext().getResources());
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(preferences);

        ActivityManager manager = mock(ActivityManager.class);
        when(context.getSystemService(eq(Context.ACTIVITY_SERVICE))).thenReturn(manager);
    }

    /**
     * Checks that receiver starts service when preference is on.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveEnabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        when(preferences.getStringSet(eq(PREF_EMAIL_TRIGGERS), anySetOf(String.class)))
                .thenReturn(Util.asSet(VAL_PREF_TRIGGER_OUT_SMS));

        BootReceiver receiver = new BootReceiver();

        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(context, intent);

        Intent result = (Intent) invocations.invocation(0)[0];
        assertEquals(ContentObserverService.class.getName(), result.getComponent().getClassName());
    }

    /**
     * Checks that receiver does not start service when preference is off.
     *
     * @throws Exception when fails
     */
    @Test
    public void testReceiveDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        when(preferences.getStringSet(eq(PREF_EMAIL_TRIGGERS), anySetOf(String.class)))
                .thenReturn(Collections.<String>emptySet());

        BootReceiver receiver = new BootReceiver();

        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());
    }

}