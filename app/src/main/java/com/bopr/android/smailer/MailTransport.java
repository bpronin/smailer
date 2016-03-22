package com.bopr.android.smailer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * An utility class that sends email using SMTP transport.
 */
public class MailTransport {

    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailTransport() {
    }

    /**
     * Initialize transport with specified properties.
     */
    public void init(String user, String password, String host, String port) {
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

        final PasswordAuthentication authentication = new PasswordAuthentication(user, password);
        session = Session.getInstance(props, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authentication;
            }
        });
    }

    /**
     * Sends email.
     */
    public void send(String subject, String body, String sender,
                     String recipients) throws MessagingException {
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), false));

        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setDataHandler(handler);
        if (recipients.indexOf(',') > 0) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        } else {
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
        }
        Transport.send(message);
    }

    private static class JSSEProvider extends Provider {

        public JSSEProvider() {
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

    private static class ByteArrayDataSource implements DataSource {

        private final String contentType;
        private byte[] data;

        public ByteArrayDataSource(byte[] data, boolean html) {
            this.data = data;
            contentType = html ? "text/plain" : "text/html";
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        @Override
        public String getName() {
            return "ByteArrayDataSource";
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}