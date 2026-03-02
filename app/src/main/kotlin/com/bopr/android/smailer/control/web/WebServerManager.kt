package com.bopr.android.smailer.control.web

import android.content.Context
import android.text.format.DateFormat
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.DEFAULT_WEB_SERVER_PORT
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_WEB_SERVER_PORT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.netty.NettyApplicationEngine.Configuration
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.html.HEAD
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr

/**
 * Web server manager.
 */
/* NOTE: to access web server on emulator use
    adb forward tcp:8888 tcp:8888 
    and then use http://localhost:8888 in browser */
class WebServerManager private constructor(private val context: Context) :
    SettingsAware(context) {

    private var server: EmbeddedServer<NettyApplicationEngine, Configuration>? = null

    private fun start(port: Int) {
        log.info("Starting server...")

        this.server = embeddedServer(Netty, port) {
            routing {
                get("/") {
                    respondIndex()
                }
                get("/history") {
                    respondHistory()
                }
            }
        }.start(false)

        log.debug("Web server started at port: $port")
    }

    private fun stop() {
        server?.let {
            it.stop()
            log.debug("Web server stopped")
        }
    }

    private fun toggleEnabled() {
        if (context.settings.getBoolean(PREF_WEB_REMOTE_CONTROL_ENABLED)) {
            start(
                context.settings.getString(PREF_WEB_SERVER_PORT, DEFAULT_WEB_SERVER_PORT).toInt()
            )
        } else {
            stop()
        }
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        when (key) {
            PREF_WEB_REMOTE_CONTROL_ENABLED -> toggleEnabled()
            PREF_WEB_SERVER_PORT -> {
                stop()
                toggleEnabled()
            }
        }
    }

    private suspend fun RoutingContext.respondIndex() {
        call.respondHtml {
            head {
                title { +"Index" }
                default()
            }
            body {
                this.apply {
                    p {
                        a("/history") { +"Get history" }
                    }
                }
            }
        }
    }

    private suspend fun RoutingContext.respondHistory() {
        val events = context.database.events
        call.respondHtml {
            head {
                title { +"History" }
                default()
            }
            body {
                table {
                    thead {
                        tr {
                            th { +"Time" }
                            th { +"Phone" }
                            th { +"Text" }
                        }
                    }
                    events.forEach { item ->
                        (item.payload as? PhoneCallData)?.let {
                            tr {
                                td {
                                    +"${
                                        DateFormat.format(
                                            context.getString(R.string._time_pattern),
                                            it.startTime
                                        )
                                    }"
                                }
                                td { +it.phone }
                                td { +(it.text ?: "") }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun HEAD.default() {
        meta { charset = "UTF-8" }
        link {
            rel = "preconnect"
            href = "https://fonts.googleapis.com"
        }
        link {
            rel = "preconnect"
            href = "https://fonts.gstatic.com"
            attributes["crossorigin"] = ""
        }
        link {
            rel = "stylesheet"
            href = "https://fonts.googleapis.com/css2?family=Roboto:wght@100..900&display=swap"
        }
        style {
            +"body { font-family: 'Roboto', sans-serif; }"
            +"table { border-collapse: collapse; width: 50%; }"
            +"th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }"
            +"th { background-color: #f2f2f2; font-weight:700; }"
        }
    }

    companion object {
        private val log = Logger("WebRemoteControl")
        private val singletonHolder = SingletonHolder { WebServerManager(it) }
        internal fun Context.enableWebServer() = singletonHolder.getInstance(this).toggleEnabled()
    }
}