package com.bopr.android.smailer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * An utility class that sends email using SMTP transport.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class JavaMailTransport {

    private static Logger log = LoggerFactory.getLogger("JavaMailTransport");

    private static final String UTF_8 = "UTF-8";
    private static final String HTML = "html";

    public static final int CHECK_RESULT_OK = 0;
    public static final int CHECK_RESULT_NOT_CONNECTED = 1;
    public static final int CHECK_RESULT_AUTHENTICATION = 2;

    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    private String sender;

    public JavaMailTransport() {
    }

    /**
     * Starts new delivery session.
     */
    public void startSession(@NonNull String account, @NonNull String password, @NonNull String host,
                             int port) {
        this.sender = account;

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", false);
        props.put("mail.smtp.quitwait", false);
        props.put("mail.smtp.connectiontimeout", 10000);

        final PasswordAuthentication authentication = new PasswordAuthentication(account, password);
        session = Session.getInstance(props, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authentication;
            }
        });
    }

    /**
     * Sends email with attachment.
     */
    public void send(@Nullable String subject, @Nullable String body,
                     @Nullable Collection<File> attachment, @NonNull String recipients) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject, UTF_8);

        if (recipients.indexOf(',') > 0) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        } else {
            message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(recipients)});
        }

        if (attachment == null) {
            message.setText(body, UTF_8, HTML);
        } else {
            message.setContent(createMultipart(body, attachment));
        }

        Transport.send(message);
    }

    /**
     * Checks connection to mail server.
     */
    public int checkConnection() {
        log.trace("checking connection");
        try {
            Transport transport = session.getTransport();
            try {
                transport.connect();
                return CHECK_RESULT_OK;
            } finally {
                try {
                    transport.close();
                } catch (MessagingException x) {
                    log.warn("closing transport failed", x);
                }
            }
        } catch (AuthenticationFailedException x) {
            log.debug("authentication failed", x);
            return CHECK_RESULT_AUTHENTICATION;
        } catch (MessagingException x) {
            log.debug("connection failed", x);
            return CHECK_RESULT_NOT_CONNECTED;
        }
    }

    @NonNull
    private Multipart createMultipart(String body, Collection<File> attachment) throws MessagingException {
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

    private static class JSSEProvider extends Provider {

        private JSSEProvider() {
            super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");

            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                public Void run() {
                    put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                    put("Alg.Alias.SSLContext.TLSv1", "TLS");
                    put("KeyManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                    put("TrustManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                    return null;
                }

            });
        }
    }

}