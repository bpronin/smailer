package com.bopr.android.smailer.util

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.WavyUnderlineSpan
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * AndroidUtil tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class UiUtilTest : BaseTest() {

    @Test
    fun testUnderwivedText() {
        val spannable = targetContext.underwivedText("Invalid text")
        assertThat(spannable, instanceOf<Any>(SpannableString::class.java))

        val span = spannable.getSpans(0, spannable.length, Any::class.java)[0]
        assertThat(span, instanceOf(WavyUnderlineSpan::class.java))
    }

    @Test
    fun testAccentedTextText() {
        val spannable = targetContext.accentedText("Invalid text")
        assertThat(spannable, instanceOf<Any>(SpannableString::class.java))

        val span = spannable.getSpans(0, spannable.length, Any::class.java)[0]
        assertThat(span, instanceOf(ForegroundColorSpan::class.java))
    }

    @Test
    fun testQuantityString() {
        val configuration = targetContext.resources.configuration
        configuration.setLocale(Locale("ru", "ru"))
        val res = targetContext.createConfigurationContext(configuration).resources

        assertEquals("Нет сообщений", res.quantityString(R.plurals.mail_items, R.string.mail_items_zero, 0))
        assertEquals("Получено 1 сообщение", res.quantityString(R.plurals.mail_items, R.string.mail_items_zero, 1))
        assertEquals("Получено 2 сообщения", res.quantityString(R.plurals.mail_items, R.string.mail_items_zero, 2))
        assertEquals("Получено 10 сообщений", res.quantityString(R.plurals.mail_items, R.string.mail_items_zero, 10))
    }

}