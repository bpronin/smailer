package com.bopr.android.smailer.ui

import android.app.backup.BackupManager
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import com.bopr.android.smailer.AppStartup.startupApplication
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.PermissionsHelper
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_PROCESS_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.control.MailRemoteControlWorker.Companion.enableMailRemoteControl
import com.bopr.android.smailer.provider.telephony.ContentObserverService.Companion.startContentObserver
import com.bopr.android.smailer.util.requireIgnoreBatteryOptimization

/**
 * Main application activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : FlavorBaseActivity(MainFragment::class) {

    private lateinit var backupManager: BackupManager
    private lateinit var permissionsHelper: PermissionsHelper
    private lateinit var notificationsHelper: NotificationsHelper
    private lateinit var settingsListener: OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(false)

        backupManager = BackupManager(this)
        notificationsHelper = NotificationsHelper(this)
        permissionsHelper = PermissionsHelper(this)
        permissionsHelper.checkAll()

        requireIgnoreBatteryOptimization()
        startupApplication()
    }

    override fun onStart() {
        super.onStart()
        settingsListener = settings.registerListener(::onSettingsChanged)
    }

    override fun onStop() {
        settings.unregisterListener(settingsListener)
        super.onStop()
    }

    private fun onSettingsChanged(settings: Settings, key: String) {
        when (key) {
            PREF_PHONE_PROCESS_TRIGGERS ->
                startContentObserver()

            PREF_REMOTE_CONTROL_ENABLED ->
                enableMailRemoteControl()
        }

        notificationsHelper.applySettings(settings, key)
        permissionsHelper.onSettingsChanged(key)
        backupManager.dataChanged()
    }

}