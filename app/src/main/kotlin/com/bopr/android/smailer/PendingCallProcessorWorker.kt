package com.bopr.android.smailer

import android.content.Context
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.UPDATE
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest.Companion.MIN_PERIODIC_INTERVAL_MILLIS
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MILLISECONDS

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
        private const val WORK_RESEND = "com.bopr.android.smailer.resend"

        fun Context.startPendingCallProcessing() {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
            val request = PeriodicWorkRequest.Builder(PendingCallProcessorWorker::class.java,
                            MIN_PERIODIC_INTERVAL_MILLIS, MILLISECONDS)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_RESEND, UPDATE, request)

            log.debug("Enabled")
        }
    }
}