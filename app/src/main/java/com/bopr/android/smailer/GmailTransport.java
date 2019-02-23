package com.bopr.android.smailer;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.WatchRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.bopr.android.smailer.util.Util.isEmpty;
import static javax.mail.Message.RecipientType.TO;

public class GmailTransport {

    private static Logger log = LoggerFactory.getLogger("GmailTransport");

    public static List<String> SCOPE_SEND = Collections.singletonList(GmailScopes.GMAIL_SEND);
    public static List<String> SCOPE_ALL = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);

    private static final String USER_ID = "me"; /* do not change */
    private static final String UTF_8 = "UTF-8";
    private static final String HTML = "html";

    private final HttpTransport transport;
    private final JacksonFactory jsonFactory;
    private final Context context;
    private Session session;
    private Gmail service;
    private String sender;

    public GmailTransport(Context context) {
        this.context = context;
        jsonFactory = JacksonFactory.getDefaultInstance();
        transport = AndroidHttp.newCompatibleTransport();
    }

    public void init(@NonNull String senderAccount, List<String> scopes) throws IllegalAccessException {
        this.sender = senderAccount;
        service = createService(createCredential(senderAccount, scopes));
        session = Session.getDefaultInstance(new Properties(), null);
    }

    public void send(String subject, String body, @Nullable Collection<File> attachment,
                     @NonNull String recipients, @Nullable String replyTo) throws IOException, MessagingException {
        Message message = createMessage(subject, body, attachment, sender, recipients, replyTo);

        service.users()
                .messages()
                .send(USER_ID, message)
                .execute();

        log.debug("Mail sent");
    }

    public void watch() throws IOException {
        WatchRequest request = new WatchRequest();

        service.users()
                .watch(USER_ID, request)
                .execute();

        log.debug("Watch");
    }

    public void list() throws IOException {
        ListMessagesResponse response = service
                .users()
                .messages()
                .list(USER_ID)
                .execute();

        String id = response.getMessages().get(0).getId();

        Message message = service
                .users()
                .messages()
                .get(USER_ID, id)
                .setFormat("metadata")
                .setMetadataHeaders(Arrays.asList("subject"))
                .execute();

        log.debug(message.getRaw());
    }

    @NonNull
    private Gmail createService(@NonNull GoogleAccountCredential credential) {
        return new Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName("smailer")
                .build();
    }

    @NonNull
    private GoogleAccountCredential createCredential(String accountName, List<String> scopes) throws IllegalAccessException {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, scopes);
        credential.setSelectedAccountName(accountName);
        if (credential.getSelectedAccount() == null) {
            throw new IllegalAccessException("Account does not exist: " + accountName);
        }
        return credential;
    }

    @NonNull
    private Message createMessage(String subject, String body, @Nullable Collection<File> attachment,
                                  @NonNull String sender, @NonNull String recipients, @Nullable String replyTo)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        createMimeMessage(subject, body, attachment, sender, recipients, replyTo).writeTo(buffer);
        return new Message().setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
    }

    @NonNull
    private MimeMessage createMimeMessage(String subject, String body, @Nullable Collection<File> attachment,
                                          @NonNull String sender, @NonNull String recipients, @Nullable String replyTo)
            throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(sender);
        message.setSubject(subject, UTF_8);
        message.setRecipients(TO, parseAddresses(recipients));
        if (!isEmpty(replyTo)) {
            message.setReplyTo(parseAddresses(replyTo));
        }

        if (attachment == null) {
            message.setText(body, UTF_8, HTML);
        } else {
            message.setContent(createMultipart(body, attachment));
        }

        return message;
    }

    private Address[] parseAddresses(String addresses) throws AddressException {
        if (addresses.indexOf(',') > 0) {
            return InternetAddress.parse(addresses);
        } else {
            return new InternetAddress[]{new InternetAddress(addresses)};
        }
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
}

