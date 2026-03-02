package com.bopr.android.smailer.ui

import android.os.Bundle
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_SERVER_HOST
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_SERVER_PORT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.updateSummary

/**
 * Web remote control settings fragment.
 */
class WebServerRemoteControlFragment : BasePreferenceFragment(R.xml.pref_remote_web_server) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreference(PREF_WEB_SERVER_HOST).setOnChangeListener {
            it.updateSummary(settings.getString(it.key))
        }

        requirePreference(PREF_WEB_SERVER_PORT).setOnChangeListener {
            it.updateSummary(settings.getString(it.key))
        }
    }
}