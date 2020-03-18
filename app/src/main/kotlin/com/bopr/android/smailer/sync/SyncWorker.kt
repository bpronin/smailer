package com.bopr.android.smailer.sync

import android.content.Context
import androidx.work.*
import androidx.work.NetworkType.CONNECTED
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.firebase.CloudMessaging.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.firebase.CloudMessaging.sendCloudMessage
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_NORMAL
import com.bopr.android.smailer.util.getAccount
import org.slf4j.LoggerFactory

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SyncWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        applicationContext.run {
            val settings = Settings(this)
            if (settings.isSyncEnabled) {
                getAccount(settings.senderAccount)?.let { account ->
                    Database(this).use { database ->
                        Synchronizer(this, account, database).run {
                            val options = inputData.getInt(SYNC_OPTIONS, SYNC_NORMAL)
                            if (sync(options)) {
                                sendCloudMessage(FCM_REQUEST_DATA_SYNC)
                            }
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
        private const val SYNC_OPTIONS = "options"

        private fun constraints() = Constraints.Builder()
                .setRequiredNetworkType(CONNECTED)
                .build()

        internal fun Context.requestDataSync(options: Int = SYNC_NORMAL) {
            if (Settings(this).isSyncEnabled) {
                log.debug("Sync requested. Option: $options")

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

    }
}