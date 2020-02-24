package com.bopr.android.smailer.sync

import android.content.ContentResolver.*
import android.content.Context
import android.os.Bundle
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_TIME
import com.bopr.android.smailer.sync.AppContentProvider.Companion.AUTHORITY
import com.bopr.android.smailer.ui.GoogleAuthorizationHelper.Companion.primaryAccount
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

object SyncEngine {

    private val log = LoggerFactory.getLogger("SyncEngine")

    fun startSyncEngine(context: Context) {
        registerDatabaseListener(context) {
            updateMetadata(context)
        }

        addPeriodicSync(primaryAccount(context), AUTHORITY, Bundle.EMPTY, 0)

        log.debug("Running")
    }

    /**
     * For debug purposes
     */
    fun syncNow(context: Context) {
        val bundle = Bundle()
        bundle.putBoolean(SYNC_EXTRAS_MANUAL, true)
        bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true)

        requestSync(primaryAccount(context), AUTHORITY, bundle)

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
        }
    }

    private fun updateMetadata(context: Context) {
        Settings(context).edit().putLong(PREF_SYNC_TIME, currentTimeMillis()).apply()

        log.debug("Metadata updated")
    }

}