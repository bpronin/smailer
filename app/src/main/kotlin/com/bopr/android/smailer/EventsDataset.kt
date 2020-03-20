package com.bopr.android.smailer

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.Database.Companion.COLUMN_ACCEPTOR
import com.bopr.android.smailer.Database.Companion.COLUMN_DETAILS
import com.bopr.android.smailer.Database.Companion.COLUMN_END_TIME
import com.bopr.android.smailer.Database.Companion.COLUMN_IS_INCOMING
import com.bopr.android.smailer.Database.Companion.COLUMN_IS_MISSED
import com.bopr.android.smailer.Database.Companion.COLUMN_LATITUDE
import com.bopr.android.smailer.Database.Companion.COLUMN_LONGITUDE
import com.bopr.android.smailer.Database.Companion.COLUMN_PHONE
import com.bopr.android.smailer.Database.Companion.COLUMN_PROCESS_STATUS
import com.bopr.android.smailer.Database.Companion.COLUMN_PROCESS_TIME
import com.bopr.android.smailer.Database.Companion.COLUMN_READ
import com.bopr.android.smailer.Database.Companion.COLUMN_START_TIME
import com.bopr.android.smailer.Database.Companion.COLUMN_STATE
import com.bopr.android.smailer.Database.Companion.COLUMN_TEXT
import com.bopr.android.smailer.Database.Companion.TABLE_EVENTS
import com.bopr.android.smailer.util.database.*
import com.bopr.android.smailer.util.strings
import org.slf4j.LoggerFactory

/**
 * Convenience [Cursor] wrapper.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventsDataset(helper: SQLiteOpenHelper, modifications: MutableSet<String>)
    : Dataset<PhoneEvent>(TABLE_EVENTS, helper, modifications) {

    private val log = LoggerFactory.getLogger("Database")

    /**
     * Returns count of unread phone events.
     */
    val unreadCount: Long
        get() {
            return readable.query(TABLE_EVENTS, strings(Database.COLUMN_COUNT), "$COLUMN_READ<>1")
                    .useFirst { getLong(0) }
        }

    /**
     * Returns pending phone events.
     */
    val filterPending: List<PhoneEvent>
        get() {
            return readable.query(
                    table = TABLE_EVENTS,
                    selection = "$COLUMN_STATE=?",
                    selectionArgs = strings(PhoneEvent.STATE_PENDING),
                    order = "$COLUMN_START_TIME DESC"
            ).useToList(::get)
        }

    /**
     * Marks all events as read.
     */
    fun markAllAsRead(read: Boolean) {
        writable.batch {
            update(TABLE_EVENTS, values {
                put(COLUMN_READ, read)
            })
        }
        modified()

        log.debug("All events marked as read")
    }

    override fun query(): Cursor {
        return readable.query(
                table = TABLE_EVENTS,
                order = "$COLUMN_START_TIME DESC"
        )
    }

    override fun get(cursor: Cursor): PhoneEvent {
        return cursor.run {
            PhoneEvent(
                    phone = getString(COLUMN_PHONE)!!,
                    isIncoming = getBoolean(COLUMN_IS_INCOMING),
                    startTime = getLong(COLUMN_START_TIME),
                    endTime = getLong(COLUMN_END_TIME),
                    isMissed = getBoolean(COLUMN_IS_MISSED),
                    text = getString(COLUMN_TEXT),
                    location = GeoCoordinates(
                            getDouble(COLUMN_LATITUDE),
                            getDouble(COLUMN_LONGITUDE)
                    ),
                    details = getString(COLUMN_DETAILS),
                    state = getInt(COLUMN_STATE),
                    acceptor = getString(COLUMN_ACCEPTOR)!!,
                    processStatus = getInt(COLUMN_PROCESS_STATUS),
                    processTime = getLong(COLUMN_PROCESS_TIME),
                    isRead = getBoolean(COLUMN_READ)
            )
        }
    }

    override fun add(element: PhoneEvent): Boolean {
        val values = values {
            put(COLUMN_PHONE, element.phone)
            put(COLUMN_ACCEPTOR, element.acceptor)
            put(COLUMN_START_TIME, element.startTime)
            put(COLUMN_STATE, element.state)
            put(COLUMN_PROCESS_STATUS, element.processStatus)
            put(COLUMN_PROCESS_TIME, element.processTime)
            put(COLUMN_IS_INCOMING, element.isIncoming)
            put(COLUMN_IS_MISSED, element.isMissed)
            put(COLUMN_END_TIME, element.endTime)
            put(COLUMN_TEXT, element.text)
            put(COLUMN_DETAILS, element.details)
            put(COLUMN_READ, element.isRead)
            element.location?.let {
                put(COLUMN_LATITUDE, it.latitude)
                put(COLUMN_LONGITUDE, it.longitude)
            }
        }

        writable.run {
            modified()
            return if (insertWithOnConflict(TABLE_EVENTS, null, values, CONFLICT_IGNORE) == -1L) {
                update(TABLE_EVENTS, values, "${COLUMN_START_TIME}=? AND ${COLUMN_ACCEPTOR}=?",
                        strings(element.startTime, element.acceptor))

                log.debug("Updated: $values")
                false
            } else {
                log.debug("Inserted: $values")
                true
            }
        }
    }

    override fun remove(element: PhoneEvent): Boolean {
        return writable.delete(TABLE_EVENTS,
                "$COLUMN_ACCEPTOR=? AND $COLUMN_START_TIME=?",
                strings(element.acceptor, element.startTime)
        ) != 0
    }

}