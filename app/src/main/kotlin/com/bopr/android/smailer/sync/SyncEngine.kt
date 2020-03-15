package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.BroadcastReceiver
import android.content.ContentResolver.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.sync.AppContentProvider.Companion.AUTHORITY
import com.bopr.android.smailer.util.getAccount
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

class SyncEngine(private val context: Context) : OnSharedPreferenceChangeListener {

    private val log = LoggerFactory.getLogger("SyncEngine")
    private val settings = Settings(context)
    private var account: Account? = null
    private val databaseListener = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            updateMetadata()
        }
    }
    private val enabled: Boolean get() = settings.getBoolean(PREF_SYNC_ENABLED)

    init {
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    fun enable() {
        if (enabled) {
            context.registerDatabaseListener(databaseListener)
            start()

            log.debug("Enabled")
        } else {
            stop()
            context.unregisterDatabaseListener(databaseListener)

            log.debug("Disabled")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_SYNC_ENABLED ->
                enable()
            PREF_SENDER_ACCOUNT ->
                restart()
        }
    }

    private fun start() {
        account = context.getAccount(settings.getString(PREF_SENDER_ACCOUNT))

        account?.let {
            requestSync(it, AUTHORITY, Bundle().apply {
                putBoolean(SYNC_EXTRAS_MANUAL, true)
                putBoolean(SYNC_EXTRAS_EXPEDITED, true)
            })

            log.debug("Sync now requested")

            addPeriodicSync(it, AUTHORITY, Bundle.EMPTY, 0)

            log.debug("Task added")
        } ?: log.debug("No account")
    }

    private fun stop() {
        account?.let {
            removePeriodicSync(it, AUTHORITY, Bundle.EMPTY)

            log.debug("Task removed")
        }
    }

    private fun restart() {
        if (enabled) {
            stop()
            start()
        }
    }

    private fun updateMetadata() {
        if (enabled) {
            Database(context).use {
                val time = currentTimeMillis()
                log.debug("Updating metadata: %tF %tT".format(time, time))
                it.lastSyncTime = time
            }
        }
    }

    companion object {

        private var instance: SyncEngine? = null

        fun setupSyncEngine(context: Context) {
            if (instance != null) {
                throw IllegalStateException("Sync engine can be instantiated only once")
            }
            instance = SyncEngine(context).apply {
                enable()
            }
        }

    }
}