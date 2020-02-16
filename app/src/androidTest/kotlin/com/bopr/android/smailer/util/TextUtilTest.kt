package com.bopr.android.smailer.util

import com.bopr.android.smailer.util.TextUtil.capitalize
import com.bopr.android.smailer.util.TextUtil.commaJoin
import com.bopr.android.smailer.util.TextUtil.commaSplit
import com.bopr.android.smailer.util.TextUtil.decimalToDMS
import com.bopr.android.smailer.util.TextUtil.formatDuration
import com.bopr.android.smailer.util.TextUtil.isQuoted
import com.bopr.android.smailer.util.TextUtil.isValidEmailAddress
import com.bopr.android.smailer.util.TextUtil.isValidEmailAddressList
import com.bopr.android.smailer.util.TextUtil.join
import com.bopr.android.smailer.util.TextUtil.quoteRegex
import com.bopr.android.smailer.util.TextUtil.readStream
import com.bopr.android.smailer.util.TextUtil.split
import com.bopr.android.smailer.util.TextUtil.unquoteRegex
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

class TextUtilTest {

    @Test
    fun testQuoteRegex() {
        assertEquals("REGEX:(.*)", quoteRegex("(.*)"))
    }

    @Test
    fun testUnquoteRegex() {
        assertEquals("(.*)", unquoteRegex("REGEX:(.*)"))
        assertNull(unquoteRegex("(.*)"))
        assertNull(unquoteRegex(""))
    }

    @Test
    fun testIsQuoted() {
        assertTrue(isQuoted("\"hello\""))
//        assertTrue(isQuoted("\"\"hello\"\""))
        assertFalse(isQuoted("\"hello"))
        assertFalse(isQuoted("hello\""))
        assertFalse(isQuoted("hello"))
        assertFalse(isQuoted(""))
        assertFalse(isQuoted(null))
    }

    @Test
    fun testJoin() {
        assertEquals("1-2-3", join(listOf(1, 2, 3), "-"))
        assertEquals("a, b, c", join(listOf("a", "b", "c"), ", "))
        assertEquals(",,", join(listOf("", "", ""), ","))
        assertEquals("", join(listOf<String>(), ","))
    }

    @Test
    fun testSplit() {
        assertEquals(listOf("1", " 2", "3 "), split("1, 2,3 ", ",", false))
        assertEquals(listOf("1", "2", "3"), split("1, 2, 3 ", ",", true))
        assertEquals(listOf(" "), split(" ", ",", false))
        assertEquals(listOf<String>(), split("", ",", true))
        assertEquals(listOf<String>(), split(" ", ",", true))
    }

    @Test
    fun testCommaJoin() {
        assertEquals("1,2,3", commaJoin(listOf(1, 2, 3)))
        assertEquals("a,b,c", commaJoin(listOf("a", "b", "c")))
    }

    @Test
    fun testCommaSplit() {
        assertEquals(listOf("1", "2", "3"), commaSplit(" 1, 2, 3 "))
        assertEquals(listOf("a", "b", "c"), commaSplit("a,b,c"))
    }


    @Test
    fun testDecimalToDMS() {
        assertEquals("90D33M59S", decimalToDMS(90.5664, "D", "M", "S"))
    }

    @Test
    fun testCapitalize() {
        assertEquals("Hello", capitalize("hello"))
        assertEquals("Hello", capitalize("Hello"))
        assertEquals("", capitalize(""))
        assertNull(capitalize(null))
    }

    @Test
    fun testFormatDuration() {
        val duration = TimeUnit.HOURS.toMillis(15) + TimeUnit.MINUTES.toMillis(16) + TimeUnit.SECONDS.toMillis(17)
        assertEquals("15:16:17", formatDuration(duration))
    }

    @Test
    fun testReadStream() {
        assertEquals("ABC", readStream(ByteArrayInputStream("ABC".toByteArray())))
        assertEquals("", readStream(ByteArrayInputStream("".toByteArray())))
        assertNull(readStream(null))
    }

    @Test
    fun testValidEmailAddress() {
        assertTrue(isValidEmailAddress("bob@mail.com"))
        assertFalse(isValidEmailAddress("bobmail.com"))
        assertFalse(isValidEmailAddress(""))
        assertFalse(isValidEmailAddress(null))
        assertTrue(isValidEmailAddressList("anna@mail.com, bob@mail.com"))
        assertFalse(isValidEmailAddressList("annamail.com, bobmail.com"))
        assertFalse(isValidEmailAddressList(",,,"))
        assertFalse(isValidEmailAddressList(""))
        assertFalse(isValidEmailAddressList(null))
    }
}