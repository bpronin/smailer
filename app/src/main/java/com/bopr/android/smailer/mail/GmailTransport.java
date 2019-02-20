package com.bopr.android.smailer.mail;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.util.Collections.singletonList;

public class GmailTransport implements MailTransport {

    private static Logger log = LoggerFactory.getLogger("GmailTransport");

    private static final String UTF_8 = "UTF-8";
    private static final String HTML = "html";

    private Context context;
    private Session session;
    private GoogleAccountCredential credential;

    public GmailTransport(Context context) {
        this.context = context;
    }

    @Override
    public void startSession(@NonNull String sender, @NonNull String password, @NonNull String host, @NonNull String port) {
        session = Session.getDefaultInstance(new Properties(), null);
        credential = createCredential(sender);
    }

    @Override
    public void send(String subject, String body, @Nullable Collection<File> attachment,
                     @NonNull String recipients) throws IOException, MessagingException {
        Message message = createMessage(subject, body, attachment, credential.getSelectedAccountName(), recipients);

        Message result = createService()
                .users()
                .messages()
                .send("smailer", message)
                .execute();

        log.debug("Mail sent: " + result.getSnippet());
    }

    private Gmail createService() {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        return new Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName("com.bopr.android.smailer")
                .build();
    }

    private GoogleAccountCredential createCredential(String accountName) {
        List<String> scopes = singletonList(GmailScopes.GMAIL_SEND);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, scopes);
        credential.setSelectedAccountName(accountName);
        return credential;
    }

    @NonNull
    private Message createMessage(String subject, String body, @Nullable Collection<File> attachment,
                                  @NonNull String sender, @NonNull String recipients) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        createMimeMessage(subject, body, attachment, sender, recipients).writeTo(buffer);
        return new Message().setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
    }

    @NonNull
    private MimeMessage createMimeMessage(String subject, String body, @Nullable Collection<File> attachment,
                                          @NonNull String sender, @NonNull String recipients) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject, UTF_8);

        if (recipients.indexOf(',') > 0) {
            message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(recipients));
        } else {
            message.setRecipients(javax.mail.Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(recipients)});
        }

        if (attachment == null) {
            message.setText(body, UTF_8, HTML);
        } else {
            message.setContent(createMultipart(body, attachment));
        }

        return message;
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

