package com.bopr.android.smailer

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.util.Mockable
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Base64
import com.google.api.client.util.StringUtils
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.ModifyMessageRequest
import com.google.common.collect.ImmutableList
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message.RecipientType.TO
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.*

/**
 * Gmail mail transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class GoogleMail(private val context: Context) {

    private val log = LoggerFactory.getLogger("GoogleMail")
    private lateinit var service: Gmail
    private lateinit var account: Account

    fun login(account: Account, scope: String) {
        this.account = account
        val credential = GoogleAccountCredential
                .usingOAuth2(context, listOf(scope))
                .setSelectedAccount(account)
        service = Gmail.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("smailer")
                .build()
    }

    @Throws(IOException::class)
    fun send(message: MailMessage) {
        service.users()
                .messages()
                .send(ME, createContent(message))
                .execute()

        log.debug("Message sent")
    }

    @Throws(IOException::class)
    fun list(query: String): List<MailMessage> {
        val response = service
                .users()
                .messages()
                .list(ME)
                .setQ(query)
                .execute()

        val result = LinkedList<MailMessage>()
        response.messages?.let {
            for (m in it) {
                val message = service
                        .users()
                        .messages()[ME, m.id]
                        .execute()
                result.add(readMessage(message))
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun markAsRead(message: MailMessage) {
        val content = ModifyMessageRequest()
                .setRemoveLabelIds(ImmutableList.of("UNREAD")) /* case sensitive */
        service.users()
                .messages()
                .modify(ME, message.id, content)
                .execute()

        log.debug("Message marked as read: " + message.id)
    }

    @Throws(IOException::class)
    fun trash(message: MailMessage) {
        service.users()
                .messages()
                .trash(ME, message.id)
                .execute()

        log.debug("Message moved to trash: " + message.id)
    }

    private fun createContent(message: MailMessage): Message {
        try {
            val session = Session.getDefaultInstance(Properties(), null)
            val mime = MimeMessage(session).apply {
                setFrom(message.from)
                setSubject(message.subject, UTF_8)
                setRecipients(TO, parseAddresses(message.recipients!!))
                if (!message.replyTo.isNullOrEmpty()) {
                    replyTo = parseAddresses(message.replyTo)
                }
                if (!message.attachment.isNullOrEmpty()) {
                    setContent(createMultipart(message.body, message.attachment))
                } else {
                    setText(message.body, UTF_8, HTML)
                }
            }
            
            val buffer = ByteArrayOutputStream()
            mime.writeTo(buffer)
            return Message().encodeRaw(buffer.toByteArray())

        } catch (x: IOException) {
            throw RuntimeException("Message creation failed", x)
        } catch (x: MessagingException) {
            throw RuntimeException("Message creation failed", x)
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
            val filePart = MimeBodyPart().apply {
                fileName = file.name
                dataHandler = DataHandler(FileDataSource(file))
            }
            content.addBodyPart(filePart)
        }

        return content
    }

    @Throws(AddressException::class)
    private fun parseAddresses(addresses: String): Array<out InternetAddress> {
        return if (addresses.indexOf(',') > 0) {
            InternetAddress.parse(addresses)
        } else {
            arrayOf(InternetAddress(addresses))
        }
    }

    private fun readMessage(gmailMessage: Message): MailMessage {
        return MailMessage(
                id = gmailMessage.id,
                subject = readHeader(gmailMessage, "subject"),
                from = readHeader(gmailMessage, "from"),
                body = readBody(gmailMessage)
        )
    }

    private fun readHeader(message: Message, name: String): String? {
        for (header in message.payload.headers) {
            if (header.name.equals(name, true)) {
                return header.value
            }
        }
        return null
    }

    private fun readBody(message: Message): String? {
        return message.payload?.let {
            val parts = it.parts
            val part = if (parts == null || parts.isEmpty()) it else parts[0]
            return StringUtils.newStringUtf8(Base64.decodeBase64(part.body.data))
        }
    }

    companion object {

        private const val ME = "me" /* exact lowercase "me" */
        private const val UTF_8 = "UTF-8"
        private const val HTML = "html"
    }

}