package com.bopr.android.smailer.messenger

/**
 * Sends informative messages to user.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
interface Messenger {

    fun isEnabled(): Boolean

    fun initialize(): Boolean

    fun sendMessage(
        message: Message,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

}
