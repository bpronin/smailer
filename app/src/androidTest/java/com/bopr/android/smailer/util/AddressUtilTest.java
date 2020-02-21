package com.bopr.android.smailer.util;

import org.junit.Test;

import static com.bopr.android.smailer.util.AddressUtil.escapePhone;
import static com.bopr.android.smailer.util.AddressUtil.extractEmail;
import static com.bopr.android.smailer.util.AddressUtil.extractPhone;
import static com.bopr.android.smailer.util.AddressUtil.phoneToRegEx;
import static com.bopr.android.smailer.util.AddressUtil.sameEmails;
import static com.bopr.android.smailer.util.AddressUtil.samePhones;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AddressUtilTest {

    @Test
    public void testPhonesEqual() {
        assertTrue(samePhones("1234", "1234"));
        assertTrue(samePhones("+1234-56-Hello", "1-234-56 - (HELLO)"));
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

    @Test
    public void testEmailEqual() {
        assertTrue(sameEmails("bobson@mail.com", "bobson@mail.com"));
        assertTrue(sameEmails("bobson@mail.com", "bob.son@mail.com"));
        assertFalse(sameEmails("bobson@mail.com", "\"bob.son\"@mail.com"));
        assertTrue(sameEmails("b.o.b.s.on@mail.com", "bobson@mail.com"));
    }

    @Test
    public void testExtractEmail() {
        assertNull(extractEmail("From address"));
        assertEquals("mail@mail.com", extractEmail("From: <mail@mail.com> address"));
    }

}