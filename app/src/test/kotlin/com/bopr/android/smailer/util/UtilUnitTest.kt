package com.bopr.android.smailer.util

import com.bopr.android.smailer.GeoCoordinates
import com.bopr.android.smailer.util.Util.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * [Util] tester.
 */
class UtilUnitTest {

    @Test
    fun testFormatLocation() {
        assertEquals("30d33m59sn, 60d33m59sw", formatLocation(GeoCoordinates(30.5664, 60.5664),
                "d", "m", "s", "n", "s", "w", "e"))
    }

    @Test
    fun testFormatLocation1() {
        assertEquals("30°33'59\"N, 60°33'59\"W", formatLocation(GeoCoordinates(30.5664, 60.5664)))
        assertEquals("30°33'59\"S, 60°33'59\"E", formatLocation(GeoCoordinates(-30.5664, -60.5664)))
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
        val duration = TimeUnit.HOURS.toMillis(15) + TimeUnit.MINUTES.toMillis(15) + TimeUnit.SECONDS.toMillis(15)
        assertEquals("15:15:15", formatDuration(duration))
    }

    @Test
    fun testIsEmpty() {
        assertFalse(isEmpty("A"))
        assertTrue(isEmpty(""))
        assertTrue(isEmpty(null as String?))
    }

    @Test
    fun testIsTrimEmpty() {
        assertFalse(isTrimEmpty("A"))
        assertTrue(isTrimEmpty("  "))
        assertTrue(isTrimEmpty(null))
    }

    @Test
    fun testIsAllEmpty() {
        assertFalse(allIsEmpty("A", "B", "C"))
        assertFalse(allIsEmpty("", "B", "C"))
        assertFalse(allIsEmpty("A", "", "C"))
        assertTrue(allIsEmpty("", "", ""))
        assertFalse(allIsEmpty(null, "B", "C"))
        assertFalse(allIsEmpty("A", null, "C"))
        assertTrue(allIsEmpty("", null, null))
        assertTrue(allIsEmpty(null, null, null))
    }

    @Test
    fun testIsAnyEmpty() {
        assertFalse(anyIsEmpty("A", "B", "C"))
        assertTrue(anyIsEmpty("", "B", "C"))
        assertTrue(anyIsEmpty("A", "", "C"))
        assertTrue(anyIsEmpty("", "", ""))
        assertTrue(anyIsEmpty(null, "B", "C"))
        assertTrue(anyIsEmpty("A", null, "C"))
        assertTrue(anyIsEmpty("", null, null))
        assertTrue(anyIsEmpty(null, null, null))
    }

    @Test
    fun testStringOf() {
        assertEquals("1, 2, 3", join(", ", 1, 2, 3))
        assertEquals("1, null, null", join(", ", 1, null, null))
        assertEquals("", join<Any>(", "))
    }

    @Test
    fun testParseSeparated() {
        assertArrayEquals(arrayOf("1", " 2", "3 "), split("1, 2,3 ", ",", false).toTypedArray())
        assertArrayEquals(arrayOf("1", "2", "3"), split("1, 2, 3 ", ",", true).toTypedArray())
        assertArrayEquals(arrayOf(" "), split(" ", ",", false).toTypedArray())
        assertArrayEquals(arrayOf<String>(), split("", ",", true).toTypedArray())
        assertArrayEquals(arrayOf<String>(), split(" ", ",", true).toTypedArray())
        assertArrayEquals(arrayOf<String>(), split(null, ",", true).toTypedArray())
    }

    @Test
    fun testAsSet() {
        val set = asSet("A", "B", "B", "C")
        assertEquals(3, set.size.toLong())
        assertTrue(set.contains("A"))
        assertTrue(set.contains("B"))
        assertTrue(set.contains("C"))
        try {
            set.add("D")
            fail("No exception")
        } catch (x: UnsupportedOperationException) { /* ok */
        }
    }

    @Test
    fun testLocaleToString() {
        assertEquals("ru_RU", localeToString(Locale("ru", "RU")))
        assertNull(localeToString(null))
        assertEquals("default", localeToString(Locale.getDefault()))
    }

    @Test
    fun testStringToLocale() {
        assertEquals(Locale("ru", "RU"), stringToLocale("ru_RU"))
        assertEquals(Locale.getDefault(), stringToLocale("default"))
        assertNull(stringToLocale(null))
    }
}