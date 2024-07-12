package com.bopr.android.smailer

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.bopr.android.smailer.CallProcessorService.Companion.startCallProcessingService
import com.bopr.android.smailer.Notifications.Companion.SERVICE_NOTIFICATION_ID
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.util.database.getLong
import com.bopr.android.smailer.util.database.getString
import com.bopr.android.smailer.util.database.getStringOrNull
import com.bopr.android.smailer.util.database.useFirst
import com.bopr.android.smailer.util.deviceName
import org.slf4j.LoggerFactory

/**
 * Listens to changes in sms content. Used to process outgoing SMS.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class ContentObserverService : Service() {

    private lateinit var contentObserver: ContentObserver
    private lateinit var notifications: Notifications

    override fun onCreate() {
        notifications = Notifications(this)
        contentObserver = SmsContentObserver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log.debug("Running")

        contentResolver.registerContentObserver(CONTENT_SMS, true, contentObserver)
        startForeground(SERVICE_NOTIFICATION_ID, notifications.serviceNotification())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(contentObserver)
        super.onDestroy()

        log.debug("Destroyed")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("Recycle")
    private fun processOutgoingSms(id: String) {
        log.debug("Processing outgoing sms: $id")

        val context = this
        contentResolver.query(CONTENT_SMS_SENT, null, "_id=?", arrayOf(id), null)?.useFirst {
            val date = getLong("date")
            startCallProcessingService(context, PhoneEvent(
                    phone = getString("address"),
                    isIncoming = false,
                    startTime = date,
                    endTime = date,
                    text = getStringOrNull("body"),
                    acceptor = deviceName()
            ))
        }
    }

    private inner class SmsContentObserver : ContentObserver(Handler(Looper.getMainLooper())) {

        private var lastProcessed: Uri? = null

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) { /* this method may be called multiple times so we need to remember processed uri */
            log.debug("Processing uri: {}", uri)

            uri?.let {
                if (uri != lastProcessed) {
                    val segments = uri.pathSegments
                    if (segments.isNotEmpty()) {
                        when (segments[0]) {
                            "raw" ->
                                log.debug("sms/raw changed")
                            "inbox" ->
                                log.debug("sms/inbox segment changed")
                            "sent" ->
                                processOutgoingSms(segments[1])
                            else ->
                                processOutgoingSms(segments[0])
                        }
                    }
                    lastProcessed = uri
                }
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("ContentObserverService")
        private val CONTENT_SMS_SENT = Uri.parse("content://sms/sent")
        private val CONTENT_SMS = Uri.parse("content://sms")

        /**
         * Starts or stops the service depending on corresponding settings.
         */
        fun Context.enableContentObserver() {
            val intent = Intent(this, ContentObserverService::class.java)
            val triggers = Settings(this).emailTriggers
            if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }

                log.debug("Enabled")
            } else {
                stopService(intent)

                log.debug("Disabled")
            }
        }
    }
}