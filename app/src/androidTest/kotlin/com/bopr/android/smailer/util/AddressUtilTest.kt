package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@SmallTest
class AddressUtilTest : BaseTest() {

    @Test
    fun test_FormatPhoneNumber() {
        assertEquals("+7 905 230-94-41", formatPhoneNumber("7 9 0523094   -41"))
    }

    @Test
    fun test_StripPhoneNumber() {
        assertEquals("123456HELLO", stripPhoneNumber("+1- 234-56 - (HeLlo ) "))
        assertEquals("", stripPhoneNumber(""))
    }

    @Test
    fun test_SamePhoneNumber() {
        assertTrue(samePhoneNumber("1234", "1234"))
        assertTrue(samePhoneNumber("+1234-56-Hello", "1-234-56 - (HELLO)"))
    }

    @Test
    fun test_PhoneNumberToRegex() {
        assertEquals("1(.*)56HE..O", phoneNumberToRegEx("+1-*-56 - (HE..O)"))
    }

    @Test
    fun test_ExtractPhoneNumber() {
        assertEquals("+7-(905)-230-94-41", extractPhoneNumber("Tel: <+7-(905)-230-94-41> specified"))
    }

    @Test
    fun test_EscapePhoneNumber() {
        assertEquals("+7-(905)-230-94-41", escapePhoneNumber("+7-(905)-230-94-41"))
        assertEquals("\"SomeCompanyPhone\"", escapePhoneNumber("SomeCompanyPhone"))
    }

    @Test
    fun test_NormalizeEmail() {
        assertEquals("bobson@mail.com", normalizeEmail("bob.son@mail.com"))
        assertEquals("bobson@mail.com", normalizeEmail("BOB.son@mail.COM"))
        assertEquals("bobson@mail.com", normalizeEmail("b.o.b.s.on@mail.com"))
        assertEquals("\"bob.son\"@mail.com", normalizeEmail("\"bob.son\"@mail.com"))
    }

    @Test
    fun test_EmailEqual() {
        assertTrue(sameEmail("bobson@mail.com", "bobson@mail.com"))
        assertTrue(sameEmail("bobson@mail.com", "bob.son@mail.com"))
        assertFalse(sameEmail("bobson@mail.com", "\"bob.son\"@mail.com"))
        assertTrue(sameEmail("b.o.b.s.on@mail.com", "bobson@mail.com"))
    }
}