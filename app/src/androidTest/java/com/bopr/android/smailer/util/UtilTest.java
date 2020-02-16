package com.bopr.android.smailer.util;

import com.bopr.android.smailer.BaseTest;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * {@link Util} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class UtilTest extends BaseTest {

    @Test
    public void testAsSet() {
        final Set<String> set = Util.asSet("A", "B", "B", "C");

        assertEquals(3, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertTrue(set.contains("C"));
        assertThrows(UnsupportedOperationException.class, new ThrowingRunnable() {

            @Override
            public void run() {
                set.add("D");
            }
        });
    }

    @Test
    public void testToArray() {
        Set<String> set = Util.asSet("A", "B", "C");
        String[] strings = Util.toArray(set);
        assertArrayEquals(strings, new String[]{"A", "B", "C"});
    }


}