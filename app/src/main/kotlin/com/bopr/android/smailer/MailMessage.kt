package com.bopr.android.smailer

import java.io.File

/**
 * Email message.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
data class MailMessage(
        var id: String? = null,
        var subject: String? = null,
        var body: String? = null,
        var attachment: Collection<File>? = null,
        var recipients: String? = null,
        var replyTo: String? = null,
        var from: String? = null)