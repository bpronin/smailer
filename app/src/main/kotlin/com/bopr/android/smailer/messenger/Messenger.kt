package com.bopr.android.smailer.messenger

import android.content.Context

/**
 * Abstract messenger. Ancestors use specific transport to send messages.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class Messenger(protected val context: Context) {

    abstract fun isEnabled(): Boolean

    abstract fun prepare(): Boolean

    abstract fun sendMessage(
        message: Message,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

}
