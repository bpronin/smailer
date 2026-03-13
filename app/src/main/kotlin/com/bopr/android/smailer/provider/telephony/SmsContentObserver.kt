package com.bopr.android.smailer.provider.telephony

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.BaseColumns._ID
import android.provider.Telephony.TextBasedSmsColumns.ADDRESS
import android.provider.Telephony.TextBasedSmsColumns.BODY
import android.provider.Telephony.TextBasedSmsColumns.DATE
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
import android.provider.Telephony.TextBasedSmsColumns.TYPE
import androidx.core.net.toUri
import com.bopr.android.smailer.data.getInt
import com.bopr.android.smailer.data.getLong
import com.bopr.android.smailer.data.getString
import com.bopr.android.smailer.data.getStringOrNull
import com.bopr.android.smailer.data.tryWithFirst
import com.bopr.android.smailer.provider.telephony.PhoneCallEventProcessor.Companion.scheduleProcessPhoneCall
import com.bopr.android.smailer.util.Logger

/**
 * Listens to changes in sms content. Used to process outgoing SMS.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
internal class SmsContentObserver(private val context: Context) : ContentObserver(
    Handler(Looper.getMainLooper())
) {

    private var lastProcessedId: Long = -1

    private fun processContent(id: Long) {
        if (lastProcessedId == id) {
            return
        }
        lastProcessedId = id

        context.contentResolver.query(
            contentUri,
            arrayOf(DATE, ADDRESS, BODY, TYPE),
            "$_ID = $id",
            null, null
        )?.tryWithFirst {
            when (getInt(TYPE)) {
                MESSAGE_TYPE_SENT -> {
                    log.debug("Found processable content: $id")
                    context.scheduleProcessPhoneCall(
                        PhoneCallData(
                            startTime = getLong(DATE),
                            phone = getString(ADDRESS),
                            endTime = getLong(DATE),
                            text = getStringOrNull(BODY)
                        )
                    )
                }
            }
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let {
            log.debug("Content changed: $it")
            val segments = it.pathSegments
            if (segments.isNotEmpty() && segments[0] != "raw") {
                processContent(segments[0].toLong())
            }
        }
    }

    fun register() {
        context.contentResolver.registerContentObserver(contentUri, true, this)
        log.debug("Enabled")
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
        log.debug("Disabled")
    }

    companion object {
        private val log = Logger("SmsObserver")
        private val contentUri = "content://sms".toUri()
    }
}