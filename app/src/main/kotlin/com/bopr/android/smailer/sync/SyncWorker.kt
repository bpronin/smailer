package com.bopr.android.smailer.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import androidx.work.NetworkType.CONNECTED
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.DB_FLAG_SYNCING
import com.bopr.android.smailer.Database.Companion.EXTRA_FLAGS
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.util.getAccount
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SyncWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        val settings = Settings(applicationContext)
        if (settings.getBoolean(PREF_SYNC_ENABLED)) {
            applicationContext.getAccount(settings.getString(PREF_SENDER_ACCOUNT))?.run {
                Database(applicationContext).use {
                    Synchronizer(applicationContext, this, it).sync()
                }

                log.debug("Synchronized")
            } ?: log.warn("No sync account")
        }
        return Result.success()
    }

    private class DatabaseListener : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val flags = intent.getIntExtra(EXTRA_FLAGS, 0)
            if (flags and DB_FLAG_SYNCING != DB_FLAG_SYNCING) {
                requestSync(context)
            }
        }
    }

    internal companion object {

        private val log = LoggerFactory.getLogger("SyncWorker")
        private const val WORK_SYNC = "com.bopr.android.smailer.sync"
        private const val WORK_PERIODIC_SYNC = "com.bopr.android.smailer.periodic_sync"
        private val databaseListener = DatabaseListener()

        private fun isFeatureEnabled(context: Context) =
                Settings(context).getBoolean(PREF_SYNC_ENABLED)

        private fun constraints(): Constraints {
            return Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
        }

        fun requestSync(context: Context) {
            log.debug("Sync requested")

            if (isFeatureEnabled(context)) {
                val request = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                        .setConstraints(constraints())
                        .build()
                WorkManager.getInstance(context).enqueueUniqueWork(WORK_SYNC,
                        ExistingWorkPolicy.KEEP, request)
            }
        }

        fun enableSyncWorker(context: Context) {
            if (isFeatureEnabled(context)) {
                val request = PeriodicWorkRequest.Builder(SyncWorker::class.java, 15, MINUTES)
                        .setConstraints(constraints())
                        .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_PERIODIC_SYNC,
                        ExistingPeriodicWorkPolicy.REPLACE, request)

                context.registerDatabaseListener(databaseListener)

                log.debug("Enabled")
            } else {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_PERIODIC_SYNC)

                context.unregisterDatabaseListener(databaseListener)

                log.debug("Disabled")
            }
        }
    }
}