package com.bopr.android.smailer.ui

import android.app.backup.BackupManager
import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.Environment.setupEnvironment
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControlWorker
import com.bopr.android.smailer.sync.SyncEngine.onSyncEngineSettingsChanged

/**
 * Main application settings activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : MainAppActivity(MainFragment::class), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var backupManager: BackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(false)

        backupManager = BackupManager(this)

        settings = Settings(this)
        settings.loadDefaults()
        settings.registerOnSharedPreferenceChangeListener(this)

        setupEnvironment(this)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_EMAIL_TRIGGERS ->
                enableContentObserver(this)
            PREF_REMOTE_CONTROL_ENABLED ->
                enableRemoteControlWorker(this)
        }

        backupManager.dataChanged()
        onSyncEngineSettingsChanged(this, key)
    }

}