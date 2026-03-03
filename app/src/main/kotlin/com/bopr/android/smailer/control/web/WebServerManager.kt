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
import com.bopr.android.smailer.util.phoneCallTypeText
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
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.ul
import kotlinx.html.unsafe

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
                default()
            }
            body {
                h1 {
                    +"SMailer"
                }
                ul {
                    li {
                        a("/history") { +"Call history" }
                    }
                }
            }
        }
    }

    private suspend fun RoutingContext.respondHistory() {
        val events = context.database.events
        call.respondHtml {
            head {
                default()
                style {
                    unsafe {
                        +"table { border-collapse: collapse; width: 100%; }"
                        +"th, td { border: 1px solid #ccc; padding: 8px; }"
                        +"th { background-color: #f2f2f2; font-weight:700; }"
                        +"th.col-type { width: 10%;}"
                        +"th.col-phone { width: 20%; }"
                        +"th.col-text { width: auto;}"
                        +"th.col-time { width: 20%; }"
//                        +"td.col-text { font-size: 60%;}"
                    }
                }
            }
            body {
                h1 {
                    +"Call history"
                }
                table {
                    thead {
                        tr {
                            th(classes = "col-type") { +"Type" }
                            th(classes = "col-phone") { +"Phone" }
                            th(classes = "col-text") { +"Text" }
                            th(classes = "col-time") { +"Time" }
                        }
                    }
                    events.forEach { item ->
                        (item.payload as? PhoneCallData)?.let {
                            tr {
                                td(classes = "col-type") {
                                    +context.getString(phoneCallTypeText(it))
                                }
                                td(classes = "col-phone") { +it.phone }
                                td(classes = "col-text") { +(it.text ?: "") }
                                td(classes = "col-time") {
                                    +DateFormat.format(
                                        context.getString(R.string._time_pattern),
                                        it.startTime
                                    ).toString()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun HEAD.default() {
        title { +"SMailer" }
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
            unsafe {
                +"body { font-family: 'Roboto', sans-serif; }"
                +"h1 { font-size: large; font-weight: 700; }"
            }
        }
    }

    companion object {
        private val log = Logger("WebRemoteControl")
        private val singletonHolder = SingletonHolder { WebServerManager(it) }
        internal fun Context.enableWebServer() = singletonHolder.getInstance(this).toggleEnabled()
    }
}