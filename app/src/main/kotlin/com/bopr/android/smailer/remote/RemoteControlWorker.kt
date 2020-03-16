package com.bopr.android.smailer.remote

import android.content.Context
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Periodically checks email out for remote tasks.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class RemoteControlWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        if (isFeatureEnabled(applicationContext)) {
            RemoteControlProcessor(applicationContext).checkMailbox()
        }
        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("RemoteControlWorker")
        private const val WORK_REMOTE = "com.bopr.android.smailer.remote"

        private fun isFeatureEnabled(context: Context): Boolean {
            return Settings(context).getBoolean(PREF_REMOTE_CONTROL_ENABLED)
        }

        fun enableRemoteControlWorker(context: Context) {
            val manager = WorkManager.getInstance(context)
            if (isFeatureEnabled(context)) {
                log.debug("Enabled")

                val constraints = Constraints.Builder()
                        .setRequiredNetworkType(CONNECTED)
                        .build()
                val request = PeriodicWorkRequest.Builder(RemoteControlWorker::class.java,
                                MIN_PERIODIC_INTERVAL_MILLIS, MILLISECONDS)
                        .setConstraints(constraints)
                        .build()
                manager.enqueueUniquePeriodicWork(WORK_REMOTE, REPLACE, request)
            } else {
                log.debug("Disabled")

                manager.cancelUniqueWork(WORK_REMOTE)
            }
        }
    }
}