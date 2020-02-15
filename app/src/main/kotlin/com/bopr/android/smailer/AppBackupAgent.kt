package com.bopr.android.smailer

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper
import org.slf4j.LoggerFactory

/**
 * Backup agent. Required on Android 2.2 (API level 8) to Android 6.0 (API level 23).
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AppBackupAgent : BackupAgentHelper() {

    private val log = LoggerFactory.getLogger("Backup")

    override fun onCreate() {
        log.debug("Create")
        addHelper("settings", SharedPreferencesBackupHelper(this, Settings.PREFERENCES_STORAGE_NAME))
        //        addHelper("database", new FileBackupHelper(this, "../databases/" + Settings.DB_NAME));
    }
}