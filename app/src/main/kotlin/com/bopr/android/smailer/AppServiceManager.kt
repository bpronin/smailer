package com.bopr.android.smailer

import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_PROCESS_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.util.Singleton
import com.bopr.android.smailer.util.startForegroundServiceCompat

class AppServiceManager private constructor(private val context: Context) :
    SettingsAware(context) {

    private fun enableService() {
        val intent = Intent(context, AppService::class.java)

        if (settings.getBoolean(PREF_WEB_REMOTE_CONTROL_ENABLED)
            || settings.getPhoneProcessTriggers().contains(VAL_PREF_TRIGGER_OUT_SMS)
        ) {
            context.startForegroundServiceCompat(intent)
        } else {
            context.stopService(intent)
        }
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        when (key) {
            PREF_WEB_REMOTE_CONTROL_ENABLED,
            PREF_PHONE_PROCESS_TRIGGERS -> enableService()
        }
    }

    companion object {
        private val singleton = Singleton { AppServiceManager(it) }
        internal fun Context.enableAppService() = singleton.getInstance(this).enableService()
    }
}