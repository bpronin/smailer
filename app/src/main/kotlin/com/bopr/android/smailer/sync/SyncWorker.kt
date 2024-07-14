package com.bopr.android.smailer.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy.KEEP
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.AccountManager
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.external.Firebase
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_NORMAL
import org.slf4j.LoggerFactory

/**
 * Worker used in synchronization application data with google drive.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SyncWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val settings = Settings(applicationContext)
        val accountManager = AccountManager(applicationContext)

        if (settings.isCloudSyncEnabled()) {
            accountManager.getGoogleAccount(settings.getSenderAccountName())?.let { account ->
                Database(applicationContext).use { database ->
                    Synchronizer(applicationContext, account, database).run {
                        val mode = inputData.getInt(SYNC_OPTIONS, SYNC_NORMAL)
                        if (sync(mode)) {
                            Firebase(applicationContext).send(FCM_REQUEST_DATA_SYNC)
                        }
                    }
                }
            } ?: log.warn("No sync account")
        }
        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("SyncWorker")
        private const val WORK_SYNC = "com.bopr.android.smailer.sync"
        private const val SYNC_OPTIONS = "options"

        internal fun Context.syncAppDataWithGoogleCloud(mode: Int = SYNC_NORMAL) {
            if (Settings(this).isCloudSyncEnabled()) {
                log.debug("Sync requested in mode: $mode")

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()

                val data = Data.Builder()
                    .putInt(SYNC_OPTIONS, mode)
                    .build()

                val request = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build()

                WorkManager.getInstance(this).enqueueUniqueWork(WORK_SYNC, KEEP, request)
            }
        }

    }
}