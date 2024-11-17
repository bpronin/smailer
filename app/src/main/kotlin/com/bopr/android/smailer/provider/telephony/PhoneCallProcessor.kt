package com.bopr.android.smailer.provider.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.messenger.MessageDispatcher
import com.bopr.android.smailer.messenger.Message
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.ACCEPT_STATE_ACCEPTED
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import com.bopr.android.smailer.util.Logger
import java.lang.System.currentTimeMillis

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

    fun process(info: PhoneCallInfo) {
        addRecord(info)
        processRecords()
    }

    fun addRecord(info: PhoneCallInfo) {
        log.debug("Adding: $info")

        database.use {
            database.commit {
                phoneCalls.add(
                    info.apply {
                        location = context.getGeoLocation()
                        acceptState = recordFilter().test(this)
                        processState = if (acceptState == ACCEPT_STATE_ACCEPTED)
                            STATE_PENDING else STATE_IGNORED
                    }
                )
            }
        }
    }

    fun processRecords(): Int {
        var processedCount = 0

        database.use {
            val pendingRecords = database.phoneCalls.filterPending

            if (pendingRecords.isNotEmpty()) {
                log.debug("Processing ${pendingRecords.size} record(s)")

                prepareDispatcher()

                database.commit {
                    batch {
                        for (record in pendingRecords) {
                            record.processTime = currentTimeMillis()
                            dispatchRecord(
                                info = record,
                                onSuccess = {
                                    record.processState = STATE_PROCESSED
                                    phoneCalls.add(record)
                                    processedCount++
                                },
                                onError = {
                                    record.processState = STATE_PENDING
                                    phoneCalls.add(record)
                                }
                            )
                        }
                    }
                }
            } else {
                log.debug("No pending records")
            }
        }

        log.debug("Processed $processedCount record(s)")

        return processedCount
    }

    private fun prepareDispatcher() {
        dispatcher.prepare()

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

    private fun recordFilter() = PhoneCallFilter(
        settings.getEmailTriggers(),
        database.phoneBlacklist,
        database.phoneWhitelist,
        database.textBlacklist,
        database.textWhitelist
    )

    companion object {

        private val log = Logger("PhoneCallProcessor")
    }

}