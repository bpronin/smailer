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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link Util} tester.
 */
public class UtilUnitTest {

    @Test
    public void testFormatLocation() throws Exception {
        assertEquals("30d33m59sn, 60d33m59sw", Util.formatLocation(new GeoCoordinates(30.5664, 60.5664),
                "d", "m", "s", "n", "s", "w", "e"));
    }

    @Test
    public void testFormatLocation1() throws Exception {
        assertEquals("30°33'59\"N, 60°33'59\"W", Util.formatLocation(new GeoCoordinates(30.5664, 60.5664)));
        assertEquals("30°33'59\"S, 60°33'59\"E", Util.formatLocation(new GeoCoordinates(-30.5664, -60.5664)));
    }

    @Test
    public void testDecimalToDMS() throws Exception {
        assertEquals("90D33M59S", Util.decimalToDMS(90.5664, "D", "M", "S"));
    }

    @Test
    public void testCapitalize() throws Exception {
        assertEquals("Hello", Util.capitalize("hello"));
        assertEquals("Hello", Util.capitalize("Hello"));
        assertEquals("", Util.capitalize(""));
        assertEquals(null, Util.capitalize(null));
    }

    @Test
    public void testFormatDuration() throws Exception {
        long duration = HOURS.toMillis(15) + MINUTES.toMillis(15) + SECONDS.toMillis(15);
        assertEquals("15:15:15", Util.formatDuration(duration));
    }

    @Test
    public void testIsEmpty() throws Exception {
        assertFalse(Util.isEmpty("A"));
        assertTrue(Util.isEmpty(""));
        assertTrue(Util.isEmpty(null));
    }

    @Test
    public void testIsTrimEmpty() throws Exception {
        assertFalse(Util.isTrimEmpty("A"));
        assertTrue(Util.isTrimEmpty("  "));
        assertTrue(Util.isTrimEmpty(null));
    }

    @Test
    public void testIsAllEmpty() throws Exception {
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
    public void testIsAnyEmpty() throws Exception {
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
    public void testStringOf() throws Exception {
        assertEquals("1, 2, 3", Util.stringOf(", ", 1, 2, 3));
        assertEquals("1, null, null", Util.stringOf(", ", 1, null, null));
        assertEquals("", Util.stringOf(", "));
    }

    @Test
    public void testListOf() throws Exception {
        assertArrayEquals(new String[]{"1", " 2", "3 "}, Util.listOf("1, 2,3 ", ",", false).toArray());
        assertArrayEquals(new String[]{"1", "2", "3"}, Util.listOf("1, 2, 3 ", ",", true).toArray());
        assertArrayEquals(new String[]{" "}, Util.listOf(" ", ",", false).toArray());
        assertArrayEquals(new String[]{}, Util.listOf("", ",", true).toArray());
        assertArrayEquals(new String[]{}, Util.listOf(" ", ",", true).toArray());

        try {
            assertArrayEquals(new String[]{}, Util.listOf(null, ",", true).toArray());
            fail("No exception");
        } catch (NullPointerException x) {
            /* ok */
        }
    }

    @Test
    public void testAsSet() throws Exception {
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
    public void testLocaleToString() throws Exception {
        assertEquals("ru_RU", Util.localeToString(new Locale("ru", "RU")));
        assertEquals(null, Util.localeToString(null));
        assertEquals("default", Util.localeToString(Locale.getDefault()));
    }

    @Test
    public void testStringToLocale() throws Exception {
        assertEquals(new Locale("ru", "RU"), Util.stringToLocale("ru_RU"));
        assertEquals(Locale.getDefault(), Util.stringToLocale("default"));
        assertEquals(null, Util.stringToLocale(null));
    }

}