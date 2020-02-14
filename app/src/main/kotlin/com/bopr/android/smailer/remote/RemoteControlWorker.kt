package com.bopr.android.smailer.remote

import android.content.Context
import androidx.work.*
import com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.settings
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Periodically checks email out for remote tasks.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class RemoteControlWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        log.debug("Working")

        if (isFeatureEnabled(applicationContext)) {
            RemoteControlService.start(applicationContext)
        }
        return Result.success()
    }

    companion object {

        private val log = LoggerFactory.getLogger("RemoteControlWorker")
        private const val WORKER_TAG = "smailer-email"

        private fun isFeatureEnabled(context: Context): Boolean {
            return settings(context).getBoolean(PREF_REMOTE_CONTROL_ENABLED, false)
        }

        fun enable(context: Context) {
            val manager = WorkManager.getInstance()
            manager.cancelAllWorkByTag(WORKER_TAG)
            if (isFeatureEnabled(context)) {
                val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                val request = PeriodicWorkRequest.Builder(RemoteControlWorker::class.java,
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