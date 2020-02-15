package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.BroadcastReceiver
import android.content.ContentResolver.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.*
import com.bopr.android.smailer.sync.AppContentProvider.Companion.AUTHORITY
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import org.slf4j.LoggerFactory

class SyncEngine private constructor(context: Context) {

    private val settings: Settings = Settings(context)
    private val databaseListener = DatabaseListener()
    private val settingsListener = SettingsListener()
    private var account: Account? = null

    init {
        account = syncAccount(context)
        settings.registerOnSharedPreferenceChangeListener(settingsListener)
        registerDatabaseListener(context, databaseListener)
    }

/*
    fun dispose(){
        stop()
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
        database.unregisterListener(databaseListener)
    }
*/

    private fun start() {
        if (account != null) {
            val bundle = Bundle()
            bundle.putBoolean(SYNC_EXTRAS_MANUAL, true)
            bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true)

            requestSync(account, AUTHORITY, bundle)
            addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 0)

            log.debug("Running")
        } else {
            log.debug("No selected account")
        }
    }

    private fun stop() {
        if (account != null) {
            removePeriodicSync(account, AUTHORITY, Bundle.EMPTY)

            log.debug("Stopped")
        }
    }

    private fun updateMetaData() {
        settings.edit().putLong(PREF_SYNC_TIME, System.currentTimeMillis()).apply()

        log.debug("Metadata updated")
    }

    private inner class DatabaseListener : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            updateMetaData()
        }
    }

    private inner class SettingsListener : OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            when (key) {
                PREF_FILTER_PHONE_BLACKLIST,
                PREF_FILTER_PHONE_WHITELIST,
                PREF_FILTER_TEXT_BLACKLIST,
                PREF_FILTER_TEXT_WHITELIST ->
                    updateMetaData()
                PREF_SENDER_ACCOUNT -> {
                    stop()
                    start()
                }
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("SyncManager")

        private fun syncAccount(context: Context): Account? {
            val name = settings(context).getString(PREF_SENDER_ACCOUNT, null)
            return GoogleAccountManager(context).getAccountByName(name)
        }

        fun enable(context: Context) {
            SyncEngine(context).start()
        }

        fun syncNow(context: Context) {
            val bundle = Bundle()
            bundle.putBoolean(SYNC_EXTRAS_MANUAL, true)
            bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true)
            requestSync(syncAccount(context), AUTHORITY, bundle)
        }

    }
}