package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.PendingCallProcessorWorker.Companion.startPendingCallProcessorWorker
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControlWorker
import com.bopr.android.smailer.sync.SyncManager.Companion.setupSyncEngine

object Environment {

    /* do not make any member fields. this object dies with its context */

    fun setupEnvironment(context: Context) {
        startPendingCallProcessorWorker(context)
        enableRemoteControlWorker(context)
        enableContentObserver(context)
        setupSyncEngine(context)
    }

}