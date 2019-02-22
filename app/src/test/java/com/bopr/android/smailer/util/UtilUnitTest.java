package com.bopr.android.smailer.util;

import com.bopr.android.smailer.GeoCoordinates;

import org.junit.Test;

import java.util.Locale;
import java.util.Set;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link Util} tester.
 */
public class UtilUnitTest {

    @Test
    public void testFormatLocation() {
        assertEquals("30d33m59sn, 60d33m59sw", Util.formatLocation(new GeoCoordinates(30.5664, 60.5664),
                "d", "m", "s", "n", "s", "w", "e"));
    }

    @Test
    public void testFormatLocation1() {
        assertEquals("30°33'59\"N, 60°33'59\"W", Util.formatLocation(new GeoCoordinates(30.5664, 60.5664)));
        assertEquals("30°33'59\"S, 60°33'59\"E", Util.formatLocation(new GeoCoordinates(-30.5664, -60.5664)));
    }

    @Test
    public void testDecimalToDMS() {
        assertEquals("90D33M59S", Util.decimalToDMS(90.5664, "D", "M", "S"));
    }

    @Test
    public void testCapitalize() {
        assertEquals("Hello", Util.capitalize("hello"));
        assertEquals("Hello", Util.capitalize("Hello"));
        assertEquals("", Util.capitalize(""));
        assertNull(Util.capitalize(null));
    }

    @Test
    public void testFormatDuration() {
        long duration = HOURS.toMillis(15) + MINUTES.toMillis(15) + SECONDS.toMillis(15);
        assertEquals("15:15:15", Util.formatDuration(duration));
    }

    @Test
    public void testIsEmpty() {
        assertFalse(Util.isEmpty("A"));
        assertTrue(Util.isEmpty(""));
        assertTrue(Util.isEmpty((String) null));
    }

    @Test
    public void testIsTrimEmpty() {
        assertFalse(Util.isTrimEmpty("A"));
        assertTrue(Util.isTrimEmpty("  "));
        assertTrue(Util.isTrimEmpty(null));
    }

    @Test
    public void testIsAllEmpty() {
        assertFalse(Util.allIsEmpty("A", "B", "C"));
        assertFalse(Util.allIsEmpty("", "B", "C"));
        assertFalse(Util.allIsEmpty("A", "", "C"));
        assertTrue(Util.allIsEmpty("", "", ""));
        assertFalse(Util.allIsEmpty(null, "B", "C"));
        assertFalse(Util.allIsEmpty("A", null, "C"));
        assertTrue(Util.allIsEmpty("", null, null));
        assertTrue(Util.allIsEmpty(null, null, null));
    }

    @Test
    public void testIsAnyEmpty() {
        assertFalse(Util.anyIsEmpty("A", "B", "C"));
        assertTrue(Util.anyIsEmpty("", "B", "C"));
        assertTrue(Util.anyIsEmpty("A", "", "C"));
        assertTrue(Util.anyIsEmpty("", "", ""));
        assertTrue(Util.anyIsEmpty(null, "B", "C"));
        assertTrue(Util.anyIsEmpty("A", null, "C"));
        assertTrue(Util.anyIsEmpty("", null, null));
        assertTrue(Util.anyIsEmpty(null, null, null));
    }

    @Test
    public void testStringOf() {
        assertEquals("1, 2, 3", Util.separated(", ", 1, 2, 3));
        assertEquals("1, null, null", Util.separated(", ", 1, null, null));
        assertEquals("", Util.separated(", "));
    }

    @Test
    public void testParseSeparated() {
        assertArrayEquals(new String[]{"1", " 2", "3 "}, Util.parseSeparated("1, 2,3 ", ",", false).toArray());
        assertArrayEquals(new String[]{"1", "2", "3"}, Util.parseSeparated("1, 2, 3 ", ",", true).toArray());
        assertArrayEquals(new String[]{" "}, Util.parseSeparated(" ", ",", false).toArray());
        assertArrayEquals(new String[]{}, Util.parseSeparated("", ",", true).toArray());
        assertArrayEquals(new String[]{}, Util.parseSeparated(" ", ",", true).toArray());
        assertArrayEquals(new String[]{}, Util.parseSeparated(null, ",", true).toArray());
    }

    @Test
    public void testAsSet() {
        Set<String> set = Util.asSet("A", "B", "B", "C");

        assertEquals(3, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertTrue(set.contains("C"));

        try {
            set.add("D");
            fail("No exception");
        } catch (UnsupportedOperationException x) {
            /* ok */
        }
    }

    @Test
    public void testLocaleToString() {
        assertEquals("ru_RU", Util.localeToString(new Locale("ru", "RU")));
        assertNull(Util.localeToString(null));
        assertEquals("default", Util.localeToString(Locale.getDefault()));
    }

    @Test
    public void testStringToLocale() {
        assertEquals(new Locale("ru", "RU"), Util.stringToLocale("ru_RU"));
        assertEquals(Locale.getDefault(), Util.stringToLocale("default"));
        assertNull(Util.stringToLocale(null));
    }

    @Test
    public void testNormalizePhone() {
        assertEquals("123", Util.normalizePhone("123"));
        assertEquals("123456HELLO", Util.normalizePhone("+1 234-56-(HeLl*.O)"));
        assertNotEquals("BCS Online", Util.normalizePhone("Beeline"));
    }

    @Test
    public void testNormalizePhonePattern() {
        assertEquals("123456", Util.normalizePhonePattern("123456"));
        assertEquals("123456(.*)(.*)56", Util.normalizePhonePattern("123456 * *56"));
        assertEquals("1(.*)3456HE(.*)O", Util.normalizePhonePattern("+1 * 34-56-(He*o)"));
    }

    @Test
    public void testPhoneMatches() {
        assertTrue(Util.normalizePhone("1234").matches(Util.normalizePhonePattern("1234")));
        assertTrue(Util.normalizePhone("+1 2 34-56-(Hello)").matches(Util.normalizePhonePattern("+1*34-56-(HE**O)")));
        assertTrue(Util.normalizePhone("*1 2 34-56-*Hello*").matches(Util.normalizePhonePattern("1*34-56-(HE * O)")));
    }

}