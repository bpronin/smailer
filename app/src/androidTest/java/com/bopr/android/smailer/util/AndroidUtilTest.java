package com.bopr.android.smailer.util;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.CallProcessorService;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.ActivityManager.RunningServiceInfo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link AndroidUtil} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AndroidUtilTest extends BaseTest {

    /**
     * Returns true if specified service is running.
     */
    private static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests {@link AndroidUtil#validatedUnderlinedText(Context, String, boolean)} method.
     *
     * @throws Exception when failed
     */
    @Test
    public void testValidateText() throws Exception {
        Spannable spannable = AndroidUtil.validatedUnderlinedText(getContext(), "Invalid text", false);

        assertThat(spannable, instanceOf(SpannableString.class));
        Object span = spannable.getSpans(0, spannable.length(), Object.class)[0];
        assertThat(span, instanceOf(WavyUnderlineSpan.class));

        spannable = AndroidUtil.validatedUnderlinedText(getContext(), "Invalid text", true);
        assertThat(spannable, instanceOf(SpannableString.class));
        Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
        assertThat(spans, emptyArray());
    }

    /**
     * Tests {@link AndroidUtil#validatedColoredText(Context, String, boolean)} method.
     *
     * @throws Exception when failed
     */
    @Test
    public void testValidatedColoredText() throws Exception {
        Spannable spannable = AndroidUtil.validatedColoredText(getContext(), "Invalid text", false);
        assertThat(spannable, instanceOf(SpannableString.class));
        Object span = spannable.getSpans(0, spannable.length(), Object.class)[0];
        assertThat(span, instanceOf(ForegroundColorSpan.class));

        spannable = AndroidUtil.validatedColoredText(getContext(), "Invalid text", true);
        assertThat(spannable, instanceOf(SpannableString.class));
        Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
        assertThat(spans, emptyArray());
    }

    /**
     * Tests {@link AndroidUtil#hasInternetConnection(Context)}} method.
     *
     * @throws Exception when failed
     */
    @SuppressWarnings("ResourceType")
    @Test
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

        when(manager.getActiveNetworkInfo()).thenReturn(null);
        assertFalse(AndroidUtil.hasInternetConnection(context));
    }

    @SuppressWarnings("ResourceType")
    @Test
    public void testIsServiceRunning() throws Exception {
        Context context = mock(Context.class);

        List<RunningServiceInfo> runningServices = new ArrayList<>();

        ActivityManager manager = mock(ActivityManager.class);
        when(manager.getRunningServices(anyInt())).thenReturn(runningServices);

        when(context.getSystemService(eq(Context.ACTIVITY_SERVICE))).thenReturn(manager);

        RunningServiceInfo info = new RunningServiceInfo();
        info.service = new ComponentName(context, CallProcessorService.class.getName());
        runningServices.add(info);

        assertTrue(isServiceRunning(context, CallProcessorService.class));

        runningServices.clear();

        assertFalse(isServiceRunning(context, CallProcessorService.class));
    }

    @Test
    public void testPutPreferencesOptional() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("BOOL", true);
        data.put("NUM", 1.1);
        data.put("DOUBLE", 2.2d);
        data.put("FLOAT", 3.3f);
        data.put("LONG", 10L);
        data.put("INT", 20);
        data.put("BYTE", 30);
        data.put("SET", Util.asSet("A", "B", "C"));
        data.put("STRING", "Hello");

        SharedPreferences preferences = getContext().getSharedPreferences("test", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

        AndroidUtil.putPreferencesOptional(preferences, data);

        assertEquals(data.size(), preferences.getAll().size());
        assertEquals(true, preferences.getBoolean("BOOL", false));
        assertEquals(1.1f, preferences.getFloat("NUM", 0));
        assertEquals(2.2f, preferences.getFloat("DOUBLE", 0));
        assertEquals(3.3f, preferences.getFloat("FLOAT", 0));
        assertEquals(10L, preferences.getLong("LONG", 0));
        assertEquals(20, preferences.getInt("INT", 0));
        assertEquals(30, preferences.getInt("BYTE", 0));
        assertEquals(Util.asSet("A", "B", "C"), preferences.getStringSet("SET", null));
        assertEquals("Hello", preferences.getString("STRING", null));

        data.put("BOOL", false);
        data.put("NUM", 10.1);
        data.put("DOUBLE", 20.2d);
        data.put("FLOAT", 30.3f);
        data.put("LONG", 100L);
        data.put("INT", 200);
        data.put("BYTE", 300);
        data.put("SET", Util.asSet("A", "B", "C", "D"));
        data.put("STRING", "Bye");

        assertEquals(data.size(), preferences.getAll().size());
        assertEquals(true, preferences.getBoolean("BOOL", false));
        assertEquals(1.1f, preferences.getFloat("NUM", 0));
        assertEquals(2.2f, preferences.getFloat("DOUBLE", 0));
        assertEquals(3.3f, preferences.getFloat("FLOAT", 0));
        assertEquals(10L, preferences.getLong("LONG", 0));
        assertEquals(20, preferences.getInt("INT", 0));
        assertEquals(30, preferences.getInt("BYTE", 0));
        assertEquals(Util.asSet("A", "B", "C"), preferences.getStringSet("SET", null));
        assertEquals("Hello", preferences.getString("STRING", null));
    }

}