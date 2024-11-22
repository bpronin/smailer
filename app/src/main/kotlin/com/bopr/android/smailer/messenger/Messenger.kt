package com.bopr.android.smailer.messenger


/**
 * Sends informative messages to user.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
interface Messenger {

    fun initialize(): Boolean

    fun sendMessage(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

}
