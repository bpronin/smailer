package com.bopr.android.smailer;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backup agent. Required on Android 2.2 (API level 8) to Android 6.0 (API level 23).
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AppBackupAgent extends BackupAgentHelper {

    private static Logger log = LoggerFactory.getLogger("Backup");

    @Override
    public void onCreate() {
        log.debug("Create");
        addHelper("preferences", new SharedPreferencesBackupHelper(this, Settings.PREFERENCES_STORAGE_NAME));
//        addHelper("keystore", new FileBackupHelper(this, "../databases/" + Cryptor.KEYSTORE_FILE));
//        addHelper("database", new FileBackupHelper(this, "../databases/" + Settings.DB_NAME));
    }

}