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
import android.provider.Telephony.TextBasedSmsColumns.TYPE
import androidx.core.net.toUri
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

    private fun processContent() {
        context.contentResolver.query(
            "content://sms/sent".toUri(),
            arrayOf(_ID, DATE, ADDRESS, BODY, TYPE),
            null,
            null,
            "$DATE DESC LIMIT 1"
        )?.tryWithFirst {
            val id = getLong(_ID)
            if (lastProcessedId != id) {
                lastProcessedId = id
                
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

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let {
            log.debug("Content changed: $it")
            val segments = uri.pathSegments
            if (segments.size == 1 && segments[0] != "raw") {
                processContent()
            }
        }
    }

    fun register() {
        context.contentResolver.registerContentObserver("content://sms".toUri(), true, this)
        log.debug("Enabled")
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
        log.debug("Disabled")
    }

    companion object {
        private val log = Logger("SmsObserver")
    }
}