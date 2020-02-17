package com.bopr.android.smailer.util

import android.content.res.Resources
import com.bopr.android.smailer.BaseTest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [TagFormatter] tester.
 */
class TagFormatterTest : BaseTest() {

    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = mock {
            on { getString(RESOURCE_PATTERN) }.doReturn("{one}, {two} and {three}")
            on { getString(RESOURCE_THREE) }.doReturn("THREE")
        }
    }

    @Test
    fun testPut() {
        val text = TagFormatter(resources)
                .pattern("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format()

        assertEquals("ONE, TWO and THREE", text)
    }

    @Test
    fun testPutMultiple() {
        val text = TagFormatter(resources)
                .pattern("{one}, {two} and {three} and {one} again")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format()

        assertEquals("ONE, TWO and THREE and ONE again", text)
    }

    @Test
    fun testPutRemoveBlank() {
        val text = TagFormatter(resources)
                .pattern("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "")
                .put("three", "THREE")
                .format()

        assertEquals("ONE,  and THREE", text)
    }

    @Test
    fun testPutEscapeNull() {
        val text = TagFormatter(resources)
                .pattern("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", null as String?)
                .put("three", "THREE")
                .format()

        assertEquals("ONE, {two} and THREE", text)
    }

    @Test
    fun testPutEscapeAbsent() {
        val text = TagFormatter(resources)
                .pattern("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("three", "THREE")
                .format()

        assertEquals("ONE, {two} and THREE", text)
    }

    @Test
    fun testResourcePattern() {
        val text = TagFormatter(resources)
                .pattern(RESOURCE_PATTERN)
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", "THREE")
                .format()

        assertEquals("ONE, TWO and THREE", text)
    }

    @Test
    fun testResourceValue() {
        val text = TagFormatter(resources)
                .pattern("{one}, {two} and {three}")
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", RESOURCE_THREE)
                .format()

        assertEquals("ONE, TWO and THREE", text)
    }

    @Test
    fun testResourcePatternAndValue() {
        val text = TagFormatter(resources)
                .pattern(RESOURCE_PATTERN)
                .put("one", "ONE")
                .put("two", "TWO")
                .put("three", RESOURCE_THREE)
                .format()

        assertEquals("ONE, TWO and THREE", text)
    }

    /*
    @Test
    public void testPutPerformance() {
        for (int i = 0; i < 1000000; i++) {
             testPut();
        }
    }
*/

    companion object {
        private const val RESOURCE_PATTERN = 1000
        private const val RESOURCE_THREE = 1001
    }
}