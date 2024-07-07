package com.bopr.android.smailer.sender

import java.io.File

/**
 * Email message.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
data class MailMessage(
        val id: String? = null,
        val subject: String? = null,
        val body: String? = null,
        val attachment: Collection<File>? = null,
        val recipients: String? = null,
        val replyTo: String? = null,
        val from: String? = null)