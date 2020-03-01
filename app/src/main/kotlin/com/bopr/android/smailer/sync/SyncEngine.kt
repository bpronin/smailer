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
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_TIME
import com.bopr.android.smailer.sync.AppContentProvider.Companion.AUTHORITY
import com.bopr.android.smailer.util.selectedAccount
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

object SyncEngine {

    private val log = LoggerFactory.getLogger("SyncEngine")
    private var databaseListener: BroadcastReceiver? = null
    private var account: Account? = null

    fun startSyncEngine(context: Context) {
        /* register it only once */
        if (databaseListener == null) {
            databaseListener = registerDatabaseListener(context) {
                updateMetadata(context)
            }
        }

        account?.let {
            removePeriodicSync(it, AUTHORITY, Bundle.EMPTY)

            log.debug("Stopped")
        }

        account = selectedAccount(context)

        account?.let {
            requestSyncNow(it)
            addPeriodicSync(it, AUTHORITY, Bundle.EMPTY, 0)

            log.debug("Running")
        } ?: log.debug("No account")
    }

    private fun requestSyncNow(account: Account) {
        val bundle = Bundle()
        bundle.putBoolean(SYNC_EXTRAS_MANUAL, true)
        bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true)

        requestSync(account, AUTHORITY, bundle)

        log.debug("Sync now")
    }

    fun onSyncEngineSettingsChanged(context: Context, setting: String) {
        when (setting) {
            PREF_FILTER_PHONE_BLACKLIST,
            PREF_FILTER_PHONE_WHITELIST,
            PREF_FILTER_TEXT_BLACKLIST,
            PREF_FILTER_TEXT_WHITELIST -> {
                updateMetadata(context)
            }
            PREF_SENDER_ACCOUNT ->
                startSyncEngine(context)
        }
    }

    private fun updateMetadata(context: Context) {
        val time = currentTimeMillis()
        Settings(context).edit().putLong(PREF_SYNC_TIME, time).apply()

        log.debug("Metadata updated: %tF %tT".format(time, time))
    }

}