package com.bopr.android.smailer.control

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.UPDATE
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequest.Companion.MIN_PERIODIC_INTERVAL_MILLIS
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.Logger
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Periodically checks service mailbox for messages containing control commands.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class MailRemoteControlWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        applicationContext.run {
            if (isFeatureEnabled) {
                MailControlProcessor(this).checkMailbox {}
            }
        }

        return Result.success()
    }

    internal companion object {

        private val log = Logger("MailRemoteControl")

        private const val WORK_REMOTE_CONTROL = "com.bopr.android.smailer.remote_control"
        private val Context.isFeatureEnabled
            get() = settings.getBoolean(
                PREF_REMOTE_CONTROL_ENABLED
            )

        fun Context.enableMailRemoteControl() {
            val manager = WorkManager.getInstance(this)
            if (isFeatureEnabled) {
                log.debug("Service enabled")

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()

                val request = PeriodicWorkRequest.Builder(
                    MailRemoteControlWorker::class.java,
                    MIN_PERIODIC_INTERVAL_MILLIS,
                    MILLISECONDS
                )
                    .setConstraints(constraints)
                    .build()

                manager.enqueueUniquePeriodicWork(WORK_REMOTE_CONTROL, UPDATE, request)
            } else {
                log.debug("Service disabled")

                manager.cancelUniqueWork(WORK_REMOTE_CONTROL)
            }
        }
    }
}