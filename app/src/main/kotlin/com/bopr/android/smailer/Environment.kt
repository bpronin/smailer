package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.PendingCallProcessorWorker.Companion.startPendingCallProcessWorker
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControlWorker
import com.bopr.android.smailer.sync.SyncEngine.enableSyncEngine

object Environment {

    /* do not make any member fields. this object dies with its context */

    fun setupEnvironment(context: Context) {
        startPendingCallProcessWorker(context)
        enableSyncEngine(context)
        enableContentObserver(context)
        enableRemoteControlWorker(context)
    }

}