package com.bopr.android.smailer.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.external.Firebase.Companion.firebase
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_NORMAL
import com.bopr.android.smailer.util.Logger

/**
 * Worker used in synchronization application data with google drive.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
internal class SyncWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        applicationContext.run {
            if (settings.getBoolean(PREF_SYNC_ENABLED)) {
                accounts.getGoogleAccount(
                    settings.getString(PREF_MAIL_SENDER_ACCOUNT)
                )?.let { account ->
                    Synchronizer(applicationContext, account).run {
                        val mode = inputData.getInt(SYNC_OPTIONS, SYNC_NORMAL)
                        if (sync(mode)) {
                            firebase.send(FCM_REQUEST_DATA_SYNC)
                        }
                    }
                } ?: log.warn("No sync account")
            }
        }
        return Result.success()
    }

    companion object {

        private val log = Logger("SyncWorker")
        const val SYNC_OPTIONS = "options"
    }
}