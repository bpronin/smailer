package com.bopr.android.smailer.provider

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_NO_CONSUMERS
import com.bopr.android.smailer.messenger.EventPayload
import com.bopr.android.smailer.messenger.MessageDispatcher
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.ContextOwner
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import com.bopr.android.smailer.util.Logger
import java.lang.System.currentTimeMillis

abstract class Processor<T : EventPayload>(private val context: Context) : ContextOwner {

    protected val database: Database = Database(context)
    protected val dispatcher = MessageDispatcher(context)
    protected val notifications by lazy { NotificationsHelper(context) }

    abstract fun getBypassReason(data: T): Bits

    override fun requireContext() = context

    fun add(data: T) {
        log.debug("Add record").verb(data)

        val bypassFlags = getBypassReason(data)

        putRecord(
            Event(
                bypassFlags = bypassFlags,
                processState = if (bypassFlags.isEmpty()) STATE_PENDING else STATE_IGNORED,
                payload = data
            )
        )
    }

    fun process(): Int {
        val events = database.useIt { events.pending }
        if (events.isEmpty()) {
            log.debug("No unprocessed events")

            return 0
        }

        log.debug("Processing ${events.size} event(s)")

        val canDispatch = dispatcher.initialize()

        for (event in events) {
            if (canDispatch) {
                event.apply {
                    processTime = currentTimeMillis()
                    location = context.getGeoLocation()
                }

                log.debug("Dispatching event").verb(event)

                dispatcher.dispatch(
                    event,
                    onSuccess = {
                        putRecord(event.apply {
                            processState = STATE_PROCESSED
                        })
                    },
                    onError = {
                        putRecord(event.apply {
                            processState = STATE_PENDING
                        })
                    }
                )
            } else {
                putRecord(event.apply {
                    bypassFlags += FLAG_BYPASS_NO_CONSUMERS
                    processState = STATE_IGNORED
                })
            }
        }

        return events.size
    }

    private fun putRecord(event: Event) {
        database.useIt {
            commit {
                events.put(event)
            }
        }
    }

    companion object {

        private val log = Logger("EventProcessor")
    }
}