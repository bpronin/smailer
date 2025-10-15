package com.bopr.android.smailer.provider.telephony

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_SERVICE
import com.bopr.android.smailer.NotificationsHelper.Companion.notifications
import com.bopr.android.smailer.data.getLong
import com.bopr.android.smailer.data.getString
import com.bopr.android.smailer.data.getStringOrNull
import com.bopr.android.smailer.data.withFirst
import com.bopr.android.smailer.provider.telephony.PhoneCallEventProcessor.Companion.processPhoneCall
import com.bopr.android.smailer.util.Logger

/**
 * Listens to changes in sms content. Used to process outgoing SMS.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class ContentObserverService : Service() {

    private lateinit var contentObserver: ContentObserver

    override fun onCreate() {
        contentObserver = SmsContentObserver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log.debug("Running")

        contentResolver.registerContentObserver(CONTENT_SMS, true, contentObserver)
        startForeground(NTF_SERVICE, notifications.createServiceNotification())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(contentObserver)
        super.onDestroy()

        log.verb("Destroyed")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("Recycle")
    private fun processOutgoingSms(id: String) {
        log.debug("Processing outgoing sms: $id")

        contentResolver.query(CONTENT_SMS_SENT, null, "_id=?", arrayOf(id), null)?.withFirst {
            processPhoneCall(
                PhoneCallData(
                    startTime = getLong("date"),
                    phone = getString("address"),
                    endTime = getLong("date"),
                    text = getStringOrNull("body")
                )
            )
        }
    }

    private inner class SmsContentObserver : ContentObserver(Handler(Looper.getMainLooper())) {

        private var lastProcessed: Uri? = null

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(
            selfChange: Boolean,
            uri: Uri?
        ) { /* this method may be called multiple times so we need to remember processed uri */
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

        private val log = Logger("ContentObserver")
        private val CONTENT_SMS_SENT = Uri.parse("content://sms/sent")
        private val CONTENT_SMS = Uri.parse("content://sms")
    }
}