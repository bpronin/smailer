package com.bopr.android.smailer.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service that returns an IBinder for the sync adapter class, allowing the sync adapter
 * framework to call onPerformSync()
 *
 *
 * Required by synchronization framework.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class SyncService : Service() {

    override fun onCreate() {
        synchronized(lock) {
            adapter = adapter ?: SyncAdapter(applicationContext, true)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
       return adapter?.syncAdapterBinder ?: throw IllegalStateException()
    }

    companion object {
        private val lock = Any()
        private var adapter: SyncAdapter? = null
    }
}