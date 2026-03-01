package com.bopr.android.smailer.control.web

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder

/**
 * Web server manager.
 */
class WebServerManager private constructor(private val context: Context) :
    SettingsAware(context) {

    fun toggleEnabled() {
        if (context.settings.getBoolean("remote_control_web_server_enabled")) {
            val host = context.settings.getString("remote_control_web_server_host", "0.0.0.0")
            val port = context.settings.getInt("remote_control_web_server_port")
            start(host, port)
        } else {
            stop()
        }
    }

    private fun start(host: String, port: Int) {
        log.debug("Web server started at port $port")
    }

    fun stop() {
        log.debug("Web server stopped")
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        if (key == "remote_control_web_server_enabled") toggleEnabled()
    }

    companion object {
        private val log = Logger("WebControl")
        private val singletonHolder = SingletonHolder { WebServerManager(it) }
        internal fun Context.startWebServer() = singletonHolder.getInstance(this).toggleEnabled()
    }
}