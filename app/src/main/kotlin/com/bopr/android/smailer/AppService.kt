package com.bopr.android.smailer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_SERVICE
import com.bopr.android.smailer.NotificationsHelper.Companion.notifications
import com.bopr.android.smailer.control.web.WebServerManager
import com.bopr.android.smailer.provider.telephony.ContentObserverManager
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.startForegroundCompat

/**
 * Main application service.
 * 
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class AppService : Service() {

    private lateinit var webServerManager: WebServerManager
    private lateinit var contentObserverManager: ContentObserverManager

    override fun onCreate() {
        log.debug("App service enabled")
        super.onCreate()
        webServerManager = WebServerManager(this).apply { enable() }
        contentObserverManager = ContentObserverManager(this).apply { enable() }
    }

    override fun onDestroy() {
        webServerManager.dispose()
        contentObserverManager.dispose()
        super.onDestroy()
        log.debug("App service disabled")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            startForegroundCompat(NTF_SERVICE, notifications.serviceNotification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private val log = Logger("AppService")
    }
}
