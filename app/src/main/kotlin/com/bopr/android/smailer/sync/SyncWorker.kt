package com.bopr.android.smailer.sync

import android.content.Context
import androidx.work.*
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.util.getAccount
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SyncWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        val settings = Settings(applicationContext)
        if (settings.getBoolean(PREF_SYNC_ENABLED)) {
            applicationContext.getAccount(settings.getString(PREF_SENDER_ACCOUNT))?.run {
                Database(applicationContext).use {
                    val synchronizer = Synchronizer(applicationContext, this, it)
                    if (inputData.getBoolean(DATA_FORCE_DOWNLOAD, false)) {
                        synchronizer.download()
                    } else {
                        synchronizer.sync()
                    }
                }

            } ?: log.warn("No sync account")
        }
        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("SyncWorker")
        private const val WORK_SYNC = "com.bopr.android.smailer.sync"
        private const val WORK_PERIODIC_SYNC = "com.bopr.android.smailer.periodic_sync"
        private const val DATA_FORCE_DOWNLOAD = "force_download"

        private fun constraints(): Constraints {
            return Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
        }

        internal fun runSyncWork(context: Context, forceDownload: Boolean = false) {
            log.debug("Sync requested")

            val data = Data.Builder()
                    .putBoolean(DATA_FORCE_DOWNLOAD, forceDownload)
                    .build()
            val request = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                    .setConstraints(constraints())
                    .setInputData(data)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_SYNC,
                    ExistingWorkPolicy.KEEP, request)
        }

        internal fun runPeriodicSyncWork(context: Context) {
            log.debug("Start")

            val request = PeriodicWorkRequest.Builder(SyncWorker::class.java,
                            MIN_PERIODIC_INTERVAL_MILLIS, MILLISECONDS)
                    .setConstraints(constraints())
                    .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_PERIODIC_SYNC,
                    ExistingPeriodicWorkPolicy.REPLACE, request)
        }

        internal fun cancelPeriodicSyncWork(context: Context) {
            log.debug("Stop")

            WorkManager.getInstance(context).cancelUniqueWork(WORK_PERIODIC_SYNC)
        }
    }
}