package com.bopr.android.smailer.util;

import org.junit.Test;

import static com.bopr.android.smailer.util.PhoneUtil.escapePhone;
import static com.bopr.android.smailer.util.PhoneUtil.extractPhone;
import static com.bopr.android.smailer.util.PhoneUtil.phoneToRegEx;
import static com.bopr.android.smailer.util.PhoneUtil.phonesEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PhoneUtilTest {

    @Test
    public void testPhonesEqual() {
        assertTrue(phonesEqual(null, null));
        assertTrue(phonesEqual("1234", "1234"));
        assertTrue(phonesEqual("+1234-56-Hello", "1-234-56 - (HELLO)"));
    }

    @Test
    public void testPhoneToRegex() {
        assertEquals("1(.*)56HE..O", phoneToRegEx("1-*-56 - (HE..O)"));
    }

    @Test
    public void testExtractPhone() {
        assertEquals("+7-(905)-230-94-41", extractPhone("Tel: <+7-(905)-230-94-41> specified"));
    }

    @Test
    public void testEscapePhone() {
        assertEquals("+7-(905)-230-94-41", escapePhone("+7-(905)-230-94-41"));
        assertEquals("\"SomeCompanyPhone\"", escapePhone("SomeCompanyPhone"));
    }

}