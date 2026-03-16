package com.bopr.android.smailer.messenger


/**
 * Sends informative messages to user.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
interface Messenger {

    suspend fun prepare(): Boolean

    suspend fun send(event: Event, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

}
