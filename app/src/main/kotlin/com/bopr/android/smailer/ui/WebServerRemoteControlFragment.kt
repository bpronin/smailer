package com.bopr.android.smailer.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_SERVER_PORT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.localIpAddress
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.updateSummary

/**
 * Web remote control settings fragment.
 */
class WebServerRemoteControlFragment : BasePreferenceFragment(R.xml.pref_remote_web_server) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreference(PREF_WEB_SERVER_PORT).setOnChangeListener {
            val port = settings.getString(it.key)
            it.updateSummary(port)

            requirePreference(PREF_TEST_WEB_SERVER).updateSummary(
                getString(
                    R.string.open_browser_at_localhost,
                    formatLink()
                )
            )
        }

        requirePreference(PREF_TEST_WEB_SERVER).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, formatLink().toUri())
            startActivity(intent)
        }
    }

    fun formatLink(): String {
        val host = localIpAddress()
        val port = settings.getString(PREF_WEB_SERVER_PORT)
        return "http://$host:$port"
    }

    companion object {

        private const val PREF_TEST_WEB_SERVER = "test_web_server"
    }
}