package com.bopr.android.smailer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import com.bopr.android.smailer.CallProcessorService.Companion.startCallProcessingService
import com.bopr.android.smailer.util.AndroidUtil
import com.bopr.android.smailer.util.db.RowSet
import org.slf4j.LoggerFactory

/**
 * Listens to changes in sms content. Used to process outgoing SMS.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class ContentObserverService : Service() {

    private lateinit var contentObserver: ContentObserver
    private lateinit var notifications: Notifications
    private lateinit var thread: HandlerThread

    override fun onCreate() {
        notifications = Notifications(this)

        thread = HandlerThread("ContentObserverService")
        thread.start()
        contentObserver = SmsContentObserver(Handler(thread.looper))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log.debug("Running")

        contentResolver.registerContentObserver(CONTENT_SMS, true, contentObserver)
        startForeground(1, notifications.foregroundServiceNotification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        thread.quit()
        contentResolver.unregisterContentObserver(contentObserver)
        super.onDestroy()

        log.debug("Destroyed")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processOutgoingSms(id: String) {
        log.debug("Processing outgoing sms: $id")

        val query = contentResolver.query(CONTENT_SMS_SENT, null, "_id=?", arrayOf(id), null)
        val event = SentSmsRowSet(query).findFirst()
        if (event != null) {
            startCallProcessingService(this, event)
        }
    }

    private inner class SentSmsRowSet(query: Cursor?) : RowSet<PhoneEvent?>(query) {

        override fun get(): PhoneEvent {
            val date = getLong("date")
            val event = PhoneEvent()
            event.isIncoming = false
            event.recipient = AndroidUtil.deviceName()
            event.phone = getString("address")
            event.startTime = date
            event.endTime = date
            event.text = getString("body")
            return event
        }
    }

    private inner class SmsContentObserver(handler: Handler) : ContentObserver(handler) {

        private var lastProcessed: Uri? = null

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) { /* this method may be called multiple times so we need to remember processed uri */
            log.debug("Processing uri: $uri")

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
         * Starts or stops the service depending on settings
         *
         * @param context context
         */
        fun enable(context: Context) {
            val intent = Intent(context, ContentObserverService::class.java)
            val triggers = Settings.settings(context).filter.triggers
            if (triggers.contains(Settings.VAL_PREF_TRIGGER_OUT_SMS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }

                log.debug("Enabled")
            } else {
                context.stopService(intent)

                log.debug("Disabled")
            }
        }
    }
}