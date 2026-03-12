package com.bopr.android.smailer.backup

import android.app.backup.BackupManager
import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.Singleton

/**
 * Listens to application data changes and notifies Android backup system.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class AppBackupManager private constructor(context: Context) : SettingsAware(context) {

    private val backupManager = BackupManager(context)

    override fun onSettingsChanged(settings: Settings, key: String) {
        backupManager.dataChanged()
    }

    companion object {

        private val singleton = Singleton { AppBackupManager(it) }
        internal fun Context.startAndroidBackup() = singleton.getInstance(this)
    }
}