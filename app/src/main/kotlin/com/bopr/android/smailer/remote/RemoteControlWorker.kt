package com.bopr.android.smailer.remote

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService.enqueueWork
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Periodically checks email out for remote tasks.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class RemoteControlWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        if (isFeatureEnabled(applicationContext)) {
            startRemoteControlService(applicationContext)
        }
        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("RemoteControlWorker")
        private const val WORKER_TAG = "com.bopr.android.smailer.remote"
        private const val JOB_ID = 1002

        private fun isFeatureEnabled(context: Context): Boolean {
            return Settings(context).getBoolean(PREF_REMOTE_CONTROL_ENABLED)
        }

        fun startRemoteControlService(context: Context) {
            log.debug("Starting service")

            enqueueWork(context, RemoteControlService::class.java, JOB_ID,
                    Intent(context, RemoteControlService::class.java))
        }

        fun enableRemoteControlWorker(context: Context) {
            val manager = WorkManager.getInstance()

            manager.cancelAllWorkByTag(WORKER_TAG)

            if (isFeatureEnabled(context)) {
                val constraints = Constraints.Builder()
                        .setRequiredNetworkType(CONNECTED)
                        .build()
                val request = PeriodicWorkRequest.Builder(RemoteControlWorker::class.java,
                                15, MINUTES) /* must be greater than [PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS] */
                        .addTag(WORKER_TAG)
                        .setConstraints(constraints)
                        .build()
                manager.enqueueUniquePeriodicWork(WORKER_TAG, REPLACE, request)

                log.debug("Enabled")
            } else {
                log.debug("Disabled")
            }
        }
    }
}