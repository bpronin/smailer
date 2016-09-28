package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static com.bopr.android.smailer.MailerService.ACTION_RESEND;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link ConnectivityReceiver} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ConnectivityReceiverTest extends BaseTest {

    private Context context;
    private NetworkInfo networkInfo;
    private SharedPreferences preferences;

    @Override
    @SuppressWarnings("ResourceType")
    public void setUp() throws Exception {
        super.setUp();

        preferences = mock(SharedPreferences.class);
        when(preferences.getBoolean(eq(KEY_PREF_RESEND_UNSENT), anyBoolean())).thenReturn(true);
        when(preferences.getBoolean(eq(KEY_PREF_SERVICE_ENABLED), anyBoolean())).thenReturn(true);

        networkInfo = mock(NetworkInfo.class);
        when(networkInfo.isConnected()).thenReturn(true);

        ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);

        context = mock(Context.class);
        when(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE))).thenReturn(connectivityManager);
        when(context.getResources()).thenReturn(getContext().getResources());
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(preferences);
    }

    /**
     * Checks that receiver starts service when internet connection is on.
     *
     * @throws Exception when fails
     */
    public void testReceiveConnectionOn() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(networkInfo.isConnected()).thenReturn(true);

        ConnectivityReceiver receiver = new ConnectivityReceiver();

        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver.onReceive(context, intent);

        Intent result = (Intent) invocations.get(0)[0];
        assertEquals(ACTION_RESEND, result.getAction());
    }

    /**
     * Checks that receiver do not starts service when internet connection is off.
     *
     * @throws Exception when fails
     */
    public void testReceiveConnectionOff() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(networkInfo.isConnected()).thenReturn(false);

        ConnectivityReceiver receiver = new ConnectivityReceiver();

        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver.onReceive(context, intent);

        assertTrue(invocations.isEmpty());
    }

    /**
     * Checks that receiver do not starts service when corresponding option is disabled.
     *
     * @throws Exception when fails
     */
    public void testReceiveDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(networkInfo.isConnected()).thenReturn(true);
        when(preferences.getBoolean(eq(KEY_PREF_RESEND_UNSENT), anyBoolean())).thenReturn(false);

        ConnectivityReceiver receiver = new ConnectivityReceiver();

        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver.onReceive(context, intent);
        assertTrue(invocations.isEmpty());
    }

    /**
     * Checks that receiver do not starts service when service is disabled.
     *
     * @throws Exception when fails
     */
    public void testReceiveServiceDisabled() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();
        doAnswer(invocations).when(context).startService(any(Intent.class));
        when(networkInfo.isConnected()).thenReturn(true);
        when(preferences.getBoolean(eq(KEY_PREF_SERVICE_ENABLED), anyBoolean())).thenReturn(false);

        ConnectivityReceiver receiver = new ConnectivityReceiver();

        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver.onReceive(context, intent);
        assertTrue(invocations.isEmpty());
    }

}