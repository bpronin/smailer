package com.bopr.android.smailer;

import android.support.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * An utility class that sends email using SMTP transport.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailTransport {

    private static Logger log = LoggerFactory.getLogger("MailTransport");

    public static final int CHECK_RESULT_OK = 0;
    public static final int CHECK_RESULT_NOT_CONNECTED = 1;
    public static final int CHECK_RESULT_AUTHENTICATION = 2;

    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailTransport() {
    }

    /**
     * Starts new delivery session.
     */
    public void startSession(String user, String password, String host, String port) {
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
//        props.put("mail.smtp.timeout", 10000);

        session = Session.getInstance(props, new PasswordAuthenticator(user, password));
    }

    /**
     * Sends email.
     */
    public void send(String subject, String body, String sender, String recipients)
            throws MessagingException {
        send(subject, body, null, sender, recipients);
    }

    public void send(String subject, String body, File[] attachment, String sender,
                     String recipients) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setRecipients(Message.RecipientType.TO, getRecipients(recipients));
        message.setContent(getContent(body, attachment));

        Transport.send(message);
    }

    @NonNull
    private Multipart getContent(String body, File[] attachment) throws MessagingException {
        Multipart content = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(body, "text/html");
        content.addBodyPart(textPart);

        if (attachment != null) {
            for (File file : attachment) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setFileName(file.getName());
                attachmentPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                content.addBodyPart(attachmentPart);
            }
        }

        return content;
    }

    @NonNull
    private InternetAddress[] getRecipients(String recipients) throws AddressException {
        if (recipients.indexOf(',') > 0) {
            return InternetAddress.parse(recipients);
        } else {
            return new InternetAddress[]{new InternetAddress(recipients)};
        }
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

    private static class PasswordAuthenticator extends Authenticator {

        private final String user;
        private final String password;

        PasswordAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
        }
    }
}