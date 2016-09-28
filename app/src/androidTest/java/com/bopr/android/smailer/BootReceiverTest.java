package com.bopr.android.smailer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.bopr.android.smailer.util.Util;

import java.util.Collections;

import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
    public void testReceiveEnabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_TRIGGERS), anySetOf(String.class)))
                .thenReturn(Util.asSet(VAL_PREF_TRIGGER_OUT_SMS));
        when(preferences.getBoolean(eq(Settings.KEY_PREF_SERVICE_ENABLED), anyBoolean()))
                .thenReturn(true);

        BootReceiver receiver = new BootReceiver();

        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(context, intent);

        Intent result = (Intent) invocations.get(0)[0];
        assertEquals(OutgoingSmsService.class.getName(), result.getComponent().getClassName());
    }

    /**
     * Checks that receiver does not start service when preference is off.
     *
     * @throws Exception when fails
     */
    public void testReceiveDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));

        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_TRIGGERS), anySetOf(String.class)))
                .thenReturn(Collections.<String>emptySet());

        BootReceiver receiver = new BootReceiver();

        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());
    }

    /**
     * Checks that receiver does not start service when service is disabled.
     *
     * @throws Exception when fails
     */
    public void testReceiveServiceDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(preferences.getBoolean(eq(Settings.KEY_PREF_SERVICE_ENABLED), anyBoolean())).thenReturn(false);

        BootReceiver receiver = new BootReceiver();

        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());
    }

}