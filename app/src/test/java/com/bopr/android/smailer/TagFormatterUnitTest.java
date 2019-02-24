package com.bopr.android.smailer;

import android.content.res.Resources;

import org.junit.Assert;
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
    public void startUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(resources.getString(PATTERN_ONE)).thenReturn("{one}, {two} and {three}");
        Mockito.when(resources.getString(THREE)).thenReturn("THREE");
    }

    @Test
    public void testPut() {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutMultiple() {
        String text = formatter("{one}, {two} and {three} and {one} again")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE, TWO and THREE and ONE again", text);
    }

/*
    @Test
    public void testPutPerformance() {
        for (int i = 0; i < 10000000; i++) {
             testPut();
        }
    }
*/

    @Test
    public void testPutRemoveBlank() {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testPutRemoveNull() {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", null)
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE,  and THREE", text);
    }

    @Test
    public void testPutRemoveAbsent() {
        String text = formatter("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE,  and THREE", text);
    }
    
    @Test
    public void testList() {
        String text = formatter("{list}")
                .putList("list", " ", "ONE", "TWO", "THREE")
                .format();

        Assert.assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testListNullValue() {
        String text = formatter("{list}")
                .putList("list", " ", "ONE", "TWO", null, "THREE")
                .format();

        Assert.assertEquals("ONE TWO THREE", text);
    }

    @Test
    public void testPutFromResource() {
        String text = formatter(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format();

        Assert.assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testPutResource() {
        String text = formatter(PATTERN_ONE, resources)
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", THREE)
                .format();

        Assert.assertEquals("ONE, TWO and THREE", text);
    }

    @Test
    public void testEscape() {
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