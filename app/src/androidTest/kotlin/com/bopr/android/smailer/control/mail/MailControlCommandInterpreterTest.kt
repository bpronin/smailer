package com.bopr.android.smailer.control.mail

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.control.ControlCommand.Action.*
import org.junit.Assert.*
import org.junit.Test

@SmallTest
class MailControlCommandInterpreterTest : BaseTest() {

    private val interpreter get() = MailControlCommandInterpreter()

    @Test
    fun testParseEmpty() {
        assertNull(interpreter.interpret(""))
    }

    @Test
    fun testParseNoAcceptor() {
        val command = interpreter.interpret("Somebody! Do something with that sheet!!!")

        assertNull(command)
    }

    @Test
    fun testParseEmptyAcceptor() {
        val command = interpreter.interpret("Dear device! do something!")!!

        assertNull(command.target)
        assertNull(command.action)
        assertNull(command.argument)
    }

    @Test
    fun testParseNoAction() {
        val command = interpreter.interpret("Dear device \"Phone\", do something!")!!

        assertEquals("Phone", command.target)
        assertNull(command.action)
        assertNull(command.argument)
    }

    @Test
    fun testParseAddNoArguments() {
        val command = interpreter.interpret("To device \"Phone\": add")!!

        assertEquals("Phone", command.target)
        assertNull(command.action)
        assertNull(command.argument)
    }

    @Test
    fun testParseRemoveNoArguments() {
        val command = interpreter.interpret("To device \"Phone\": remove")!!

        assertEquals("Phone", command.target)
        assertNull(command.action)
        assertNull(command.argument)
    }

    @Test
    fun testParseAddPhoneNoNumber() {
        val command = interpreter.interpret("To device \"Phone\": add phone to blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_PHONE_TO_BLACKLIST, command.action)
        assertNull(command.argument)
    }

    @Test
    fun testParseAddPhoneNoList() {
        val command = interpreter.interpret("To device \"Phone\": add phone +12354")!!

        assertEquals("Phone", command.target)
        assertNull(command.action)
        assertNotNull(command.argument)
    }

    @Test
    fun testParseAddPhoneToBlacklist() {
        val command = interpreter.interpret("To device \"Phone\": add phone +7905-09441 to blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_PHONE_TO_BLACKLIST, command.action)
        assertEquals("+7905-09441", command.argument)
    }

    @Test
    fun testParsePutPhoneToBlacklist() {
        val command = interpreter.interpret("To device \"Phone\": put phone +7905-09441 to blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_PHONE_TO_BLACKLIST, command.action)
        assertEquals("+7905-09441", command.argument)
    }

    @Test
    fun testParseAddQuotedPhoneToBlacklist() {
        val command = interpreter.interpret("To device \"Phone\": add phone \"THE-PHONE\" to blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_PHONE_TO_BLACKLIST, command.action)
        assertEquals("THE-PHONE", command.argument)
    }

    @Test
    fun testParseAddPhoneToWhitelist() {
        val command = interpreter.interpret("To device \"Phone\": add phone +7905-09441 to whitelist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_PHONE_TO_WHITELIST, command.action)
        assertEquals("+7905-09441", command.argument)
    }

    @Test
    fun testParseRemovePhoneFromBlacklist() {
        val command = interpreter.interpret("To device \"Phone\": remove phone +7905-09441 from blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(REMOVE_PHONE_FROM_BLACKLIST, command.action)
        assertEquals("+7905-09441", command.argument)
    }

    @Test
    fun testParseDeletePhoneFromBlacklist() {
        val command = interpreter.interpret("To device \"Phone\": delete phone +7905-09441 from blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(REMOVE_PHONE_FROM_BLACKLIST, command.action)
        assertEquals("+7905-09441", command.argument)
    }

    @Test
    fun testParseRemovePhoneFromWhitelist() {
        val command = interpreter.interpret("To device \"Phone\": remove phone +7905-09441 from whitelist")!!

        assertEquals("Phone", command.target)
        assertEquals(REMOVE_PHONE_FROM_WHITELIST, command.action)
        assertEquals("+7905-09441", command.argument)
    }

    @Test
    fun testParseAddTextNoText() {
        val command = interpreter.interpret("To device \"Phone\": add text to blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_TEXT_TO_BLACKLIST, command.action)
        assertNull(command.argument)
    }

    @Test
    fun testParseAddTextNoList() {
        val command = interpreter.interpret("To device \"Phone\": add text \"Hello\"")!!

        assertEquals("Phone", command.target)
        assertNull(command.action)
        assertNotNull(command.argument)
    }

    @Test
    fun testParseAddTextToBlacklist() {
        val command = interpreter.interpret("To device \"Phone\": add text \"Hello\" to blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_TEXT_TO_BLACKLIST, command.action)
        assertEquals("Hello", command.argument)
    }

    @Test
    fun testParseAddTextToWhitelist() {
        val command = interpreter.interpret("To device \"Phone\": add text \"Hello\" to whitelist")!!

        assertEquals("Phone", command.target)
        assertEquals(ADD_TEXT_TO_WHITELIST, command.action)
        assertEquals("Hello", command.argument)
    }

    @Test
    fun testParseRemoveTextFromBlacklist() {
        val command = interpreter.interpret("To device \"Phone\": remove text \"Hello\" from blacklist")!!

        assertEquals("Phone", command.target)
        assertEquals(REMOVE_TEXT_FROM_BLACKLIST, command.action)
        assertEquals("Hello", command.argument)
    }

    @Test
    fun testParseRemoveTextFromWhitelist() {
        val command = interpreter.interpret("To device \"Phone\": remove text \"Hello\" from whitelist")!!

        assertEquals("Phone", command.target)
        assertEquals(REMOVE_TEXT_FROM_WHITELIST, command.action)
        assertEquals("Hello", command.argument)
    }

    @Test
    fun testParseSendSms() {
        val command = interpreter.interpret("To device \"The Device\": send SMS \"Message\nto caller\" to +12345")!!

        assertEquals(SEND_SMS_TO_CALLER, command.action)
        assertEquals("The Device", command.target)
        assertEquals("Message\nto caller", command.arguments["text"])
        assertEquals("+12345", command.arguments["phone"])
    }

    @Test
    fun testParseSendSmsNoPhone() {
        val command = interpreter.interpret("To device \"The Device\": send SMS \"Message to caller\"")!!

        assertEquals(SEND_SMS_TO_CALLER, command.action)
        assertEquals("The Device", command.target)
        assertEquals("Message to caller", command.arguments["text"])
        assertNull(command.arguments["phone"])
    }

    @Test
    fun testParseSendSmsNoText() {
        val command = interpreter.interpret("To device \"The Device\": send SMS to +12345")!!

        assertEquals(SEND_SMS_TO_CALLER, command.action)
        assertEquals("The Device", command.target)
        assertNull(command.arguments["text"])
        assertEquals("+12345", command.arguments["phone"])
    }

}