package com.bopr.android.smailer.provider.telephony

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.net.toUri
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
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class ContentObserverService : Service() {

    private lateinit var observer: ContentObserver

    override fun onCreate() {
        observer = SmsContentObserver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log.debug("Running")

        contentResolver.registerContentObserver("content://sms".toUri(), true, observer)

        val notification = notifications.createServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NTF_SERVICE, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NTF_SERVICE, notification)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(observer)
        super.onDestroy()

        log.verb("Destroyed")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private inner class SmsContentObserver : ContentObserver(Handler(Looper.getMainLooper())) {

        fun processSms(smsId: String) {
            applicationContext.apply {
                contentResolver.query(
                    "content://sms/sent".toUri(),
                    null,
                    "_id=?",
                    arrayOf(smsId),
                    null
                )?.withFirst {
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
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri?.let {
                val segments = uri.pathSegments
                if (segments.isNotEmpty() && (segments[0] == "sent" || segments.size > 1)) {
                    val smsId = if (segments[0] == "sent") segments[1] else segments[0]
                    processSms(smsId)
                }
            }
        }
    }

    companion object {

        private val log = Logger("ContentObserver")
    }
}