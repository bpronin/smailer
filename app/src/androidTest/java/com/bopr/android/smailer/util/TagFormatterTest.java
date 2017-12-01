package com.bopr.android.smailer.util;

import android.content.res.Resources;

import com.bopr.android.smailer.BaseTest;

import org.junit.Test;

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
    public void setUp() throws Exception {
        resources = mock(Resources.class);
        when(resources.getString(PATTERN_ONE)).thenReturn("{one}, {two} and {three}");
        when(resources.getString(THREE)).thenReturn("THREE");
    }

    @Test
    public void testPut() throws Exception {
        String text = TagFormatter.from("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutRemoveAbsent() throws Exception {
        String text = TagFormatter.from("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("three", "THREE")
                .format();

        assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testPutRemoveBlank() throws Exception {
        String text = TagFormatter.from("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "")
                .put("three", "THREE")
                .format();

        assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testList() throws Exception {
        String text = TagFormatter.from("{list}")
                .putList("list", " ", "ONE", "TWO", "THREE")
                .format();

        assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testListNullValue() throws Exception {
        String text = TagFormatter.from("{list}")
                .putList("list", " ", "ONE", "TWO", null, "THREE")
                .format();

        assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testPutFromResource() throws Exception {
        String text = TagFormatter.from(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutResource() throws Exception {
        String text = TagFormatter.from(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .putResource("three", THREE)
                .format();

        assertEquals("ONE, TWO and THREE", text);
    }

}