package com.bopr.android.smailer

import android.content.Context
import android.util.Log
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.ResendWorker.Companion.enableResendWorker
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControlWorker
import com.bopr.android.smailer.sync.SyncEngine.Companion.startSyncEngine
import org.slf4j.LoggerFactory

object Environment {

    private val log = LoggerFactory.getLogger("Environment")

    fun setupEnvironment(context: Context) {
        log.debug("Application init")

        setupDefaultExceptionHandler()
        startSyncEngine(context)
        enableContentObserver(context)
        enableResendWorker(context)
        enableRemoteControlWorker(context)
        //todo try to put settings listeners here
    }

    private fun setupDefaultExceptionHandler() {
        val defaultHandler = requireNotNull(Thread.getDefaultUncaughtExceptionHandler())
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                log.error("Application crashed", throwable)
            } catch (x: Throwable) {
                Log.e("main", "Failed to handle uncaught exception")
            }
            defaultHandler.uncaughtException(thread, throwable)
        }
    }

}