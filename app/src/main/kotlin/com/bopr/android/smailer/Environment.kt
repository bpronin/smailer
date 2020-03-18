package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.PendingCallProcessorWorker.Companion.startPendingCallProcessing
import com.bopr.android.smailer.firebase.CloudMessaging.subscribeToCloudMessaging
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControl
import com.bopr.android.smailer.sync.SyncWorker.Companion.requestDataSync

object Environment {

    fun Context.startApplicationServices() {
        enableContentObserver()
        startPendingCallProcessing()
        enableRemoteControl()
        requestDataSync()
        subscribeToCloudMessaging()
    }

}