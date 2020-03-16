package com.bopr.android.smailer

import android.content.Context
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class PendingCallProcessorWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        CallProcessor(applicationContext).processPending()
        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("PendingCallProcessorWorker")
        private const val WORKER_TAG = "com.bopr.android.smailer.resend"

        fun startPendingCallProcessorWorker(context: Context) {
            val manager = WorkManager.getInstance(context)

            manager.cancelAllWorkByTag(WORKER_TAG)

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
            val request = PeriodicWorkRequest.Builder(PendingCallProcessorWorker::class.java,
                            15, MINUTES) /* must be greater than [PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS] */
                    .addTag(WORKER_TAG)
                    .setConstraints(constraints)
                    .build()
            manager.enqueueUniquePeriodicWork(WORKER_TAG, REPLACE, request)

            log.debug("Enabled")
        }
    }
}