package com.bopr.android.smailer

import android.content.Context
import androidx.work.*
import com.bopr.android.smailer.Settings.Companion.PREF_RESEND_UNSENT
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class ResendWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        log.debug("Working")

        if (isFeatureEnabled(applicationContext)) {
            PendingCallProcessorService.start(applicationContext)
        }
        return Result.success()
    }

    companion object {

        private val log = LoggerFactory.getLogger("ResendWorker")
        private const val WORKER_TAG = "smailer-resend"

        private fun isFeatureEnabled(context: Context): Boolean {
            return Settings(context).getBoolean(PREF_RESEND_UNSENT, true)
        }

        fun enable(context: Context) {
            val manager = WorkManager.getInstance()
            manager.cancelAllWorkByTag(WORKER_TAG)
            if (isFeatureEnabled(context)) {
                val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                val request = PeriodicWorkRequest.Builder(ResendWorker::class.java,
                        15, TimeUnit.MINUTES) /* interval must be lesser than PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS */
                        .addTag(WORKER_TAG)
                        .setConstraints(constraints)
                        .build()
                manager.enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request)

                log.debug("Enabled")
            } else {
                log.debug("Disabled")
            }
        }
    }
}