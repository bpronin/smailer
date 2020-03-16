package com.bopr.android.smailer.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.DB_FLAG_SYNCING
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.sync.SyncWorker.Companion.cancelPeriodicSyncWork
import com.bopr.android.smailer.sync.SyncWorker.Companion.runPeriodicSyncWork
import com.bopr.android.smailer.sync.SyncWorker.Companion.runSyncWork
import org.slf4j.LoggerFactory

internal class SyncManager(private val context: Context) : OnSharedPreferenceChangeListener {

    private val settings = Settings(context)
    private val databaseListener = DatabaseListener()
    private val enabled: Boolean
        get() = settings.getBoolean(PREF_SYNC_ENABLED)

    init {
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    fun enable() {
        if (enabled) {
            context.registerDatabaseListener(databaseListener)
            runPeriodicSyncWork(context)

            log.debug("Enabled")
        } else {
            context.unregisterDatabaseListener(databaseListener)
            cancelPeriodicSyncWork(context)

            log.debug("Disabled")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_SYNC_ENABLED ->
                enable()
            PREF_SENDER_ACCOUNT -> {
                if (enabled) {
                    runSyncWork(context, true)
                }
            }
        }
    }

    private inner class DatabaseListener : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (enabled) {
                val flags = intent.getIntExtra(Database.EXTRA_FLAGS, 0)
                if (flags and DB_FLAG_SYNCING != DB_FLAG_SYNCING) {
                    runSyncWork(context)
                }
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("SyncEngine")
        private lateinit var instance: SyncManager

        fun setupSyncEngine(context: Context) {
            instance = SyncManager(context)
            instance.enable()
        }

    }
}