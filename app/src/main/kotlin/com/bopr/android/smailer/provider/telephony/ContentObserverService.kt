package com.bopr.android.smailer.provider.telephony

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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

    private lateinit var contentObserver: ContentObserver

    override fun onCreate() {
        contentObserver = SmsContentObserver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log.debug("Running")

        contentResolver.registerContentObserver("content://sms".toUri(), true, contentObserver)

        val notification = notifications.createServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NTF_SERVICE, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NTF_SERVICE, notification)
        }

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

    private inner class SmsContentObserver : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri?.let {
                val segments = uri.pathSegments
                if (segments.isNotEmpty() && (segments[0] == "sent" || segments.size > 1)) {
                    val smsId = if (segments[0] == "sent") segments[1] else segments[0]

                    val workRequest = OneTimeWorkRequestBuilder<SmsProcessWorker>()
                        .setInputData(workDataOf("sms_id" to smsId))
                        .build()

                    WorkManager.getInstance(applicationContext).enqueue(workRequest)
                }
            }
        }
    }

    companion object {

        private val log = Logger("ContentObserver")
    }
}

internal class SmsProcessWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val smsId = inputData.getString("sms_id") ?: return Result.failure()

        applicationContext.contentResolver.query(
            "content://sms/sent".toUri(),
            null,
            "_id=?",
            arrayOf(smsId),
            null
        )?.withFirst {
            applicationContext.processPhoneCall(
                PhoneCallData(
                    startTime = getLong("date"),
                    phone = getString("address"),
                    endTime = getLong("date"),
                    text = getStringOrNull("body")
                )
            )
        }

        return Result.success()
    }
}