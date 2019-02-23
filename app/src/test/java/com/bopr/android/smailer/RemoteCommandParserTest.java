package com.bopr.android.smailer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("ConstantConditions")
public class RemoteCommandParserTest {

    @Test
    public void testParseEmpty() {
        RemoteCommandParser parser = new RemoteCommandParser();

        assertNull(parser.parse(new MailMessage("0", null, null)));
        assertNull(parser.parse(new MailMessage("0", null, "")));
        assertNull(parser.parse(new MailMessage("0", "Re: [SMailer]", "")));
        assertNull(parser.parse(new MailMessage("0", "Re: [SMailer]", " ")));
        assertNull(parser.parse(new MailMessage("0", "Re: [SMailer]", "This is nothing to do with commands")));
        assertNull(parser.parse(new MailMessage("0", "Re: [SMailer]", "Add")));
        assertNull(parser.parse(new MailMessage("0", "Re: [SMailer]", "Remove")));
    }

    @Test
    public void testParseBadFormatted() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "To the\n BLACKLIST \r\n Add This, young Padawan!!!");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    /* phone add */

    @Test
    public void testParseAddPhoneToBlacklistFromSubject() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "add to blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToBlacklistFromBody() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "add phone +45782 to blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToBlacklistFromSubjectActionOmitted() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToBlacklistFromBodyActionOmitted() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "+49847987. blacklist it");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToWhitelistFromSubject() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "add to whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToWhitelistFromBody() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "add phone +45782 to whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddPhoneToWhitelistFromSubjectActionOmitted() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }
    @Test
    public void testParseAddPhoneToWhitelistFromBodyActionOmitted() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "+49847987. whitelist it");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_PHONE_TO_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    /* phone remove */

    @Test
    public void testParseRemovePhoneFromBlacklistFromSubject() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "remove from blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseRemovePhoneFromBlacklistFromBody() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "remove phone +45782 from blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseRemovePhoneFromWhitelistFromSubject() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "remove from whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseRemovePhoneFromWhitelistFromBody() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "remove phone +45782 from whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.REMOVE_PHONE_FROM_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }
    
    /* text add */

    @Test
    public void testParseAddTextToBlacklist() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "add text \"spam\" to blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_TEXT_TO_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddTextToBlacklistActionOmitted() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "blacklist text \"spam\"");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_TEXT_TO_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddTextToWhitelist() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "add text \"spam\" to whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_TEXT_TO_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseAddTextToWhitelistActionOmitted() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "text \"spam\". whitelist it");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.ADD_TEXT_TO_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    /* text remove */

    @Test
    public void testParseRemoveTextFromBlacklist() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "remove text \"spam\" from blacklist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.REMOVE_TEXT_FROM_BLACKLIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

    @Test
    public void testParseRemoveTextFromWhitelist() {
        MailMessage message = new MailMessage("0", "Re: [SMailer] Incoming SMS from +12345678901",
                "remove text \"spam\" from whitelist");

        RemoteCommandParser.Result result = new RemoteCommandParser().parse(message);

        assertEquals(RemoteCommandParser.REMOVE_TEXT_FROM_WHITELIST, result.action);
//        assertEquals("+12345678901", result.argument);
    }

}