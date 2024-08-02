package com.bopr.android.smailer.provider.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.processor.EventDispatcher
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.provider.EventState.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventData.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.GeoLocation.Companion.requestGeoLocation
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

/**
 * Sends out messages for phone events.
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

    fun process(data: PhoneEventData) {
        log.debug("Processing: {}", data)

        database.use {
            updateEvent(data) {
                database.commit {
                    events.add(data)
                }
            }
        }
    }

    fun processPending() {
        database.use {
            val pendingEvents = database.events.filterPending
            if (pendingEvents.isEmpty()) {
                log.debug("No pending events")
            } else {
                log.debug("Processing ${pendingEvents.size} pending event(s)")

                prepare()
                database.commit {
                    batch {
                        for (event in pendingEvents) {
                            event.processTime = currentTimeMillis()
                            if (dispatch(event)) {
                                event.state = STATE_PROCESSED
                                events.add(event)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateEvent(data: PhoneEventData, onComplete: () -> Unit) {
        context.requestGeoLocation(database) { currentLocation ->

            prepare()

            data.apply {
                location = currentLocation
                processStatus = eventFilter().test(this)
                processTime = currentTimeMillis()

                if (processStatus != STATUS_ACCEPTED) {
                    state = STATE_IGNORED
                    log.debug("Ignored")
                } else if (dispatch(this)) {
                    state = STATE_PROCESSED
                    log.debug("Processed")
                } else {
                    state = STATE_PENDING
                    log.debug("Postponed")
                }
            }

            onComplete()
        }
    }

    private fun prepare() {
        eventDispatcher.prepare()

        log.debug("Dispatcher prepared")
    }

    private fun dispatch(data: PhoneEventData): Boolean {
        log.debug("Dispatching event")

        return try {
            eventDispatcher.dispatch(Event(payload = data))
            if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS)) {
                notifySuccess()
            }
            true
        } catch (x: Exception) {
            log.warn("Failed dispatching: ", x)

            false
        }
    }

    private fun notifySuccess() {
        notifications.notifyInfo(
            context.getString(R.string.email_successfully_send),
            null,
            MainActivity::class
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

        private val log = LoggerFactory.getLogger("PhoneEventProcessor")
    }

}