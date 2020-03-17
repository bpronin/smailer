package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.PendingCallProcessorWorker.Companion.startPendingCallProcessing
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControl
import com.bopr.android.smailer.sync.SyncWorker.Companion.enablePeriodicDataSync

object Environment {

    fun Context.startApplicationServices() {
        enableContentObserver()
        startPendingCallProcessing()
        enableRemoteControl()
        enablePeriodicDataSync()
    }

}