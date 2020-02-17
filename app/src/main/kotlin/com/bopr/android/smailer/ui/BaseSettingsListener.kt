package com.bopr.android.smailer.ui

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

open class BaseSettingsListener internal constructor(context: Context) : OnSharedPreferenceChangeListener {

    private val backupManager: BackupManager = BackupManager(context)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        backupManager.dataChanged()
    }

}