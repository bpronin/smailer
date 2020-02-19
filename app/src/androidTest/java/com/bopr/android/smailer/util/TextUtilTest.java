package com.bopr.android.smailer.util;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.GeoCoordinates;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static com.bopr.android.smailer.util.TextUtil.capitalize;
import static com.bopr.android.smailer.util.TextUtil.commaJoin;
import static com.bopr.android.smailer.util.TextUtil.commaSplit;
import static com.bopr.android.smailer.util.TextUtil.decimalToDMS;
import static com.bopr.android.smailer.util.TextUtil.escapeRegex;
import static com.bopr.android.smailer.util.TextUtil.formatCoordinates;
import static com.bopr.android.smailer.util.TextUtil.formatDuration;
import static com.bopr.android.smailer.util.TextUtil.isNotEmpty;
import static com.bopr.android.smailer.util.TextUtil.isNullOrBlank;
import static com.bopr.android.smailer.util.TextUtil.isNullOrEmpty;
import static com.bopr.android.smailer.util.TextUtil.isQuoted;
import static com.bopr.android.smailer.util.TextUtil.join;
import static com.bopr.android.smailer.util.TextUtil.split;
import static com.bopr.android.smailer.util.TextUtil.unescapeRegex;
import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link TextUtil} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TextUtilTest extends BaseTest {

    @Test
    public void testFormatLocation() {
        assertEquals("30d33m59sn, 60d33m59sw", formatCoordinates(new GeoCoordinates(30.5664, 60.5664),
                "d", "m", "s", "n", "s", "w", "e"));
    }

    @Test
    public void testFormatLocation1() {
        assertEquals("30°33'59\"N, 60°33'59\"W", formatCoordinates(new GeoCoordinates(30.5664, 60.5664)));
        assertEquals("30°33'59\"S, 60°33'59\"E", formatCoordinates(new GeoCoordinates(-30.5664, -60.5664)));
    }

    @Test
    public void testDecimalToDMS() {
        assertEquals("90D33M59S", decimalToDMS(90.5664, "D", "M", "S"));
    }

    @Test
    public void testCapitalize() {
        assertEquals("Hello", capitalize("hello"));
        assertEquals("Hello", capitalize("Hello"));
        assertEquals("", capitalize(""));
        assertNull(capitalize(null));
    }

    @Test
    public void testIsQuoted() {
        assertTrue(isQuoted("\"hello\""));
        assertTrue(isQuoted("\"\"hello\"\""));
        assertTrue(isQuoted("\"\"hello\""));
        assertFalse(isQuoted("hello"));
        assertFalse(isQuoted("\"hello"));
        assertFalse(isQuoted("hello\""));
        assertFalse(isQuoted(""));
        assertFalse(isQuoted(" "));
        assertFalse(isQuoted(null));
    }

    @Test
    public void testFormatDuration() {
        long duration = HOURS.toMillis(15) + MINUTES.toMillis(15) + SECONDS.toMillis(15);
        assertEquals("15:15:15", formatDuration(duration));
    }

    @Test
    public void testIsNullOrEmpty() {
        assertFalse(isNullOrEmpty("A"));
        assertFalse(isNullOrEmpty("   "));
        assertTrue(isNullOrEmpty(""));
        assertTrue(isNullOrEmpty(null));
    }

    @Test
    public void testIsNotEmpty() {
        assertTrue(isNotEmpty("A"));
        assertTrue(isNotEmpty("   "));
        assertFalse(isNotEmpty(""));
        assertFalse(isNotEmpty(null));
    }

    @Test
    public void testIsNullOrBlank() {
        assertFalse(isNullOrBlank("A"));
        assertTrue(isNullOrBlank(""));
        assertTrue(isNullOrBlank(null));
        assertFalse(isNullOrBlank(" A "));
        assertTrue(isNullOrBlank("    "));
    }

    @Test
    public void testJoin() {
        assertEquals("1, 2, 3", join(of(1, 2, 3), ", "));
        assertEquals("1, , ", join(of(1, "", ""), ", "));
        assertEquals("", join(of(), ", "));
        assertEquals("[1]|[2]|[3]", join(of(1, 2, 3), "|", new Function<Object, String>() {

            @Override
            public String apply(Object s) {
                return "[" + s + "]";
            }
        }));
    }

    @Test
    public void tesSplit() {
        assertEquals(of(), split("", ";"));
        assertEquals(of(" "), split(" ", ";"));
        assertEquals(of("1", " 2", "3 "), split("1; 2;3 ", ";"));
        assertEquals(of("[1]", "[2]", "[3]"), split("1;2;3", ";", new Function<String, String>() {

            @Override
            public String apply(String s) {
                return "[" + s + "]";
            }
        }));
    }

    @Test
    public void testEscapeRegex() {
        assertEquals("REGEX:text", escapeRegex("text"));
        assertEquals("REGEX:", escapeRegex(""));
        assertEquals("REGEX:   ", escapeRegex("   "));
        assertEquals("REGEX:REGEX:", escapeRegex("REGEX:"));
    }

    @Test
    public void testUnescapeRegex() {
        assertNull(unescapeRegex(null));
        assertNull(unescapeRegex(""));
        assertNull(unescapeRegex("   "));
        assertNull(unescapeRegex("text"));
        assertEquals("text", unescapeRegex("REGEX:text"));
        assertEquals("REGEX:", unescapeRegex("REGEX:REGEX:"));
        assertEquals("", unescapeRegex("REGEX:"));
    }

    @Test
    public void testCommaJoin() {
        assertEquals("a,b,c", commaJoin(asList("a", "b", "c")));
        assertEquals("1,2,3", commaJoin(asList(1, 2, 3)));
        assertEquals("1,c,3", commaJoin(asList(1, "c", 3)));
        assertEquals("a,b/,c,d", commaJoin(asList("a", "b,c", "d")));
        assertEquals("", commaJoin(ImmutableSet.of()));
    }

    @Test
    public void testCommaSplit() {
        assertTrue(commaSplit("").isEmpty());
        assertEquals(of(""), commaSplit("   "));
        assertEquals(of("1", "2", "3"), commaSplit("1,2,3"));
        assertEquals(of("1", "2", "3"), commaSplit(" 1, 2, 3 "));
        assertEquals(of("a", "b,c", "d"), commaSplit("a, b/,c, d"));
    }

}