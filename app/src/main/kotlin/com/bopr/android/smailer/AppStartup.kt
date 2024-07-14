package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.external.Firebase.Companion.subscribeToFirebaseMessaging
import com.bopr.android.smailer.provider.telephony.ContentObserverService.Companion.startContentObserver
import com.bopr.android.smailer.provider.telephony.PendingEventsProcessorWorker.Companion.startPendingEventsProcessing
import com.bopr.android.smailer.control.MailControlWorker.Companion.enableMailRemoteControl
import com.bopr.android.smailer.sync.SyncWorker.Companion.syncAppDataWithGoogleCloud

/**
 * Application startup routines.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object AppStartup {

    fun Context.startUpAppServices() {
        startContentObserver()
        startPendingEventsProcessing()
        enableMailRemoteControl()
        syncAppDataWithGoogleCloud()
        subscribeToFirebaseMessaging()
    }

}