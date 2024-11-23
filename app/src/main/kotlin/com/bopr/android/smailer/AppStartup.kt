package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.control.FirebaseControlService.Companion.startFirebaseMessaging
import com.bopr.android.smailer.control.MailRemoteControlWorker.Companion.enableMailRemoteControl
import com.bopr.android.smailer.provider.telephony.ContentObserverService.Companion.startContentObserver
import com.bopr.android.smailer.sync.Synchronizer.Companion.startGoogleCloudSync

/**
 * Application startup routines.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object AppStartup {

    fun Context.startupApplication() {
        startContentObserver()
        enableMailRemoteControl()
        startGoogleCloudSync()
        startFirebaseMessaging()
    }

}