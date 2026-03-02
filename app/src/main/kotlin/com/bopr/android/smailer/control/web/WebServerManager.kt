package com.bopr.android.smailer.control.web

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_SERVER_HOST
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_SERVER_PORT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder

/**
 * Web server manager.
 */
class WebServerManager private constructor(private val context: Context) :
    SettingsAware(context) {

    private fun toggleEnabled() {
        if (context.settings.getBoolean(PREF_WEB_REMOTE_CONTROL_ENABLED)) {
            start(
                context.settings.getString(PREF_WEB_SERVER_HOST)!!,
                context.settings.getString(PREF_WEB_SERVER_PORT)!!.toInt()
            )
        } else {
            stop()
        }
    }

    private fun start(host: String, port: Int) {
        // TODO: implement
        log.debug("Web server started at host $host, port $port")
    }

    private fun stop() {
        // TODO: implement
        log.debug("Web server stopped")
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        when (key) {
            PREF_WEB_REMOTE_CONTROL_ENABLED -> toggleEnabled()
            PREF_WEB_SERVER_HOST,
            PREF_WEB_SERVER_PORT -> {
                stop()
                toggleEnabled()
            }
        }
    }

    companion object {
        private val log = Logger("WebRemoteControl")
        private val singletonHolder = SingletonHolder { WebServerManager(it) }
        internal fun Context.enableWebServer() = singletonHolder.getInstance(this).toggleEnabled()
    }
}