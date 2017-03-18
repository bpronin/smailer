package com.bopr.android.smailer;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Class AppBackupAgent.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AppBackupAgent extends BackupAgentHelper {

    @Override
    public void onCreate() {
        addHelper("preferences", new SharedPreferencesBackupHelper(this, Settings.PREFERENCES_STORAGE_NAME));
        addHelper("database", new FileBackupHelper(this, "../databases/" + Settings.DB_NAME));
    }

}