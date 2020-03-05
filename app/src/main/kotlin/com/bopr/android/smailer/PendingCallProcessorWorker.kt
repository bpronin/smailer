package com.bopr.android.smailer

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService.enqueueWork
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
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
        private const val JOB_ID = 1000

        fun startPendingCallProcessorService(context: Context) {
            log.debug("Starting service")

            enqueueWork(context, PendingCallProcessorService::class.java, JOB_ID,
                    Intent(context, PendingCallProcessorService::class.java))
        }

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