package com.bopr.android.smailer.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy.KEEP
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.sync.SyncWorker.Companion.SYNC_OPTIONS
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_NORMAL
import com.bopr.android.smailer.util.Disposable
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder

class SyncManager private constructor(private val context: Context) : Disposable {

    private val databaseListener = context.database.registerListener { startWork() }
    private val settingsListener = context.settings.registerListener(::onSettingsChanged)

    private fun startWork() {
        if (context.settings.getBoolean(PREF_SYNC_ENABLED)) {
            log.debug("Sync requested")

            WorkManager.getInstance(context).enqueueUniqueWork(
                "com.bopr.android.smailer.sync",
                KEEP,
                OneTimeWorkRequest.Builder(SyncWorker::class.java)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(CONNECTED)
                            .build()
                    )
                    .setInputData(
                        Data.Builder()
                            .putInt(SYNC_OPTIONS, SYNC_NORMAL)
                            .build()
                    )
                    .build()
            )
        }
    }

    override fun dispose() {
        context.settings.unregisterListener(settingsListener)
        context.database.unregisterListener(databaseListener)
    }

    private fun onSettingsChanged(settings: Settings, key: String) {
        if (key == PREF_MAIL_SENDER_ACCOUNT) {
            val accountName = settings.getString(PREF_MAIL_SENDER_ACCOUNT)
            if (context.accounts.isGoogleAccountExists(accountName)) startWork()
        }
    }

    companion object {

        private val log = Logger("Synchronizer")

        private val singletonHolder = SingletonHolder { SyncManager(it) }
        internal fun Context.startGoogleCloudSync() =
            singletonHolder.getInstance(this).startWork()
    }
}