package com.bopr.android.smailer

import android.content.Context
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import com.bopr.android.smailer.PendingCallProcessorService.Companion.startPendingCallProcessorService
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class PendingCallProcessorWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        startPendingCallProcessorService(applicationContext)
        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("ResendWorker")
        private const val WORKER_TAG = "com.bopr.android.smailer.resend"

        fun startPendingCallProcessWorker() {
            val manager = WorkManager.getInstance()

            manager.cancelAllWorkByTag(WORKER_TAG)

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val request = PeriodicWorkRequest.Builder(PendingCallProcessorWorker::class.java,
                    15, TimeUnit.MINUTES) /* interval must be greater than [PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS] */
                    .addTag(WORKER_TAG)
                    .setConstraints(constraints)
                    .build()
            manager.enqueueUniquePeriodicWork(WORKER_TAG, REPLACE, request)

            log.debug("Enabled")
        }
    }
}