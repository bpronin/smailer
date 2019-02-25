package com.bopr.android.smailer.util;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.GeoCoordinates;

import org.junit.Test;

import java.util.Locale;
import java.util.Set;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * {@link Util} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class UtilTest extends BaseTest {

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
        assertTrue(!Util.isEmpty("A"));
        assertTrue(Util.isEmpty(""));
        assertTrue(Util.isEmpty((String) null));
    }

    @Test
    public void testIsTrimEmpty() {
        assertTrue(!Util.isTrimEmpty("A"));
        assertTrue(Util.isTrimEmpty(""));
        assertTrue(Util.isTrimEmpty(null));
        assertTrue(!Util.isTrimEmpty(" A "));
        assertTrue(Util.isTrimEmpty("    "));
    }

    @Test
    public void testIsAllEmpty() {
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
    public void testIsAnyEmpty() {
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
    public void testStringOf() {
        assertEquals("1, 2, 3", Util.join(", ", 1, 2, 3));
        assertEquals("1, null, null", Util.join(", ", 1, null, null));
        assertEquals("", Util.join(", "));
    }

    @Test
    public void testListOf() {
        assertArrayEquals(new String[]{"1", " 2", "3 "}, Util.split("1, 2,3 ", ",", false).toArray());
        assertArrayEquals(new String[]{"1", "2", "3"}, Util.split("1, 2, 3 ", ",", true).toArray());
        assertArrayEquals(new String[]{" "}, Util.split(" ", ",", false).toArray());
        assertArrayEquals(new String[]{}, Util.split("", ",", true).toArray());
        assertArrayEquals(new String[]{}, Util.split(" ", ",", true).toArray());
        assertArrayEquals(new String[]{}, Util.split(null, ",", true).toArray());
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
    public void testToArray() {
        Set<String> set = Util.asSet("A", "B", "C");
        String[] strings = Util.toArray(set);
        assertArrayEquals(strings, new String[]{"A", "B", "C"});
    }

    @Test
    public void testStringToLocale() {
        assertEquals(new Locale("ru", "RU"), Util.stringToLocale("ru_RU"));
        assertEquals(Locale.getDefault(), Util.stringToLocale("default"));
        assertNull(Util.stringToLocale(""));
        assertNull(Util.stringToLocale(null));
    }

    @Test
    public void testLocaleToString() {
        assertEquals("ru_RU", Util.localeToString(new Locale("ru", "RU")));
        assertEquals("default", Util.localeToString(Locale.getDefault()));
        assertNull(Util.localeToString(null));
    }


}