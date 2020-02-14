package com.bopr.android.smailer.util;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.GeoCoordinates;

import org.junit.Test;

import java.util.Locale;
import java.util.Set;

import static com.bopr.android.smailer.util.AddressUtil.extractEmail;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link Util} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class UtilTest extends BaseTest {

    @Test
    public void testFormatLocation() {
        assertEquals("30d33m59sn, 60d33m59sw", TextUtil.formatLocation(new GeoCoordinates(30.5664, 60.5664),
                "d", "m", "s", "n", "s", "w", "e"));
    }

    @Test
    public void testFormatLocation1() {
        assertEquals("30°33'59\"N, 60°33'59\"W", TextUtil.formatLocation(new GeoCoordinates(30.5664, 60.5664)));
        assertEquals("30°33'59\"S, 60°33'59\"E", TextUtil.formatLocation(new GeoCoordinates(-30.5664, -60.5664)));
    }

    @Test
    public void testDecimalToDMS() {
        assertEquals("90D33M59S", TextUtil.decimalToDMS(90.5664, "D", "M", "S"));
    }

    @Test
    public void testCapitalize() {
        assertEquals("Hello", TextUtil.capitalize("hello"));
        assertEquals("Hello", TextUtil.capitalize("Hello"));
        assertEquals("", TextUtil.capitalize(""));
        assertNull(TextUtil.capitalize(null));
    }

    @Test
    public void testFormatDuration() {
        long duration = HOURS.toMillis(15) + MINUTES.toMillis(15) + SECONDS.toMillis(15);
        assertEquals("15:15:15", TextUtil.formatDuration(duration));
    }

    @Test
    public void testIsEmpty() {
        assertFalse(TextUtil.isNullOrEmpty("A"));
        assertTrue(TextUtil.isNullOrEmpty(""));
        assertTrue(TextUtil.isNullOrEmpty((String) null));
    }

    @Test
    public void testIsTrimEmpty() {
        assertFalse(TextUtil.isNullOrBlank("A"));
        assertTrue(TextUtil.isNullOrBlank(""));
        assertTrue(TextUtil.isNullOrBlank(null));
        assertFalse(TextUtil.isNullOrBlank(" A "));
        assertTrue(TextUtil.isNullOrBlank("    "));
    }

    @Test
    public void testStringOf() {
        assertEquals("1, 2, 3", TextUtil.join(", ", 1, 2, 3));
        assertEquals("1, null, null", TextUtil.join(", ", 1, null, null));
        assertEquals("", TextUtil.join(", "));
    }

    @Test
    public void testListOf() {
        assertArrayEquals(new String[]{"1", " 2", "3 "}, TextUtil.split("1, 2,3 ", ",", false).toArray());
        assertArrayEquals(new String[]{"1", "2", "3"}, TextUtil.split("1, 2, 3 ", ",", true).toArray());
        assertArrayEquals(new String[]{" "}, TextUtil.split(" ", ",", false).toArray());
        assertArrayEquals(new String[]{}, TextUtil.split("", ",", true).toArray());
        assertArrayEquals(new String[]{}, TextUtil.split(" ", ",", true).toArray());
        assertArrayEquals(new String[]{}, TextUtil.split(null, ",", true).toArray());
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
        assertEquals(new Locale("ru", "RU"), TextUtil.stringToLocale("ru_RU"));
        assertEquals(Locale.getDefault(), TextUtil.stringToLocale("default"));
        assertNull(TextUtil.stringToLocale(""));
        assertNull(TextUtil.stringToLocale(null));
    }

    @Test
    public void testLocaleToString() {
        assertEquals("ru_RU", TextUtil.localeToString(new Locale("ru", "RU")));
        assertEquals("default", TextUtil.localeToString(Locale.getDefault()));
        assertNull(TextUtil.localeToString(null));
    }

    @Test
    public void testExtractEmail() {
        assertNull(extractEmail("From address"));
        assertEquals("mail@mail.com", extractEmail("From: <mail@mail.com> address"));
    }


}