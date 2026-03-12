package com.bopr.android.smailer.provider.telephony

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_PROCESS_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.Logger

/**
 * Content observers manager. Enables or disables content observers depending on settings.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class ContentObserverManager(private val context: Context) : SettingsAware(context) {

    private var smsObserver: SmsContentObserver? = null

    fun enable() {
        if (settings.getPhoneProcessTriggers().contains(VAL_PREF_TRIGGER_OUT_SMS)) {
            registerObserver()
        } else {
            unregisterObserver()
        }
    }

    private fun registerObserver() {
        if (smsObserver != null) {
            log.warn("SMS observer already enabled")
        } else {
            smsObserver = SmsContentObserver(context).apply { register() }
        }
    }

    private fun unregisterObserver() {
        smsObserver?.apply {
            unregister()
            smsObserver = null
        }
    }

    override fun dispose() {
        unregisterObserver()
        super.dispose()
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        if (key == PREF_PHONE_PROCESS_TRIGGERS) enable()
    }

    companion object {
        private val log = Logger("ContentObserver")
    }
}