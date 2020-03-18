package com.bopr.android.smailer.ui

import android.app.backup.BackupManager
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.Environment.startApplicationServices
import com.bopr.android.smailer.Notifications
import com.bopr.android.smailer.PermissionsHelper
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.firebase.CloudMessaging.resubscribeToCloudMessaging
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControl
import com.bopr.android.smailer.sync.SyncWorker.Companion.requestDataSync
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_DOWNLOAD
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.requireIgnoreBatteryOptimization
import com.bopr.android.smailer.util.isAccountExists

/**
 * Main application activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : MainAppActivity(MainFragment::class), OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var backupManager: BackupManager
    private lateinit var permissionsHelper: PermissionsHelper
    private lateinit var notifications: Notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(false)

        settings = Settings(this)
        backupManager = BackupManager(this)
        notifications = Notifications(this)
        permissionsHelper = PermissionsHelper(this)

        settings.loadDefaults()
        settings.registerOnSharedPreferenceChangeListener(this)

        startApplicationServices()

        permissionsHelper.checkAll()
        requireIgnoreBatteryOptimization(this)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_EMAIL_TRIGGERS ->
                enableContentObserver()
            PREF_REMOTE_CONTROL_ENABLED ->
                enableRemoteControl()
            PREF_SENDER_ACCOUNT -> {
                if (isAccountExists(settings.senderAccount)) {
                    requestDataSync(SYNC_FORCE_DOWNLOAD)
                    resubscribeToCloudMessaging()
                }
            }
        }

        notifications.onSettingsChanged(settings, key)
        permissionsHelper.onSettingsChanged(key)
        backupManager.dataChanged()
    }

}