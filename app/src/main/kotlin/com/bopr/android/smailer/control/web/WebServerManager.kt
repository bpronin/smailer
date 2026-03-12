package com.bopr.android.smailer.control.web

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.DEFAULT_WEB_SERVER_PORT
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_SERVER_PORT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.util.Logger
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.netty.NettyApplicationEngine.Configuration
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Web server manager.
 */
/* NOTE: to access web server on emulator use
    adb forward tcp:8888 tcp:8888 
    and then use http://localhost:8888 in browser */
class WebServerManager(private val context: Context) :
    SettingsAware(context) {

    private var server: EmbeddedServer<NettyApplicationEngine, Configuration>? = null

    private fun start(port: Int) {
        if (server != null) {
            log.warn("Server already running")
            return
        }

        this.server = embeddedServer(Netty, port) {
            routing {
                get("/") {
                    call.respondHtmlTemplate(IndexPage(context)) { }
                }
                get("/history") {
                    call.respondHtmlTemplate(HistoryPage(context)) { }
                }
            }
        }.start(false)

        log.debug("Server started at port: $port")
    }

    private fun stop() {
        server?.let {
            it.stop()
            server = null
            log.debug("Server stopped")
        }
    }

    fun enable() {
        if (context.settings.getBoolean(PREF_WEB_REMOTE_CONTROL_ENABLED)) {
            start(
                context.settings.getString(PREF_WEB_SERVER_PORT, DEFAULT_WEB_SERVER_PORT).toInt()
            )
        } else {
            stop()
        }
    }

    override fun dispose() {
        stop()
        super.dispose()
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        when (key) {
            PREF_WEB_REMOTE_CONTROL_ENABLED -> enable()
            PREF_WEB_SERVER_PORT -> {
                stop()
                enable()
            }
        }
    }

    companion object {
        private val log = Logger("WebControl")
    }
}