package com.bopr.android.smailer.provider.telephony

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_PROCESS_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder

/**
 * Listens to changes in sms content. Used to process outgoing SMS.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class ContentObserverManager private constructor(private val context: Context) :
    SettingsAware(context) {

    private fun startService() {
        val intent = Intent(context, ContentObserver::class.java)
        val triggers = settings.getPhoneProcessTriggers()

        if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            log.debug("Service enabled")
        } else {
            context.stopService(intent)

            log.debug("Service disabled")
        }
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        if (key == PREF_PHONE_PROCESS_TRIGGERS) startService()
    }

    companion object {

        private val log = Logger("ContentObserver")

        private val singletonHolder = SingletonHolder { ContentObserverManager(it) }
        internal fun Context.startContentObserver() =
            singletonHolder.getInstance(this).startService()
    }
}