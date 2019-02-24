package com.bopr.android.smailer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("ConstantConditions")
public class RemoteCommandParserTest {
    
    @Test
    public void testParseEmpty() {
        RemoteCommandParser parser = new RemoteCommandParser();
        MailMessage message = new MailMessage();
        message.setSubject(null);
        message.setBody(null);
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject(null);
        message.setBody("");
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject("Re: [SMailer]");
        message.setBody("");
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject("Re: [SMailer]");
        message.setBody(" ");
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject("Re: [SMailer]");
        message.setBody("This is nothing to do with commands");
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject("Re: [SMailer]");
        message.setBody("Add");
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject("Re: [SMailer]");
        message.setBody("Remove");
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject("Re: [SMailer]");
        message.setBody("Add text");
        assertNull(parser.parse(message));

        message = new MailMessage();
        message.setSubject("Re: [SMailer]");
        message.setBody("Remove text");
        assertNull(parser.parse(message));
    }

    @Test
    public void testParseBadFormatted() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("To the\n BLACKLIST \r\n Add This, young Padawan!!!");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
        assertEquals("12345678901", result.argument);
    }

    /* phone add */

    @Test
    public void testParseAddPhoneToBlacklistFromSubject() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("add to blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
        assertEquals("12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToBlacklistFromBody() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("add phone +7905*09441 to blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
        assertEquals("7905*09441", result.argument);
    }

    @Test
    public void testParseAddPhoneToBlacklistFromSubjectActionOmitted() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
        assertEquals("12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToBlacklistFromBodyActionOmitted() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("\"+4-098-HELLO-87\". blacklist it");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
        assertEquals("+4-098-HELLO-87", result.argument);
    }

    @Test
    public void testParseAddPhoneToWhitelistFromSubject() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("add to whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
        assertEquals("12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToWhitelistFromBody() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("add phone +45782 to whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
        assertEquals("45782", result.argument);
    }

    @Test
    public void testParseAddPhoneToWhitelistFromSubjectActionOmitted() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
        assertEquals("12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToWhitelistFromBodyActionOmitted() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("+49847987. whitelist it");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
        assertEquals("49847987", result.argument);
    }

    /* phone remove */

    @Test
    public void testParseRemovePhoneFromBlacklistFromSubject() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("remove from blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_BLACKLIST, result.action);
        assertEquals("12345678901", result.argument);
    }

    @Test
    public void testParseRemovePhoneFromBlacklistFromBody() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("remove phone +45782 from blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_BLACKLIST, result.action);
        assertEquals("45782", result.argument);
    }

    @Test
    public void testParseRemovePhoneFromWhitelistFromSubject() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("remove from whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_WHITELIST, result.action);
        assertEquals("12345678901", result.argument);
    }

    @Test
    public void testParseRemovePhoneFromWhitelistFromBody() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("remove phone +45782 from whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_WHITELIST, result.action);
        assertEquals("45782", result.argument);
    }

    /* text add */

    @Test
    public void testParseAddTextToBlacklist() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("add text \"spam\" to blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_TEXT_TO_BLACKLIST, result.action);
        assertEquals("spam", result.argument);
    }

    @Test
    public void testParseAddTextToBlacklistMultipleQuotations() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("add text \"spam\" to blacklist \"not spam\" and \"something else\"");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_TEXT_TO_BLACKLIST, result.action);
        assertEquals("spam", result.argument);
    }

    @Test
    public void testParseAddTextToBlacklistActionOmitted() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("blacklist text \"spam\"");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_TEXT_TO_BLACKLIST, result.action);
        assertEquals("spam", result.argument);
    }

    @Test
    public void testParseAddTextToWhitelist() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("add text \"spam\" to whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_TEXT_TO_WHITELIST, result.action);
        assertEquals("spam", result.argument);
    }

    @Test
    public void testParseAddTextToWhitelistActionOmitted() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("text \"spam\". whitelist it");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.ADD_TEXT_TO_WHITELIST, result.action);
        assertEquals("spam", result.argument);
    }

    /* text remove */

    @Test
    public void testParseRemoveTextFromBlacklist() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("remove text \"spam\" from blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.REMOVE_TEXT_FROM_BLACKLIST, result.action);
        assertEquals("spam", result.argument);
    }

    @Test
    public void testParseRemoveTextFromWhitelist() {
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from +12345678901");
        message.setBody("remove text \"spam\" from whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);
        assertEquals(RemoteCommandParser.REMOVE_TEXT_FROM_WHITELIST, result.action);
        assertEquals("spam", result.argument);
    }
}