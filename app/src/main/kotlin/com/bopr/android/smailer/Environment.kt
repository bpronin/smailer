package com.bopr.android.smailer

import android.content.Context
import android.util.Log
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.ResendWorker.Companion.enableResendWorker
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControlWorker
import com.bopr.android.smailer.sync.SyncEngine.Companion.startSyncEngine
import org.slf4j.LoggerFactory.getLogger

object Environment {

     /* do not make any member fields. this object dies with its context */

    fun setupEnvironment(context: Context) {
        getLogger("Application").debug("Application init")

        setupDefaultExceptionHandler()
        startSyncEngine(context)
        enableContentObserver(context)
        enableResendWorker(context)
        enableRemoteControlWorker(context)
    }

    private fun setupDefaultExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                getLogger("Application").error("Application crashed", throwable)
            } catch (x: Throwable) {
                Log.e("main", "Failed to handle uncaught exception")
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

}