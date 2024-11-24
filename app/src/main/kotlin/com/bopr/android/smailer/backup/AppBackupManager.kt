package com.bopr.android.smailer.backup

import android.app.backup.BackupManager
import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.SingletonHolder

/**
 * Listens to application data changes and notifies Android backup system.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AppBackupManager private constructor(context: Context) : SettingsAware(context) {

    private val backupManager = BackupManager(context)

    override fun onSettingsChanged(settings: Settings, key: String) {
        backupManager.dataChanged()
    }

    companion object {

        private val singletonHolder = SingletonHolder { AppBackupManager(it) }
        internal fun Context.startAndroidBackup() = singletonHolder.getInstance(this)
    }
}