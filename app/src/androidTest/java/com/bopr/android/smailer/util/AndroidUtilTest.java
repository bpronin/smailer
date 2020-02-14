package com.bopr.android.smailer.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;

/**
 * {@link AndroidUtil} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AndroidUtilTest extends BaseTest {

    /**
     * Tests {@link UiUtil#underwivedText(Context, String)} method.
     */
    @Test
    public void testUnderwivedText() {
        Spannable spannable = UiUtil.underwivedText(getContext(), "Invalid text");

        assertThat(spannable, instanceOf(SpannableString.class));
        Object span = spannable.getSpans(0, spannable.length(), Object.class)[0];
        assertThat(span, instanceOf(WavyUnderlineSpan.class));

        spannable = UiUtil.underwivedText(getContext(), "Invalid text");
        assertThat(spannable, instanceOf(SpannableString.class));
        Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
        assertThat(spans, emptyArray());
    }

    /**
     * Tests {@link UiUtil#accentedText(Context, String)} method.
     */
    @Test
    public void testAccentedTextText() {
        Spannable spannable = UiUtil.accentedText(getContext(), "Invalid text");
        assertThat(spannable, instanceOf(SpannableString.class));
        Object span = spannable.getSpans(0, spannable.length(), Object.class)[0];
        assertThat(span, instanceOf(ForegroundColorSpan.class));

        spannable = UiUtil.accentedText(getContext(), "Invalid text");
        assertThat(spannable, instanceOf(SpannableString.class));
        Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
        assertThat(spans, emptyArray());
    }

//    /**
//     * Tests {@link AndroidUtil#hasInternetConnection(Context)}} method.
//     */
//    @SuppressWarnings("ResourceType")
//    @Test
//    public void testHasInternetConnection() {
//        NetworkInfo info = mock(NetworkInfo.class);
//
//        ConnectivityManager manager = mock(ConnectivityManager.class);
//        when(manager.getActiveNetworkInfo()).thenReturn(info);
//
//        Context context = mock(Context.class);
//        when(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE))).thenReturn(manager);
//
//        when(info.isConnected()).thenReturn(true);
//        assertTrue(AndroidUtil.hasInternetConnection(context));
//
//        when(info.isConnected()).thenReturn(false);
//        assertFalse(AndroidUtil.hasInternetConnection(context));
//
//        when(manager.getActiveNetworkInfo()).thenReturn(null);
//        assertFalse(AndroidUtil.hasInternetConnection(context));
//    }

}