package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.BroadcastReceiver
import android.content.ContentResolver.*
import android.content.Context
import android.os.Bundle
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_TIME
import com.bopr.android.smailer.sync.AppContentProvider.Companion.AUTHORITY
import com.bopr.android.smailer.util.selectedAccount
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

object SyncEngine {

    private val log = LoggerFactory.getLogger("SyncEngine")
    private var databaseListener: BroadcastReceiver? = null
    private var account: Account? = null

    fun enableSyncEngine(context: Context) {
        /* register it only once */
        if (databaseListener == null) {
            databaseListener = registerDatabaseListener(context) {
                updateMetadata(context)
            }
        }

        if (isEnabled(context)) {
            start(context)

            log.debug("Enabled")
        } else {
            stop()

            log.debug("Disabled")
        }
    }

    fun onSyncEngineSettingsChanged(context: Context, setting: String) {
        when (setting) {
            PREF_SYNC_ENABLED ->
                enableSyncEngine(context)
            PREF_SENDER_ACCOUNT ->
                restart(context)
            PREF_FILTER_PHONE_BLACKLIST,
            PREF_FILTER_PHONE_WHITELIST,
            PREF_FILTER_TEXT_BLACKLIST,
            PREF_FILTER_TEXT_WHITELIST ->
                updateMetadata(context)
        }
    }

    private fun start(context: Context) {
        account = selectedAccount(context)

        account?.let {
            syncNow(it)
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

    private fun syncNow(account: Account) {
        val bundle = Bundle()
        bundle.putBoolean(SYNC_EXTRAS_MANUAL, true)
        bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true)

        requestSync(account, AUTHORITY, bundle)

        log.debug("Sync now")
    }

    private fun restart(context: Context) {
        if (isEnabled(context)) {
            stop()
            start(context)
        }
    }

    private fun isEnabled(context: Context) =
            Settings(context).getBoolean(PREF_SYNC_ENABLED)

    private fun updateMetadata(context: Context) {
        val time = currentTimeMillis()
        Settings(context).update { putLong(PREF_SYNC_TIME, time) }

        log.debug("Metadata updated: %tF %tT".format(time, time))
    }

}