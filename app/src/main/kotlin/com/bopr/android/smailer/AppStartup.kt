package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.backup.AppBackupManager.Companion.startAndroidBackup
import com.bopr.android.smailer.control.firebase.FirebaseControlManager.Companion.startFirebaseMessaging
import com.bopr.android.smailer.control.mail.MailControlManager.Companion.enableMailRemoteControl
import com.bopr.android.smailer.control.web.WebServerManager.Companion.enableWebRemoteControl
import com.bopr.android.smailer.provider.telephony.ContentObserverManager.Companion.enableContentObserver
import com.bopr.android.smailer.sync.SyncManager.Companion.startGoogleCloudSync

/**
 * Application startup routines.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
object AppStartup {

    fun Context.startupApplication() {
        enableContentObserver()
        enableMailRemoteControl()
        enableWebRemoteControl()
        startGoogleCloudSync()
        startFirebaseMessaging()
        startAndroidBackup()
    }

}