package com.bopr.android.smailer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.util.Util;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2;
import static com.google.api.client.util.Base64.decodeBase64;
import static com.google.api.client.util.StringUtils.newStringUtf8;
import static java.util.Arrays.asList;
import static javax.mail.Message.RecipientType.TO;

/**
 * Gmail mail transport.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GoogleMailSupport {

    private static Logger log = LoggerFactory.getLogger("GoogleMailSupport");

    private static final String ME = "me"; /* exact "me" */
    private static final String UTF_8 = "UTF-8";
    private static final String HTML = "html";

    private final HttpTransport transport;
    private final Context context;
    private Session session;
    private Gmail service;
    private String sender;

    public GoogleMailSupport(Context context) {
        this.context = context;
        transport = AndroidHttp.newCompatibleTransport();
    }

    public void init(@NonNull String accountName, String... scopes) {
        this.sender = accountName;
        service = createService(createCredential(accountName, scopes));
        session = Session.getDefaultInstance(new Properties(), null);
    }

    public void send(@NonNull MailMessage message) throws IOException, MessagingException {
        service.users()
                .messages()
                .send(ME, createGmailMessage(message))
                .execute();

        log.debug("Message sent");
    }

    public List<MailMessage> list(String query) throws IOException {
        ListMessagesResponse response = service
                .users()
                .messages()
                .list(ME)
                .setQ(query)
                .execute();

        LinkedList<MailMessage> result = new LinkedList<>();
        List<Message> messages = response.getMessages();
        if (messages != null) {
            for (Message m : messages) {
                Message message = service
                        .users()
                        .messages()
                        .get(ME, m.getId())
                        .execute();

                result.add(readMessage(message));
            }
        }
        return result;
    }

    void markAsRead(MailMessage message) throws IOException {
        ModifyMessageRequest content = new ModifyMessageRequest()
                .setRemoveLabelIds(ImmutableList.of("UNREAD")); /* case sensitive */
        service.users()
                .messages()
                .modify(ME, message.getId(), content)
                .execute();

        log.debug("Message marked as read: " + message.getId());
    }

    void trash(MailMessage message) throws IOException {
        service.users()
                .messages()
                .trash(ME, message.getId())
                .execute();

        log.debug("Message moved to trash: " + message.getId());
    }

    @NonNull
    private GoogleAccountCredential createCredential(String accountName, String... scopes) {
        GoogleAccountCredential credential = usingOAuth2(context, asList(scopes));
        credential.setSelectedAccountName(accountName);
        if (credential.getSelectedAccount() == null) {
            throw new IllegalArgumentException("Account does not exist: " + accountName);
        }
        return credential;
    }

    @NonNull
    private Gmail createService(@NonNull GoogleAccountCredential credential) {
        return new Gmail.Builder(transport, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("smailer")
                .build();
    }

    @NonNull
    private Message createGmailMessage(MailMessage message) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        createMimeMessage(message, sender).writeTo(buffer);
        return new Message().encodeRaw(buffer.toByteArray());
    }

    @NonNull
    private MimeMessage createMimeMessage(@NonNull MailMessage message, @NonNull String sender)
            throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(sender);
        mimeMessage.setSubject(message.getSubject(), UTF_8);
        mimeMessage.setRecipients(TO, parseAddresses(message.getRecipients()));
        if (!Util.isEmpty(message.getReplyTo())) {
            mimeMessage.setReplyTo(parseAddresses(message.getReplyTo()));
        }

        if (message.getAttachment() == null) {
            mimeMessage.setText(message.getBody(), UTF_8, HTML);
        } else {
            mimeMessage.setContent(createMultipart(message.getBody(), message.getAttachment()));
        }

        return mimeMessage;
    }

    @NonNull
    private Multipart createMultipart(String body, @NonNull Collection<File> attachment) throws MessagingException {
        Multipart content = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body, UTF_8, HTML);
        content.addBodyPart(textPart);

        for (File file : attachment) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setFileName(file.getName());
            attachmentPart.setDataHandler(new DataHandler(new FileDataSource(file)));
            content.addBodyPart(attachmentPart);
        }

        return content;
    }

    private Address[] parseAddresses(@NonNull String addresses) throws AddressException {
        if (addresses.indexOf(',') > 0) {
            return InternetAddress.parse(addresses);
        } else {
            return new InternetAddress[]{new InternetAddress(addresses)};
        }
    }

/*
    private MimeMessage toMimeMessage(Message message) throws MessagingException {
        String raw = newStringUtf8(decodeBase64(message.getRaw()));
        return new MimeMessage(session, new ByteArrayInputStream(raw.getBytes()));
    }
*/

    private MailMessage readMessage(Message gmailMessage) {
        MailMessage message = new MailMessage();
        message.setId(gmailMessage.getId());
        message.setSubject(readHeader(gmailMessage, "subject"));
        message.setFrom(readHeader(gmailMessage, "from"));
        message.setBody(readBody(gmailMessage));
        return message;
    }

    @Nullable
    private String readHeader(Message message, String name) {
        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if (header.getName().equalsIgnoreCase(name)) {
                return header.getValue();
            }
        }
        return null;
    }

    @Nullable
    private String readBody(Message message) {
        MessagePart payload = message.getPayload();
        if (payload != null) {
            List<MessagePart> parts = payload.getParts();
            if (parts == null) {
                return newStringUtf8(decodeBase64(payload.getBody().getData()));
            } else if (!parts.isEmpty()) {
                return newStringUtf8(decodeBase64(parts.get(0).getBody().getData()));
            }
        }
        return null;
    }

}

