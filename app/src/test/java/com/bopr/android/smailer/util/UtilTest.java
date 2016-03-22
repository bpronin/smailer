package com.bopr.android.smailer.util;

import org.junit.Test;

import java.util.Locale;
import java.util.Set;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link Util} tester.
 */
public class UtilTest {

    @Test
    public void testFormatLocation() throws Exception {
        assertEquals("30d33m59sn, 60d33m59sw", Util.formatLocation(30.5664, 60.5664, "d", "m", "s", "n", "s", "w", "e"));
    }

    @Test
    public void testFormatLocation1() throws Exception {
        assertEquals("30°33'59\"N, 60°33'59\"W", Util.formatLocation(30.5664, 60.5664));
    }

    @Test
    public void testDecimalToDMS() throws Exception {
        assertEquals("90D33M59S", Util.decimalToDMS(90.5664, "D", "M", "S"));
    }

    @Test
    public void testCapitalize() throws Exception {
        assertEquals("Hello", Util.capitalize("hello"));
        assertEquals("Hello", Util.capitalize("Hello"));
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

    @Test
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

    @Test
    public void testListOf() throws Exception {
        assertEquals("1, 2, 3", Util.listOf(", ", 1, 2, 3));
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
            fail();
        } catch (UnsupportedOperationException x) {
            /* ok */
        }
    }

    @Test
    public void testLocale() throws Exception {
        assertEquals(new Locale("ru", "RU"), Util.stringToLocale("ru_RU"));
        assertEquals("ru_RU", Util.localeToString(new Locale("ru", "RU")));
    }

}