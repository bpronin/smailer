package com.bopr.android.smailer.ui

import android.app.backup.BackupManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.ContentObserverService.Companion.enableContentObserver
import com.bopr.android.smailer.Environment.setupEnvironment
import com.bopr.android.smailer.ResendWorker.Companion.enableResendWorker
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_RESEND_UNSENT
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enableRemoteControlWorker
import com.bopr.android.smailer.sync.SyncEngine.onSyncEngineSettingsChanged

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : AppActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

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
        handleStartupParams(intent)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_RESEND_UNSENT ->
                enableResendWorker(this)
            PREF_EMAIL_TRIGGERS ->
                enableContentObserver(this)
            PREF_REMOTE_CONTROL_ENABLED ->
                enableRemoteControlWorker(this)
        }

        onSyncEngineSettingsChanged(this, key)
    }

    override fun createFragment(): Fragment {
        return MainFragment()
    }

    private fun handleStartupParams(intent: Intent) {
        val stringExtra = intent.getStringExtra("screen")
        if (stringExtra != null) {
            when (stringExtra) {
                "debug" -> try {
                    startActivity(Intent(this, Class.forName("com.bopr.android.smailer.ui.DebugActivity")))
                } catch (x: ClassNotFoundException) {
                    throw RuntimeException(x)
                }
            }
        }
    }
}