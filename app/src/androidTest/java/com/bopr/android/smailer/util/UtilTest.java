package com.bopr.android.smailer.util;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.GeoCoordinates;

import org.junit.Test;

import java.util.Locale;
import java.util.Set;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertArrayEquals;

/**
 * {@link Util} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class UtilTest extends BaseTest {

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
        assertTrue(!Util.isEmpty("A"));
        assertTrue(Util.isEmpty(""));
        assertTrue(Util.isEmpty(null));
    }

    @Test
    public void testIsTrimEmpty() throws Exception {
        assertTrue(!Util.isTrimEmpty("A"));
        assertTrue(Util.isTrimEmpty(""));
        assertTrue(Util.isTrimEmpty(null));
        assertTrue(!Util.isTrimEmpty(" A "));
        assertTrue(Util.isTrimEmpty("    "));
    }

    @Test
    public void testIsAllEmpty() throws Exception {
        assertTrue(!Util.allIsEmpty("A", "B", "C"));
        assertTrue(!Util.allIsEmpty("", "B", "C"));
        assertTrue(!Util.allIsEmpty("A", "", "C"));
        assertTrue(Util.allIsEmpty("", "", ""));
        assertTrue(!Util.allIsEmpty(null, "B", "C"));
        assertTrue(!Util.allIsEmpty("A", null, "C"));
        assertTrue(Util.allIsEmpty("", null, null));
        assertTrue(Util.allIsEmpty(null, null, null));
    }

    @Test
    public void testIsAnyEmpty() throws Exception {
        assertTrue(!Util.anyIsEmpty("A", "B", "C"));
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
        assertEquals("1, 2, 3", Util.separated(", ", 1, 2, 3));
        assertEquals("1, null, null", Util.separated(", ", 1, null, null));
        assertEquals("", Util.separated(", "));
    }

    @Test
    public void testListOf() throws Exception {
        assertArrayEquals(new String[]{"1", " 2", "3 "}, Util.parseSeparated("1, 2,3 ", ",", false).toArray());
        assertArrayEquals(new String[]{"1", "2", "3"}, Util.parseSeparated("1, 2, 3 ", ",", true).toArray());
        assertArrayEquals(new String[]{" "}, Util.parseSeparated(" ", ",", false).toArray());
        assertArrayEquals(new String[]{}, Util.parseSeparated("", ",", true).toArray());
        assertArrayEquals(new String[]{}, Util.parseSeparated(" ", ",", true).toArray());

        try {
            assertArrayEquals(new String[]{}, Util.parseSeparated(null, ",", true).toArray());
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
    public void testToArray() throws Exception {
        Set<String> set = Util.asSet("A", "B", "C");
        String[] strings = Util.toArray(set);
        assertArrayEquals(strings, new String[]{"A", "B", "C"});
    }

    @Test
    public void testStringToLocale() throws Exception {
        assertEquals(new Locale("ru", "RU"), Util.stringToLocale("ru_RU"));
        assertEquals(Locale.getDefault(), Util.stringToLocale("default"));
        assertNull(Util.stringToLocale(""));
        assertNull(Util.stringToLocale(null));
    }

    @Test
    public void testLocaleToString() throws Exception {
        assertEquals("ru_RU", Util.localeToString(new Locale("ru", "RU")));
        assertEquals("default", Util.localeToString(Locale.getDefault()));
        assertNull(Util.localeToString(null));
    }

}