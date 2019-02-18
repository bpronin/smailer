package com.bopr.android.smailer;

import android.content.res.Resources;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.bopr.android.smailer.util.TagFormatter.formatter;


/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TagFormatterUnitTest {

    private static final int PATTERN_ONE = 1000;
    private static final int THREE = 1001;

    @Mock
    private static Resources resources;

    @SuppressWarnings("ResourceType")
    @Before
    public void startUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resources.getString(PATTERN_ONE)).thenReturn("{one}, {two} and {three}");
        Mockito.when(resources.getString(THREE)).thenReturn("THREE");
    }

    @Test
    public void testPut() throws Exception {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutRemoveAbsent() throws Exception {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testPutRemoveBlank() throws Exception {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testList() throws Exception {
        String text = formatter("{list}")
                .putList("list", " ", "ONE", "TWO", "THREE")
                .format();

        Assert.assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testListNullValue() throws Exception {
        String text = formatter("{list}")
                .putList("list", " ", "ONE", "TWO", null, "THREE")
                .format();

        Assert.assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testPutFromResource() throws Exception {
        String text = formatter(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutResource() throws Exception {
        String text = formatter(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .putRes("three", THREE)
                .format();

        Assert.assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testEscape() throws Exception {
//        String text = formatter("/{three/}")
////                .put("one", "ONE")
//                .put("three", "THREE")
//                .format();
//        String text = "{three}".replaceAll("\\{(.*?)\\}", "text");
        String text = "{/{three".replaceAll("\\{(?!/\\{)", "*");
        System.out.println(text);
//        Assert.assertEquals("{three}", text);
    }

}