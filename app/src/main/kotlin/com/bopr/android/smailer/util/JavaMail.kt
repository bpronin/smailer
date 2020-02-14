package com.bopr.android.smailer.util

import org.slf4j.LoggerFactory
import java.io.File
import java.security.AccessController
import java.security.PrivilegedAction
import java.security.Provider
import java.security.Security
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.Message.RecipientType.TO
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@Suppress("unused")
object JavaMail {

    private val log = LoggerFactory.getLogger("JavaMailTransport")
    private const val UTF_8 = "UTF-8"
    private const val HTML = "html"
    const val CHECK_RESULT_OK = 0
    const val CHECK_RESULT_NOT_CONNECTED = 1
    const val CHECK_RESULT_AUTHENTICATION = 2

    private lateinit var account: String
    private lateinit var session: Session

    init {
        Security.addProvider(JSSEProvider())
    }

    /**
     * Starts new delivery session.
     */
    fun startSession(account: String, password: String, host: String, port: Int) {
        this.account = account

        val props = Properties()
        props["mail.transport.protocol"] = "smtp"
        props["mail.host"] = host
        props["mail.smtp.port"] = port
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.socketFactory.port"] = port
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = false
        props["mail.smtp.quitwait"] = false
        props["mail.smtp.connectiontimeout"] = 10000

        val authentication = PasswordAuthentication(account, password)
        session = Session.getInstance(props, object : Authenticator() {

            override fun getPasswordAuthentication(): PasswordAuthentication {
                return authentication
            }
        })
    }

    /**
     * Sends email with attachment.
     */
    @Throws(MessagingException::class)
    fun send(recipients: String, subject: String?, body: String?, attachment: Collection<File>?) {
        val message = MimeMessage(session).apply {
            sender = InternetAddress(account)
            setSubject(subject, UTF_8)
        }

        if (recipients.indexOf(',') > 0) {
            message.setRecipients(TO, InternetAddress.parse(recipients))
        } else {
            message.setRecipients(TO, arrayOf(InternetAddress(recipients)))
        }

        if (attachment == null) {
            message.setText(body, UTF_8, HTML)
        } else {
            message.setContent(createMultipart(body, attachment))
        }

        Transport.send(message)
    }

    /**
     * Checks connection to mail server.
     */
    fun checkConnection(): Int {
        log.debug("checking connection")

        try {
            val transport = session.transport
            try {
                transport.connect()
                return CHECK_RESULT_OK
            } finally {
                try {
                    transport.close()
                } catch (x: MessagingException) {
                    log.warn("Closing transport failed", x)
                }
            }
        } catch (x: AuthenticationFailedException) {
            log.debug("Authentication failed", x)

            return CHECK_RESULT_AUTHENTICATION
        } catch (x: MessagingException) {
            log.debug("Connection failed", x)

            return CHECK_RESULT_NOT_CONNECTED
        }
    }

    @Throws(MessagingException::class)
    private fun createMultipart(body: String?, attachment: Collection<File>): Multipart {
        val content: Multipart = MimeMultipart()

        val textPart = MimeBodyPart().apply {
            setText(body, UTF_8, HTML)
        }
        content.addBodyPart(textPart)

        for (file in attachment) {
            val attachmentPart = MimeBodyPart().apply {
                fileName = file.name
                dataHandler = DataHandler(FileDataSource(file))
            }
            content.addBodyPart(attachmentPart)
        }

        return content
    }

    private class JSSEProvider : Provider("HarmonyJSSE", 1.0, "Harmony JSSE Provider") {

        init {
            AccessController.doPrivileged<Void>(PrivilegedAction<Void?> {
                put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl")
                put("Alg.Alias.SSLContext.TLSv1", "TLS")
                put("KeyManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl")
                put("TrustManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl")
                null
            })
        }
    }
}