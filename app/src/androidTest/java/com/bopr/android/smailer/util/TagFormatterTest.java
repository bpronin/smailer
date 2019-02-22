package com.bopr.android.smailer.util;

import android.content.res.Resources;

import com.bopr.android.smailer.BaseTest;

import org.junit.Test;

import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * {@link TagFormatter} tester.
 */
public class TagFormatterTest extends BaseTest {

    private static final int PATTERN_ONE = 1000;
    private static final int THREE = 1001;

    private Resources resources;

    @SuppressWarnings("ResourceType")
    public void setUp() {
        resources = mock(Resources.class);
        when(resources.getString(PATTERN_ONE)).thenReturn("{one}, {two} and {three}");
        when(resources.getString(THREE)).thenReturn("THREE");
    }

    @Test
    public void testPut() {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutRemoveAbsent() {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("three", "THREE")
                .format();

        assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testPutRemoveBlank() {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "")
                .put("three", "THREE")
                .format();

        assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testList() {
        String text = formatter("{list}")
                .putList("list", " ", "ONE", "TWO", "THREE")
                .format();

        assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testListNullValue() {
        String text = formatter("{list}")
                .putList("list", " ", "ONE", "TWO", null, "THREE")
                .format();

        assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testPutFromResource() {
        String text = formatter(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutResource() {
        String text = formatter(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .putRes("three", THREE)
                .format();

        assertEquals("ONE, TWO and THREE", text);
    }

}