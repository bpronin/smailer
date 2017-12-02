package com.bopr.android.smailer.util;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


/**
 * Class RegexpUtilTest tester.
 */
public class RegexpUtilTest {

    @Test
    public void caseInsensitive() throws Exception {
        assertTrue("Text containing Ann and Bob".matches(RegexpUtil.caseInsensitive(".*bob.*")));
    }

    @Test
    public void patternContains() throws Exception {
        assertTrue("Text containing Ann and Bob".matches(RegexpUtil.patternContains("Bob")));
        assertTrue("Text containing Ann and Bob".matches(RegexpUtil.patternContains("Bob", "Ann")));
        assertTrue("Text containing Ann and Bob".matches(RegexpUtil.patternContains("Bob", "Can")));
        assertFalse("Text containing Ann and Bob".matches(RegexpUtil.patternContains("Can")));
    }

    @Test
    public void patternDoesNotContain() throws Exception {
        assertTrue("Text containing Ann and Bob".matches(RegexpUtil.patternDoesNotContain("Can")));
        assertTrue("Text containing Ann and Bob".matches(RegexpUtil.patternDoesNotContain("Can", "Dan")));
        assertFalse("Text containing Ann and Bob".matches(RegexpUtil.patternDoesNotContain("Ann")));
    }

}