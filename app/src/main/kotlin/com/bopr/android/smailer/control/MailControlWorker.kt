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
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Periodically checks service mailbox for messages containing control commands.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class MailControlWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        applicationContext.run {
            if (isFeatureEnabled) {
                MailControlProcessor(this).checkMailbox()
            }
        }

        return Result.success()
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("MailRemoteControlWorker")
        private const val WORK_REMOTE_CONTROL = "com.bopr.android.smailer.remote_control"
        private val Context.isFeatureEnabled
            get() = Settings(this).getBoolean(
                PREF_REMOTE_CONTROL_ENABLED
            )

        fun Context.enableMailRemoteControl() {
            val manager = WorkManager.getInstance(this)
            if (isFeatureEnabled) {
                log.debug("Enabled")

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()

                val request = PeriodicWorkRequest.Builder(
                    MailControlWorker::class.java,
                    MIN_PERIODIC_INTERVAL_MILLIS,
                    MILLISECONDS
                )
                    .setConstraints(constraints)
                    .build()

                manager.enqueueUniquePeriodicWork(WORK_REMOTE_CONTROL, UPDATE, request)
            } else {
                log.debug("Disabled")

                manager.cancelUniqueWork(WORK_REMOTE_CONTROL)
            }
        }
    }
}