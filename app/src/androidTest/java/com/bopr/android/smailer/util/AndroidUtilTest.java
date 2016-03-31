package com.bopr.android.smailer.util;

import android.text.Spannable;
import android.text.SpannableString;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

/**
 * {@link AndroidUtil} tester.
 */
public class AndroidUtilTest extends BaseTest {

    public void testValidateText() throws Exception {
        Spannable spannable = AndroidUtil.validatedText(getContext(), "Invalid text", false);
        assertTrue(spannable instanceof SpannableString);
        Object span = spannable.getSpans(0, spannable.length(), Object.class)[0];
        assertTrue(span instanceof WavyUnderlineSpan);
    }

}