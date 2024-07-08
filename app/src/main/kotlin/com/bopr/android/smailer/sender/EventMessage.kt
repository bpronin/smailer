package com.bopr.android.smailer.sender

/**
 * A message to be sent to user using configured messaging transports.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
data class EventMessage(
    val id: String? = null,
    val subject: String? = null,
    val text: String
)