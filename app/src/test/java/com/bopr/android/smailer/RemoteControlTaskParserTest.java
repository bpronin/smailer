package com.bopr.android.smailer;

import org.junit.Test;

import static com.bopr.android.smailer.RemoteControlTask.ADD_PHONE_TO_BLACKLIST;
import static com.bopr.android.smailer.RemoteControlTask.ADD_PHONE_TO_WHITELIST;
import static com.bopr.android.smailer.RemoteControlTask.ADD_TEXT_TO_BLACKLIST;
import static com.bopr.android.smailer.RemoteControlTask.ADD_TEXT_TO_WHITELIST;
import static com.bopr.android.smailer.RemoteControlTask.REMOVE_PHONE_FROM_BLACKLIST;
import static com.bopr.android.smailer.RemoteControlTask.REMOVE_PHONE_FROM_WHITELIST;
import static com.bopr.android.smailer.RemoteControlTask.REMOVE_TEXT_FROM_BLACKLIST;
import static com.bopr.android.smailer.RemoteControlTask.REMOVE_TEXT_FROM_WHITELIST;
import static com.bopr.android.smailer.RemoteControlTask.SEND_SMS_TO_CALLER;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class RemoteControlTaskParserTest {

    @Test
    public void testParseEmpty() {
        RemoteControlTaskParser parser = new RemoteControlTaskParser();

        RemoteControlTask task;
        MailMessage message;

        message = new MailMessage();
        message.setBody(null);
        try {
            parser.parse(message.getBody());
            fail();
        } catch (NullPointerException x) {
            /* ok */
        }

        message = new MailMessage();
        message.setBody("");
        task = parser.parse(message.getBody());
        assertNull(task.getAcceptor());
        assertNull(task.getAction());
    }

    @Test
    public void testParseNoAcceptor() {
        MailMessage message = new MailMessage();
        message.setBody("Somebody! Do something with that sheet!!!");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());
        assertNull(task.getAcceptor());
        assertNull(task.getAction());
        assertNull(task.getArgument());
    }

    @Test
    public void testParseNoAction() {
        MailMessage message = new MailMessage();
        message.setBody("Dear device \"Phone\", do something!");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertNull(task.getAction());
        assertNull(task.getArgument());
    }

    @Test
    public void testParseAddNoArguments() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertNull(task.getAction());
        assertNull(task.getArgument());
    }

    @Test
    public void testParseRemoveNoArguments() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": remove");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertNull(task.getAction());
        assertNull(task.getArgument());
    }

    @Test
    public void testParseAddPhoneNoNumber() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add phone to blacklist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(ADD_PHONE_TO_BLACKLIST, task.getAction());
        assertNull(task.getArgument());
    }

    @Test
    public void testParseAddPhoneNoList() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add phone +12354");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertNull(task.getAction());
        assertNotNull(task.getArgument());
    }

    @Test
    public void testParseAddPhoneToBlacklist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add phone +7905-09441 to blacklist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(ADD_PHONE_TO_BLACKLIST, task.getAction());
        assertEquals("+7905-09441", task.getArgument());
    }

    @Test
    public void testParseAddPhoneToWhitelist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add phone +7905-09441 to whitelist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(ADD_PHONE_TO_WHITELIST, task.getAction());
        assertEquals("+7905-09441", task.getArgument());
    }

    @Test
    public void testParseRemovePhoneFromBlacklist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": remove phone +7905-09441 from blacklist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(REMOVE_PHONE_FROM_BLACKLIST, task.getAction());
        assertEquals("+7905-09441", task.getArgument());
    }

    @Test
    public void testParseRemovePhoneFromWhitelist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": remove phone +7905-09441 from whitelist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(REMOVE_PHONE_FROM_WHITELIST, task.getAction());
        assertEquals("+7905-09441", task.getArgument());
    }

    @Test
    public void testParseAddTextNoText() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add text to blacklist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(ADD_TEXT_TO_BLACKLIST, task.getAction());
        assertNull(task.getArgument());
    }

    @Test
    public void testParseAddTextNoList() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add text \"Hello\"");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertNull(task.getAction());
        assertNotNull(task.getArgument());
    }

    @Test
    public void testParseAddTextToBlacklist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add text \"Hello\" to blacklist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(ADD_TEXT_TO_BLACKLIST, task.getAction());
        assertEquals("Hello", task.getArgument());
    }

    @Test
    public void testParseAddTextToWhitelist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": add text \"Hello\" to whitelist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(ADD_TEXT_TO_WHITELIST, task.getAction());
        assertEquals("Hello", task.getArgument());
    }

    @Test
    public void testParseRemoveTextFromBlacklist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": remove text \"Hello\" from blacklist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(REMOVE_TEXT_FROM_BLACKLIST, task.getAction());
        assertEquals("Hello", task.getArgument());
    }

    @Test
    public void testParseRemoveTextFromWhitelist() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"Phone\": remove text \"Hello\" from whitelist");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals("Phone", task.getAcceptor());
        assertEquals(REMOVE_TEXT_FROM_WHITELIST, task.getAction());
        assertEquals("Hello", task.getArgument());
    }

    @Test
    public void testParseSendSms() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"The Device\": send SMS \"Message to caller\" to +12345");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals(SEND_SMS_TO_CALLER, task.getAction());
        assertEquals("The Device", task.getAcceptor());
        assertEquals("Message to caller", task.getArgument("text"));
        assertEquals("+12345", task.getArgument("phone"));
    }

    @Test
    public void testParseSendSmsNoPhone() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"The Device\": send SMS \"Message to caller\"");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals(SEND_SMS_TO_CALLER, task.getAction());
        assertEquals("The Device", task.getAcceptor());
        assertEquals("Message to caller", task.getArgument("text"));
        assertNull(task.getArgument("phone"));
    }

    @Test
    public void testParseSendSmsNoText() {
        MailMessage message = new MailMessage();
        message.setBody("To device \"The Device\": send SMS to +12345");

        RemoteControlTask task = new RemoteControlTaskParser().parse(message.getBody());

        assertEquals(SEND_SMS_TO_CALLER, task.getAction());
        assertEquals("The Device", task.getAcceptor());
        assertNull(task.getArgument("text"));
        assertEquals("+12345", task.getArgument("phone"));
    }

/*
    @Test
    public void testParseMultipleCommands() {
        // TODO: 25.02.2019 implement multiple remote commands parsing
        MailMessage message = new MailMessage();
        message.setSubject("Re: [SMailer] Incoming SMS from 12345678901");
        message.setBody("add text \"spam\" to whitelist remove text \"not spam\" from blacklist");

        RemoteControlTaskParser.RemoteControlTask task = new RemoteControlTaskParser().parse(message);
        assertEquals(ADD_TEXT_TO_BLACKLIST, task.getAction());
        assertEquals("spam", task.getArgument());
    }
*/
}