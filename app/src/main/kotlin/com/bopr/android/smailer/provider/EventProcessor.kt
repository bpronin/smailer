package com.bopr.android.smailer.provider

import android.content.Context
import androidx.work.OneTimeWorkRequest.Builder
import androidx.work.WorkManager
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_NO_CONSUMERS
import com.bopr.android.smailer.messenger.EventPayload
import com.bopr.android.smailer.messenger.MessageDispatcher
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import com.bopr.android.smailer.util.Logger
import java.lang.System.currentTimeMillis
import kotlin.reflect.KClass

abstract class EventProcessor<P : EventPayload>(private val context: Context) {

    private val dispatcher = MessageDispatcher(context)

    fun scheduleProcess(payload: P, worker: KClass<out EventProcessorWorker>) {
        val flags = getBypassReason(payload)
        val event = Event(
            bypassFlags = flags,
            processState = if (flags.isEmpty()) STATE_PENDING else STATE_IGNORED,
            payload = payload
        )

        log.debug("Scheduling process: $event")

        val inserted = context.database.commit { events.insert(event) }
        if (inserted) {
            WorkManager.getInstance(context).enqueue(Builder(worker).build())
        }
    }

    suspend fun processPending(): Int {
        val events = context.database.events.drainPending()
        if (events.isEmpty()) {
            log.debug("No unprocessed events")

            return 0
        }

        log.debug("Processing ${events.size} event(s)")

        val canDispatch = dispatcher.prepare()

        for (event in events) {
            if (canDispatch) {
                event.apply {
                    processTime = currentTimeMillis()
                    location = context.getGeoLocation()
                }

                dispatcher.dispatch(
                    event,
                    onSuccess = {
                        updateDatabase(event.apply {
                            processState = STATE_PROCESSED
                        })
                    },
                    onError = {
                        updateDatabase(event.apply {
                            processState = STATE_PENDING
                        })
                    }
                )
            } else {
                updateDatabase(event.apply {
                    bypassFlags += FLAG_BYPASS_NO_CONSUMERS
                    processState = STATE_IGNORED
                })
            }
        }

        return events.size
    }

    abstract fun getBypassReason(payload: P): Bits

    private fun updateDatabase(event: Event) = context.database.commit {
        events.updateState(event)
    }

    companion object {
        private val log = Logger("EventProcessor")
    }
}