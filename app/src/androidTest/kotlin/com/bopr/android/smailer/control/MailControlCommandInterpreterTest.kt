package com.bopr.android.smailer.control

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.control.ControlCommand.Action
import com.bopr.android.smailer.control.ControlCommand.Action.*
import org.junit.Assert.*
import org.junit.Test

@SmallTest
class MailControlCommandInterpreterTest : BaseTest() {

    @Test
    fun testParseEmpty() {
        assertNull(MailControlCommandInterpreter().interpret(""))
    }

    @Test
    fun testParseNoAcceptor() {
        val task = MailControlCommandInterpreter().interpret("Somebody! Do something with that sheet!!!")

        assertNull(task)
    }

    @Test
    fun testParseEmptyAcceptor() {
        val task = MailControlCommandInterpreter().interpret("Dear device! do something!")!!

        assertNull(task.acceptor)
        assertNull(task.action)
        assertNull(task.argument)
    }

    @Test
    fun testParseNoAction() {
        val task = MailControlCommandInterpreter().interpret("Dear device \"Phone\", do something!")!!

        assertEquals("Phone", task.acceptor)
        assertNull(task.action)
        assertNull(task.argument)
    }

    @Test
    fun testParseAddNoArguments() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add")!!

        assertEquals("Phone", task.acceptor)
        assertNull(task.action)
        assertNull(task.argument)
    }

    @Test
    fun testParseRemoveNoArguments() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": remove")!!

        assertEquals("Phone", task.acceptor)
        assertNull(task.action)
        assertNull(task.argument)
    }

    @Test
    fun testParseAddPhoneNoNumber() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add phone to blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_PHONE_TO_BLACKLIST, task.action)
        assertNull(task.argument)
    }

    @Test
    fun testParseAddPhoneNoList() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add phone +12354")!!

        assertEquals("Phone", task.acceptor)
        assertNull(task.action)
        assertNotNull(task.argument)
    }

    @Test
    fun testParseAddPhoneToBlacklist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add phone +7905-09441 to blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_PHONE_TO_BLACKLIST, task.action)
        assertEquals("+7905-09441", task.argument)
    }

    @Test
    fun testParsePutPhoneToBlacklist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": put phone +7905-09441 to blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_PHONE_TO_BLACKLIST, task.action)
        assertEquals("+7905-09441", task.argument)
    }

    @Test
    fun testParseAddQuotedPhoneToBlacklist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add phone \"THE-PHONE\" to blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_PHONE_TO_BLACKLIST, task.action)
        assertEquals("THE-PHONE", task.argument)
    }

    @Test
    fun testParseAddPhoneToWhitelist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add phone +7905-09441 to whitelist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_PHONE_TO_WHITELIST, task.action)
        assertEquals("+7905-09441", task.argument)
    }

    @Test
    fun testParseRemovePhoneFromBlacklist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": remove phone +7905-09441 from blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(REMOVE_PHONE_FROM_BLACKLIST, task.action)
        assertEquals("+7905-09441", task.argument)
    }

    @Test
    fun testParseDeletePhoneFromBlacklist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": delete phone +7905-09441 from blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(REMOVE_PHONE_FROM_BLACKLIST, task.action)
        assertEquals("+7905-09441", task.argument)
    }

    @Test
    fun testParseRemovePhoneFromWhitelist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": remove phone +7905-09441 from whitelist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(REMOVE_PHONE_FROM_WHITELIST, task.action)
        assertEquals("+7905-09441", task.argument)
    }

    @Test
    fun testParseAddTextNoText() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add text to blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_TEXT_TO_BLACKLIST, task.action)
        assertNull(task.argument)
    }

    @Test
    fun testParseAddTextNoList() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add text \"Hello\"")!!

        assertEquals("Phone", task.acceptor)
        assertNull(task.action)
        assertNotNull(task.argument)
    }

    @Test
    fun testParseAddTextToBlacklist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add text \"Hello\" to blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_TEXT_TO_BLACKLIST, task.action)
        assertEquals("Hello", task.argument)
    }

    @Test
    fun testParseAddTextToWhitelist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": add text \"Hello\" to whitelist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(ADD_TEXT_TO_WHITELIST, task.action)
        assertEquals("Hello", task.argument)
    }

    @Test
    fun testParseRemoveTextFromBlacklist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": remove text \"Hello\" from blacklist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(REMOVE_TEXT_FROM_BLACKLIST, task.action)
        assertEquals("Hello", task.argument)
    }

    @Test
    fun testParseRemoveTextFromWhitelist() {
        val task = MailControlCommandInterpreter().interpret("To device \"Phone\": remove text \"Hello\" from whitelist")!!

        assertEquals("Phone", task.acceptor)
        assertEquals(REMOVE_TEXT_FROM_WHITELIST, task.action)
        assertEquals("Hello", task.argument)
    }

    @Test
    fun testParseSendSms() {
        val task = MailControlCommandInterpreter().interpret("To device \"The Device\": send SMS \"Message\nto caller\" to +12345")!!

        assertEquals(SEND_SMS_TO_CALLER, task.action)
        assertEquals("The Device", task.acceptor)
        assertEquals("Message\nto caller", task.arguments["text"])
        assertEquals("+12345", task.arguments["phone"])
    }

    @Test
    fun testParseSendSmsNoPhone() {
        val task = MailControlCommandInterpreter().interpret("To device \"The Device\": send SMS \"Message to caller\"")!!

        assertEquals(SEND_SMS_TO_CALLER, task.action)
        assertEquals("The Device", task.acceptor)
        assertEquals("Message to caller", task.arguments["text"])
        assertNull(task.arguments["phone"])
    }

    @Test
    fun testParseSendSmsNoText() {
        val task = MailControlCommandInterpreter().interpret("To device \"The Device\": send SMS to +12345")!!

        assertEquals(SEND_SMS_TO_CALLER, task.action)
        assertEquals("The Device", task.acceptor)
        assertNull(task.arguments["text"])
        assertEquals("+12345", task.arguments["phone"])
    }
}