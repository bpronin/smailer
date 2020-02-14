package com.bopr.android.smailer

import android.content.Context
import android.util.Log
import com.bopr.android.smailer.remote.RemoteControlWorker
import com.bopr.android.smailer.sync.SyncManager
import com.bopr.android.smailer.util.Util.requireNonNull
import org.slf4j.LoggerFactory

object Environment {

    private val log = LoggerFactory.getLogger("Environment")

    private fun setUpDefaultExceptionHandler() {
        val defaultHandler = requireNonNull(Thread.getDefaultUncaughtExceptionHandler())
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                log.error("Application crashed", throwable)
            } catch (x: Throwable) {
                Log.e("main", "Failed to handle uncaught exception")
            }
            defaultHandler.uncaughtException(thread, throwable)
        }
    }

    fun setupEnvironment(context: Context) {
        log.debug("Application init")

        setUpDefaultExceptionHandler()

        ContentObserverService.enable(context)
        ResendWorker.enable(context)
        RemoteControlWorker.enable(context)
        SyncManager.enable(context)
    }
}