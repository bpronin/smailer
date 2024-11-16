package com.bopr.android.smailer.provider.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.messenger.EventDispatcher
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.provider.EventState.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventData.Companion.ACCEPT_STATE_ACCEPTED
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.useIt
import java.lang.System.currentTimeMillis

/**
 * Precesses phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneEventProcessor(
    private val context: Context,
    private val database: Database = Database(context),
    private val eventDispatcher: EventDispatcher = EventDispatcher(context),
    private val notifications: NotificationsHelper = NotificationsHelper(context),
) {

    private val settings: Settings = Settings(context)

    fun process(event: PhoneEventData) {
        log.debug("Processing: $event")

        database.useIt {
            commit {
                val currentLocation = context.getGeoLocation(this)
                events.add(
                    event.apply {
                        location = currentLocation
                        acceptState = eventFilter().test(this)
                        processState = (if (acceptState == ACCEPT_STATE_ACCEPTED)
                            STATE_PENDING else STATE_IGNORED)
                    })
            }

            processPending()

//                context.requestGeoLocation(
//                database = this,
//                onSuccess = { currentLocation ->
//                    commit {
//                        events.add(
//                            event.apply {
//                                location = currentLocation
//                                acceptState = eventFilter().test(this)
//                                processState = (if (acceptState == ACCEPT_STATE_ACCEPTED)
//                                    STATE_PENDING else STATE_IGNORED)
//                            })
//                    }
//
//                    processPending()
//                },
//                onError = { error ->
//                    log.error("Processing failed", error)
//                }
//            )
        }
    }

    fun processPending(): Int {
        var processedEventsCount = 0

        database.useIt {
            val pendingEvents = events.filterPending

            if (pendingEvents.isEmpty()) {
                log.debug("No pending events")
            } else {
                log.debug("Processing ${pendingEvents.size} pending event(s)")

                prepare()
                commit {
                    batch {
                        for (event in pendingEvents) {
                            event.processTime = currentTimeMillis()
                            dispatch(event,
                                onSuccess = {
                                    event.processState = STATE_PROCESSED
                                    events.add(event)
                                    processedEventsCount++
                                },
                                onError = {
                                    event.processState = STATE_PENDING
                                    events.add(event)
                                }
                            )
                        }
                    }
                }
            }
        }

        log.debug("Processed $processedEventsCount event(s)")

        return processedEventsCount
    }

    private fun prepare() {
        eventDispatcher.prepare()

        log.debug("Dispatcher prepared")
    }

    private fun dispatch(
        data: PhoneEventData,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        log.debug("Dispatching event...")

        eventDispatcher.dispatch(
            Event(payload = data),
            onSuccess = {
                notifySuccess()
                onSuccess()
            },
            onError = { error ->
                log.warn("Dispatch failed: ", error)

                onError(error)
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

    private fun eventFilter() = PhoneEventFilter(
        settings.getEmailTriggers(),
        database.phoneBlacklist,
        database.phoneWhitelist,
        database.smsTextBlacklist,
        database.smsTextWhitelist
    )

    companion object {

        private val log = Logger("PhoneEventProcessor")
    }

}