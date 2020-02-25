package com.bopr.android.smailer.util

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.ui.WavyUnderlineSpan
import com.bopr.android.smailer.util.UiUtil.accentedText
import com.bopr.android.smailer.util.UiUtil.underwivedText
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * [AndroidUtil] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class UiUtilTest : BaseTest() {

    /**
     * Tests [UiUtil.underwivedText] method.
     */
    @Test
    fun testUnderwivedText() {
        val spannable = underwivedText(targetContext, "Invalid text")
        assertThat(spannable, instanceOf<Any>(SpannableString::class.java))

        val span = spannable.getSpans(0, spannable.length, Any::class.java)[0]
        assertThat(span, instanceOf(WavyUnderlineSpan::class.java))
    }

    /**
     * Tests [UiUtil.accentedText] method.
     */
    @Test
    fun testAccentedTextText() {
        val spannable = accentedText(targetContext, "Invalid text")
        assertThat(spannable, instanceOf<Any>(SpannableString::class.java))

        val span = spannable.getSpans(0, spannable.length, Any::class.java)[0]
        assertThat(span, instanceOf(ForegroundColorSpan::class.java))
    }

}