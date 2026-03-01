package com.bopr.android.smailer.ui

import android.os.Bundle
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.control.web.WebServerManager
import com.bopr.android.smailer.control.web.WebServerManager.Companion.startWebServer
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.setOnChangeListener

/**
 * Web server settings fragment.
 */
class WebServerSettingsFragment : BasePreferenceFragment(R.xml.pref_web_server_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requirePreference("remote_control_web_server_enabled").setOnChangeListener {
//            if (settings.getBoolean("remote_control_web_server_enabled")) {
//                requireContext().startWebServer()
//            } else {
//                requireContext().stopWebServer()
//            }
//        }
    }
}