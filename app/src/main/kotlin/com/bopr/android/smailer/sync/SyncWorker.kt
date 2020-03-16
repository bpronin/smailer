package com.bopr.android.smailer.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.sync.SyncService.Companion.startSyncService
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SyncWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        startSyncService(applicationContext)
        return Result.success()
    }

    private class DatabaseListener : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            log.debug("Sync requested")
            startSyncService(context)
        }
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("SyncWorker")
        private const val WORKER_TAG = "com.bopr.android.smailer.sync"
        private val databaseListener = DatabaseListener()

        fun enableSyncWorker(context: Context) {
            val manager = WorkManager.getInstance(context)

            manager.cancelAllWorkByTag(WORKER_TAG)

            if (Settings(context).getBoolean(PREF_SYNC_ENABLED)) {
                val constraints = Constraints.Builder()
                        .setRequiredNetworkType(CONNECTED)
                        .build()
                val request = PeriodicWorkRequest.Builder(SyncWorker::class.java,
                                15, MINUTES) /* must be greater than [PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS] */
                        .addTag(WORKER_TAG)
                        .setConstraints(constraints)
                        .build()
                manager.enqueueUniquePeriodicWork(WORKER_TAG, REPLACE, request)

                context.registerDatabaseListener(databaseListener)

                log.debug("Enabled")
            } else {
                context.unregisterDatabaseListener(databaseListener)

                log.debug("Disabled")
            }
        }
    }
}