package com.bopr.android.smailer.util;

import com.bopr.android.smailer.BaseTest;

import org.junit.Test;

import java.util.Set;

import static com.bopr.android.smailer.util.Util.setOf;
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
    public void testSetOf() {
        final Set<String> set = setOf("A", "B", "B", "C");

        assertEquals(3, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertTrue(set.contains("C"));
        assertThrows(UnsupportedOperationException.class, () -> set.add("D"));
    }


}