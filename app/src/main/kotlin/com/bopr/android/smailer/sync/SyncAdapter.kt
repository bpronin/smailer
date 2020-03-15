package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import com.bopr.android.smailer.Database
import org.slf4j.LoggerFactory

/**
 * Handle the transfer of data between a server and an app, using the Android sync adapter framework.
 *
 *
 * Required by synchronization framework.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
/* To debug it (to put breakpoints) remove android:process=":sync" from AndroidManifest */
class SyncAdapter(context: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(context, autoInitialize) {

    private val log = LoggerFactory.getLogger("SyncAdapter")

    override fun onPerformSync(account: Account, extras: Bundle?, authority: String?,
                               provider: ContentProviderClient?, syncResult: SyncResult?) {
        val upload = extras?.getBoolean("expect_upload", false)
        try {
            Database(context).use {
                Synchronizer(context, account, it).sync()
            }
            log.debug("Synchronized")
        } catch (x: Exception) {
            log.warn("Synchronization failed ", x)
        }
    }
}