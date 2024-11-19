package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.BackoffPolicy.EXPONENTIAL
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.messenger.Message
import com.bopr.android.smailer.messenger.MessageDispatcher
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.FLAG_BYPASS_NONE
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.FLAG_BYPASS_NO_CONSUMERS
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import com.bopr.android.smailer.util.Logger
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Precesses phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneCallProcessor(
    private val context: Context,
    private val database: Database = Database(context),
    private val notifications: NotificationsHelper = NotificationsHelper(context),
) {

    private val settings = Settings(context)
    private val dispatcher = MessageDispatcher(context)

    fun addRecord(info: PhoneCallInfo) {
        log.debug("Add record").verb(info)

        putRecord(info.apply {
            bypassFlags = detectBypassFlags(this)
            if (bypassFlags != FLAG_BYPASS_NONE) processState = STATE_IGNORED
        })
    }

    fun processRecords(): Int {
        val records = database.use { it.phoneCalls.filterPending }
        if (records.isEmpty()) {
            log.debug("No pending records")

            return 0
        }

        log.debug("Processing ${records.size} record(s)")

        val canDispatch = dispatcher.initialize()

        for (record in records) {
            if (canDispatch) {
                record.apply {
                    processTime = currentTimeMillis()
                    location = context.getGeoLocation()
                }

                val message = Message(payload = record)

                log.debug("Dispatching message").verb(message)

                dispatcher.dispatch(
                    message,
                    onSuccess = {
                        notifySuccess()
                        putRecord(record.apply {
                            processState = STATE_PROCESSED
                        })
                    },
                    onError = {
                        putRecord(record.apply {
                            processState = STATE_PENDING
                        })
                    }
                )
            } else {
                putRecord(record.apply {
                    bypassFlags += FLAG_BYPASS_NO_CONSUMERS
                    processState = STATE_IGNORED
                })
            }
        }

        return records.size
    }

    private fun putRecord(info: PhoneCallInfo) {
        database.use {
            it.commit {
                phoneCalls.put(info)
            }
        }
    }

    private fun notifySuccess() {
        if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS))
            notifications.notifyInfo(
                title = context.getString(R.string.email_successfully_send),
                target = MainActivity::class
            )
    }

    private fun detectBypassFlags(info: PhoneCallInfo): Bits {
        return PhoneCallFilter(
            settings.getMailTriggers(),
            database.phoneBlacklist,
            database.phoneWhitelist,
            database.textBlacklist,
            database.textWhitelist
        ).test(info)
    }

    companion object {

        private val log = Logger("PhoneCallProcessor")

        fun Context.processPhoneCall(info: PhoneCallInfo) {
            /* add record to database now */
            PhoneCallProcessor(this).addRecord(info)

            /* process it later */
            WorkManager.getInstance(this).enqueue(
                OneTimeWorkRequest.Builder(PhoneCallProcessingWorker::class.java)
                    .setBackoffCriteria(EXPONENTIAL, 1, MINUTES)
                    .build()
            )
        }
    }

}