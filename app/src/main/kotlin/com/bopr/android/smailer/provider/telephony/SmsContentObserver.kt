package com.bopr.android.smailer.provider.telephony

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
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
internal class SmsContentObserver(private val context: Context) : ContentObserver(
    Handler(Looper.getMainLooper())
) {

    private fun processContent(smsId: String) {
        context.applicationContext.apply {
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
            log.debug("Content changed: $it")
            val segments = uri.pathSegments
            if (segments.isNotEmpty() && (segments[0] == "sent" || segments.size > 1)) {
                val smsId = if (segments[0] == "sent") segments[1] else segments[0]
                processContent(smsId)
            }
        }
    }

    fun register() {
        context.contentResolver.registerContentObserver(
            "content://sms".toUri(),
            true,
            this
        )
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