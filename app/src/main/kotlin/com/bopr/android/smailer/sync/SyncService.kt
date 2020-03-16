package com.bopr.android.smailer.sync

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.util.getAccount
import org.slf4j.LoggerFactory

class SyncService : IntentService("google-drive-sync") {

    override fun onHandleIntent(intent: Intent?) {
        try {
            val settings = Settings(this)
            if (settings.getBoolean(PREF_SYNC_ENABLED)) {
                val account = getAccount(settings.getString(PREF_SENDER_ACCOUNT))
                account?.let {
                    Database(this).use {
                        Synchronizer(this, account, it).sync()
                    }
                } ?: log.warn("No sync account")
            }

            log.debug("Synchronized")
        } catch (x: Exception) {
            log.warn("Synchronization failed ", x)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("SyncService")

        fun startSyncService(context: Context) {
            log.debug("Starting service")

            context.startService(Intent(context, SyncService::class.java))
        }
    }
}