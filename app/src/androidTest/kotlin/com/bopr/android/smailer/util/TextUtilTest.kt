package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.google.api.client.testing.util.TestableByteArrayInputStream
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

@SmallTest
class TextUtilTest : BaseTest() {

    @Test
    fun testQuoteRegex() {
        assertEquals("REGEX:(.*)", escapeRegex("(.*)"))
    }

    @Test
    fun testUnquoteRegex() {
        assertEquals("(.*)", unescapeRegex("REGEX:(.*)"))
        assertNull(unescapeRegex("(.*)"))
        assertNull(unescapeRegex(""))
    }

    @Test
    fun testIsQuoted() {
        assertTrue(isQuoted("\"hello\""))
        assertTrue(isQuoted("\"\"hello\"\""))
        assertTrue(isQuoted("\"\"hello\""))
        assertFalse(isQuoted("\"hello\" hello"))
        assertFalse(isQuoted("\"hello"))
        assertFalse(isQuoted("hello\""))
        assertFalse(isQuoted("hello"))
        assertFalse(isQuoted(""))
        assertFalse(isQuoted("  "))
        assertFalse(isQuoted(null))
    }

    @Test
    fun testCommaJoin() {
        assertEquals("1,2,3", commaJoin(listOf(1, 2, 3)))
        assertEquals("a,b,c", commaJoin(listOf("a", "b", "c")))
        assertEquals("a,a/,b,c", commaJoin(listOf("a", "a,b", "c")))
        assertEquals("", commaJoin(listOf<String>()))
    }

    @Test
    fun testCommaSplit() {
        assertEquals(listOf("1", "2", "3"), commaSplit(" 1, 2, 3 "))
        assertEquals(listOf("a", "b", "c"), commaSplit("a,b,c"))
        assertEquals(listOf("a", "b", "a,b", "c"), commaSplit("a, b, a/,b, c"))
        assertEquals(listOf<String>(), commaSplit(""))
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
        val stream = TestableByteArrayInputStream("ABC".toByteArray())
        assertEquals("ABC", readStream(stream))
        assertTrue(stream.isClosed)
        assertEquals("", readStream(ByteArrayInputStream("".toByteArray())))
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

    @Test
    fun testEscapeRegex() {
        assertEquals("REGEX:text", escapeRegex("text"))
        assertEquals("REGEX:", escapeRegex(""))
        assertEquals("REGEX:   ", escapeRegex("   "))
        assertEquals("REGEX:REGEX:", escapeRegex("REGEX:"))
    }

    @Test
    fun testUnescapeRegex() {
        assertNull(unescapeRegex(null))
        assertNull(unescapeRegex(""))
        assertNull(unescapeRegex("   "))
        assertNull(unescapeRegex("text"))
        assertEquals("text", unescapeRegex("REGEX:text"))
        assertEquals("REGEX:", unescapeRegex("REGEX:REGEX:"))
        assertEquals("", unescapeRegex("REGEX:"))
    }

}