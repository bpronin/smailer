package com.bopr.android.smailer.util

import com.bopr.android.smailer.util.AddressUtil.escapePhone
import com.bopr.android.smailer.util.AddressUtil.extractPhone
import com.bopr.android.smailer.util.AddressUtil.normalizeEmail
import com.bopr.android.smailer.util.AddressUtil.normalizePhone
import com.bopr.android.smailer.util.AddressUtil.phoneToRegEx
import com.bopr.android.smailer.util.AddressUtil.sameEmail
import com.bopr.android.smailer.util.AddressUtil.samePhone
import org.junit.Assert.*
import org.junit.Test

class AddressUtilTest {

    @Test
    fun testNormalizePhone() {
        assertEquals("123456HELLO", normalizePhone("+1- 234-56 - (HeLlo ) "))
        assertEquals("", normalizePhone(""))
    }

    @Test
    fun testPhonesEqual() {
        assertTrue(samePhone("1234", "1234"))
        assertTrue(samePhone("+1234-56-Hello", "1-234-56 - (HELLO)"))
    }

    @Test
    fun testPhoneToRegex() {
        assertEquals("1(.*)56HE..O", phoneToRegEx("+1-*-56 - (HE..O)"))
    }

    @Test
    fun testExtractPhone() {
        assertEquals("+7-(905)-230-94-41", extractPhone("Tel: <+7-(905)-230-94-41> specified"))
    }

    @Test
    fun testEscapePhone() {
        assertEquals("+7-(905)-230-94-41", escapePhone("+7-(905)-230-94-41"))
        assertEquals("\"SomeCompanyPhone\"", escapePhone("SomeCompanyPhone"))
    }

    @Test
    fun testNormalizeEmail() {
        assertEquals("bobson@mail.com", normalizeEmail("bob.son@mail.com"))
        assertEquals("bobson@mail.com", normalizeEmail("b.o.b.s.on@mail.com"))
        assertEquals("\"bob.son\"@mail.com", normalizeEmail("\"bob.son\"@mail.com"))
    }

    @Test
    fun testEmailEqual() {
        assertTrue(sameEmail("bobson@mail.com", "bobson@mail.com"))
        assertTrue(sameEmail("bobson@mail.com", "bob.son@mail.com"))
        assertFalse(sameEmail("bobson@mail.com", "\"bob.son\"@mail.com"))
        assertTrue(sameEmail("b.o.b.s.on@mail.com", "bobson@mail.com"))
    }
}