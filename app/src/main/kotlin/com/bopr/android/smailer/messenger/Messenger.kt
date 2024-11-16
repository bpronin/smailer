package com.bopr.android.smailer.messenger

import android.content.Context
import com.bopr.android.smailer.provider.Event

/**
 * Abstract messenger. Ancestors use specific transport to send messages for events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class Messenger(val context: Context) {

    abstract fun isEnabled(): Boolean

    abstract fun prepare(): Boolean

    abstract fun sendMessage(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

}
