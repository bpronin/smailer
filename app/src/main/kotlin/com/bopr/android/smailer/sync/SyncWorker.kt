package com.bopr.android.smailer.sync

import android.content.Context
import androidx.work.*
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_NORMAL
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
        applicationContext.run {
            if (isFeatureEnabled()) {
                getAccount(Settings(this).senderAccount)?.let { account ->
                    Database(this).use { database ->
                        Synchronizer(this, account, database).run {
                            sync(inputData.getInt(SYNC_OPTIONS, SYNC_NORMAL))
                        }
                    }
                } ?: log.warn("No sync account")
            }
        }
        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("SyncWorker")
        private const val WORK_SYNC = "com.bopr.android.smailer.sync"
        private const val WORK_PERIODIC_SYNC = "com.bopr.android.smailer.periodic_sync"
        private const val SYNC_OPTIONS = "options"

        private fun Context.isFeatureEnabled() = Settings(this).isSyncEnabled

        private fun constraints() = Constraints.Builder()
                .setRequiredNetworkType(CONNECTED)
                .build()

        internal fun Context.requestDataSync(options: Int = SYNC_NORMAL) {
            if (isFeatureEnabled()) {
                log.debug("Sync requested")

                val data = Data.Builder()
                        .putInt(SYNC_OPTIONS, options)
                        .build()
                val request = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                        .setConstraints(constraints())
                        .setInputData(data)
                        .build()
                WorkManager.getInstance(this).enqueueUniqueWork(WORK_SYNC,
                        ExistingWorkPolicy.KEEP, request)
            }
        }

        internal fun Context.enablePeriodicDataSync() {
            if (isFeatureEnabled()) {
                log.debug("Start periodic sync")

                val request = PeriodicWorkRequest.Builder(SyncWorker::class.java,
                                MIN_PERIODIC_INTERVAL_MILLIS, MILLISECONDS)
                        .setConstraints(constraints())
                        .build()
                WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_PERIODIC_SYNC,
                        ExistingPeriodicWorkPolicy.REPLACE, request)
            } else {
                log.debug("Stop periodic sync")

                WorkManager.getInstance(this).cancelUniqueWork(WORK_PERIODIC_SYNC)
            }
        }

    }
}