package com.bopr.android.smailer.control.mail

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.UPDATE
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequest.Companion.MIN_PERIODIC_INTERVAL_MILLIS
import androidx.work.WorkManager
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Periodically checks service mailbox for messages containing control commands.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
internal class MailControlManager private constructor(context: Context) :
    SettingsAware(context) {

    private val workManager = WorkManager.getInstance(context)

    fun startWork() {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_ENABLED)) {
            workManager.enqueueUniquePeriodicWork(
                WORK_REMOTE_CONTROL,
                UPDATE,
                PeriodicWorkRequest.Builder(
                    MailControlWorker::class.java,
                    MIN_PERIODIC_INTERVAL_MILLIS,
                    MILLISECONDS
                ).setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(CONNECTED)
                        .build()
                ).build()
            )

            log.debug("Running")
        } else {
            cancelWork()
        }
    }

    fun cancelWork() {
        workManager.cancelUniqueWork(WORK_REMOTE_CONTROL)

        log.debug("Canceled")
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        if (key == PREF_REMOTE_CONTROL_ENABLED) startWork()
    }

    override fun dispose() {
        cancelWork()
        super.dispose()

        log.debug("Disposed")
    }

    internal companion object {

        private val log = Logger("MailControl")

        private const val WORK_REMOTE_CONTROL = "com.bopr.android.smailer.remote_control"

        private val singletonHolder = SingletonHolder { MailControlManager(it) }
        internal fun Context.startMailControl() =
            singletonHolder.getInstance(this).startWork()
    }
}