package com.bopr.android.smailer.ui;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;

public class BaseSettingsListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private BackupManager backupManager;

    BaseSettingsListener(Context context) {
        backupManager = new BackupManager(context);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        backupManager.dataChanged();
    }
}
