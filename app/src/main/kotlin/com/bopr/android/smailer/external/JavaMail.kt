package com.bopr.android.smailer.external

import com.bopr.android.smailer.consumer.mail.MailMessage
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
    private const val CHECK_RESULT_OK = 0
    private const val CHECK_RESULT_NOT_CONNECTED = 1
    private const val CHECK_RESULT_AUTHENTICATION = 2

    init {
        Security.addProvider(JSSEProvider())
    }

    /**
     * Sends email with attachment.
     */
    @Throws(MessagingException::class)
    fun send(account: String, password: String, smtpHost: String, smtpPort: Int = 465,
             vararg messages: MailMessage
    ) {
        val session = startSession(smtpHost, smtpPort, account, password)

        val transport = session.transport
        try {
            transport.connect()
            for (message in messages) {
                val mimeMessage = createMimeMessage(message, session)
                transport.sendMessage(mimeMessage, mimeMessage.getRecipients(TO))
            }
        } finally {
            try {
                transport.close()
            } catch (x: MessagingException) {
                log.warn("Closing transport failed", x)
            }
        }
    }

    private fun startSession(
        smtpHost: String,
        smtpPort: Int,
        account: String,
        password: String
    ): Session {
        val props = Properties()
        props["mail.transport.protocol"] = "smtp"
        props["mail.host"] = smtpHost
        props["mail.smtp.port"] = smtpPort
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.socketFactory.port"] = smtpPort
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = false
        props["mail.smtp.quitwait"] = false
        props["mail.smtp.connectiontimeout"] = 10000

        val session = Session.getInstance(props, object : Authenticator() {

            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(account, password)
            }
        })
        return session
    }

    private fun createMimeMessage(m: MailMessage, session: Session): Message {
        return MimeMessage(session).apply {
            sender = InternetAddress(m.from)
            setSubject(m.subject, UTF_8)

            m.recipients?.let {
                if (it.indexOf(',') > 0) {
                    setRecipients(TO, InternetAddress.parse(it))
                } else {
                    setRecipients(TO, arrayOf(InternetAddress(it)))
                }
            }

            m.attachment?.let {
                setContent(createMultipart(m.body, m.attachment))
            } ?: run {
                setText(m.body, UTF_8, HTML)
            }
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
            AccessController.doPrivileged<Void>(PrivilegedAction {
                put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl")
                put("Alg.Alias.SSLContext.TLSv1", "TLS")
                put(
                    "KeyManagerFactory.X509",
                    "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl"
                )
                put(
                    "TrustManagerFactory.X509",
                    "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl"
                )
                null
            })
        }
    }
}