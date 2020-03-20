package com.bopr.android.smailer

import android.content.ContentValues
import android.database.Cursor
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
import com.bopr.android.smailer.GeoCoordinates.Companion.coordinatesOf
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.util.database.*
import com.bopr.android.smailer.util.strings
import org.slf4j.LoggerFactory

class EventsDataset(helper: SQLiteOpenHelper, modifications: MutableSet<String>)
    : Dataset<PhoneEvent>(TABLE_EVENTS, helper, modifications) {

    private val log = LoggerFactory.getLogger("Database")

    override val keyColumns = strings(COLUMN_START_TIME, COLUMN_ACCEPTOR)

    /**
     * Returns count of unread phone events.
     */
    val unreadCount: Long
        get() = read {
            count(tableName, "$COLUMN_READ<>1")
        }

    /**
     * Returns pending phone events.
     */
    val filterPending: Set<PhoneEvent>
        get() = read {
            query(tableName,
                    selection = "$COLUMN_STATE=$STATE_PENDING",
                    order = "$COLUMN_START_TIME DESC"
            )
        }.toSet(::get)

    /**
     * Marks all events as read.
     */
    fun markAllAsRead(read: Boolean) {
        write {
            update(tableName, values {
                put(COLUMN_READ, read)
            })
        }

        log.debug("All events marked as read")
    }

    override fun query(): Cursor {
        return read {
            query(tableName, order = "$COLUMN_START_TIME DESC")
        }
    }

    override fun key(element: PhoneEvent): Array<String> {
        return strings(element.startTime, element.acceptor)
    }

    override fun get(cursor: Cursor): PhoneEvent {
        return cursor.run {
            PhoneEvent(
                    phone = getString(COLUMN_PHONE),
                    isIncoming = getBoolean(COLUMN_IS_INCOMING),
                    startTime = getLong(COLUMN_START_TIME),
                    endTime = getLongOrNull(COLUMN_END_TIME),
                    isMissed = getBoolean(COLUMN_IS_MISSED),
                    text = getStringOrNull(COLUMN_TEXT),
                    location = coordinatesOf(
                            getDoubleOrNull(COLUMN_LATITUDE),
                            getDoubleOrNull(COLUMN_LONGITUDE)
                    ),
                    details = getStringOrNull(COLUMN_DETAILS),
                    state = getInt(COLUMN_STATE),
                    acceptor = getString(COLUMN_ACCEPTOR),
                    processStatus = getInt(COLUMN_PROCESS_STATUS),
                    processTime = getLongOrNull(COLUMN_PROCESS_TIME),
                    isRead = getBoolean(COLUMN_READ)
            )
        }
    }

    override fun values(element: PhoneEvent): ContentValues {
        return values {
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
            element.location?.run {
                put(COLUMN_LATITUDE, latitude)
                put(COLUMN_LONGITUDE, longitude)
            }
        }
    }
}