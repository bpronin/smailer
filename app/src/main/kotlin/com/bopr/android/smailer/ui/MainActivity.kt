package com.bopr.android.smailer.ui

import android.app.backup.BackupManager
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.AppStartup.startUpAppServices
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.PermissionsHelper
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.control.MailControlWorker.Companion.enableMailRemoteControl
import com.bopr.android.smailer.external.Firebase.Companion.resubscribeToFirebaseMessaging
import com.bopr.android.smailer.provider.telephony.ContentObserverService.Companion.startContentObserver
import com.bopr.android.smailer.sync.SyncWorker.Companion.syncAppDataWithGoogleCloud
import com.bopr.android.smailer.util.requireIgnoreBatteryOptimization

/**
 * Main application activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseFlavorActivity(MainFragment::class), OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var backupManager: BackupManager
    private lateinit var permissionsHelper: PermissionsHelper
    private lateinit var notificationsHelper: NotificationsHelper
    private lateinit var accountHelper: AccountHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(false)

        backupManager = BackupManager(this)
        notificationsHelper = NotificationsHelper(this)
        accountHelper = AccountHelper(this)
        settings = Settings(this).apply {
            loadDefaults()
            registerOnSharedPreferenceChangeListener(this@MainActivity)
        }

        permissionsHelper = PermissionsHelper(this){
            requireIgnoreBatteryOptimization()
        }
        permissionsHelper.checkAll()

        startUpAppServices()
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_EMAIL_TRIGGERS ->
                startContentObserver()

            PREF_REMOTE_CONTROL_ENABLED ->
                enableMailRemoteControl()

            PREF_EMAIL_SENDER_ACCOUNT -> {
                if (accountHelper.isGoogleAccountExists(settings.getString(PREF_EMAIL_SENDER_ACCOUNT))) {
                    syncAppDataWithGoogleCloud()
                    resubscribeToFirebaseMessaging()
                }
            }
        }

        notificationsHelper.onSettingsChanged(settings, key)
        permissionsHelper.onSettingsChanged(key)
        backupManager.dataChanged()
    }

}