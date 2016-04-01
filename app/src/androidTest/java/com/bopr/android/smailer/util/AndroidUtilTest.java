package com.bopr.android.smailer.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableString;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import org.mockito.Mockito;

import static android.Manifest.permission.BROADCAST_SMS;
import static org.mockito.Mockito.*;

/**
 * {@link AndroidUtil} tester.
 */
public class AndroidUtilTest extends BaseTest {

    /**
     * Tests {@link AndroidUtil#validatedText(Context, String, boolean)} method.
     *
     * @throws Exception when failed
     */
    public void testValidateText() throws Exception {
        Spannable spannable = AndroidUtil.validatedText(getContext(), "Invalid text", false);
        assertTrue(spannable instanceof SpannableString);
        Object span = spannable.getSpans(0, spannable.length(), Object.class)[0];
        assertTrue(span instanceof WavyUnderlineSpan);
    }

    /**
     * Tests {@link AndroidUtil#hasInternetConnection(Context)}} method.
     *
     * @throws Exception when failed
     */
    @SuppressWarnings("ResourceType")
    public void testHasInternetConnection() throws Exception {
        NetworkInfo info = mock(NetworkInfo.class);

        ConnectivityManager manager = mock(ConnectivityManager.class);
        when(manager.getActiveNetworkInfo()).thenReturn(info);

        Context context = mock(Context.class);
        when(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE))).thenReturn(manager);

        when(info.isConnected()).thenReturn(true);

        assertTrue(AndroidUtil.hasInternetConnection(context));

        when(info.isConnected()).thenReturn(false);

        assertFalse(AndroidUtil.hasInternetConnection(context));
    }

}