package com.bopr.android.smailer;

import com.bopr.android.smailer.util.Util;

import java.util.Locale;
import java.util.Set;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertArrayEquals;

/**
 * {@link Util} tester.
 */
public class UtilTest extends BaseTest {

    public void testFormatLocation() throws Exception {
        assertEquals("30d33m59sn, 60d33m59sw", Util.formatLocation(new GeoCoordinates(30.5664, 60.5664), "d",
                "m",
                "s", "n", "s",
                "w",
                "e"));
    }

    public void testFormatLocation1() throws Exception {
        assertEquals("30°33'59\"N, 60°33'59\"W", Util.formatLocation(new GeoCoordinates(30.5664, 60.5664)));
        assertEquals("30°33'59\"S, 60°33'59\"E", Util.formatLocation(new GeoCoordinates(-30.5664, -60.5664)));
    }

    public void testDecimalToDMS() throws Exception {
        assertEquals("90D33M59S", Util.decimalToDMS(90.5664, "D", "M", "S"));
    }

    public void testCapitalize() throws Exception {
        assertEquals("Hello", Util.capitalize("hello"));
        assertEquals("Hello", Util.capitalize("Hello"));
    }

    public void testFormatDuration() throws Exception {
        long duration = HOURS.toMillis(15) + MINUTES.toMillis(15) + SECONDS.toMillis(15);
        assertEquals("15:15:15", Util.formatDuration(duration));
    }

    public void testIsEmpty() throws Exception {
        assertTrue(!Util.isEmpty("A"));
        assertTrue(Util.isEmpty(""));
        assertTrue(Util.isEmpty(null));
    }

    public void testIsAllEmpty() throws Exception {
        assertTrue(!Util.isAllEmpty("A", "B", "C"));
        assertTrue(!Util.isAllEmpty("", "B", "C"));
        assertTrue(!Util.isAllEmpty("A", "", "C"));
        assertTrue(Util.isAllEmpty("", "", ""));
        assertTrue(!Util.isAllEmpty(null, "B", "C"));
        assertTrue(!Util.isAllEmpty("A", null, "C"));
        assertTrue(Util.isAllEmpty("", null, null));
        assertTrue(Util.isAllEmpty(null, null, null));
    }

    public void testIsAnyEmpty() throws Exception {
        assertTrue(!Util.isAnyEmpty("A", "B", "C"));
        assertTrue(Util.isAnyEmpty("", "B", "C"));
        assertTrue(Util.isAnyEmpty("A", "", "C"));
        assertTrue(Util.isAnyEmpty("", "", ""));
        assertTrue(Util.isAnyEmpty(null, "B", "C"));
        assertTrue(Util.isAnyEmpty("A", null, "C"));
        assertTrue(Util.isAnyEmpty("", null, null));
        assertTrue(Util.isAnyEmpty(null, null, null));
    }

    public void testStringOf() throws Exception {
        assertEquals("1, 2, 3", Util.stringOf(", ", 1, 2, 3));
        assertEquals("1, null, null", Util.stringOf(", ", 1, null, null));
        assertEquals("", Util.stringOf(", "));
    }

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

    public void testLocale() throws Exception {
        assertEquals(new Locale("ru", "RU"), Util.stringToLocale("ru_RU"));
        assertEquals("ru_RU", Util.localeToString(new Locale("ru", "RU")));
    }

}