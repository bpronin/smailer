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
import com.bopr.android.smailer.ui.MainActivity
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
    private val dispatcher: MessageDispatcher = MessageDispatcher(context),
    private val notifications: NotificationsHelper = NotificationsHelper(context),
) {

    private val settings: Settings = Settings(context)

    fun addRecord(info: PhoneCallInfo) {
        log.debug("Add record").verb(info)

        commitRecord(info.apply {
            bypassFlags = getBypassFlags(this)
            processState = if (bypassFlags == FLAG_BYPASS_NONE) STATE_PENDING else STATE_IGNORED
        })
    }

    fun processRecords(): Int {
        val records = database.use {
            it.phoneCalls.filterPending
        }

        val recordsCount = records.size

        if (recordsCount > 0) {
            log.debug("Processing $recordsCount record(s)")

            prepareDispatcher()

            for (record in records) {
                record.apply {
                    processTime = currentTimeMillis()
                    location = context.getGeoLocation()
                }

                dispatchRecord(
                    info = record,
                    onSuccess = {
                        commitRecord(record.apply {
                            processState = STATE_PROCESSED
                        })
                    },
                    onError = {
                        commitRecord(record.apply {
                            processState = STATE_PENDING
                        })
                    }
                )
            }
        } else {
            log.debug("No pending records")
        }

        return recordsCount
    }

    private fun commitRecord(info: PhoneCallInfo) {
        database.use {
            it.commit {
                phoneCalls.put(info)
            }
        }
    }

    private fun prepareDispatcher() {
        dispatcher.initialize()

        log.debug("Dispatcher prepared")
    }

    private fun dispatchRecord(
        info: PhoneCallInfo,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val message = Message(payload = info)

        log.debug("Dispatching message")

        dispatcher.dispatch(
            message,
            onSuccess = {
                notifySuccess()
                onSuccess()
            },
            onError = {
                log.warn("Dispatch failed: ", it)

                onError(it)
            }
        )
    }

    private fun notifySuccess() {
        if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS))
            notifications.notifyInfo(
                title = context.getString(R.string.email_successfully_send),
                target = MainActivity::class
            )
    }

    private fun getBypassFlags(info: PhoneCallInfo) = PhoneCallFilter(
        settings.getMailTriggers(),
        database.phoneBlacklist,
        database.phoneWhitelist,
        database.textBlacklist,
        database.textWhitelist
    ).test(info)


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