package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.data.Database.Companion.COLUMN_ACCEPTOR
import com.bopr.android.smailer.data.Database.Companion.COLUMN_BYPASS
import com.bopr.android.smailer.data.Database.Companion.COLUMN_CREATE_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_LATITUDE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_LONGITUDE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_PROCESS
import com.bopr.android.smailer.data.Database.Companion.COLUMN_PROCESS_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_READ
import com.bopr.android.smailer.data.Database.Companion.COLUMN_STATE
import com.bopr.android.smailer.data.Database.Companion.TABLE_EVENTS
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.GeoLocation.Companion.fromCoordinates
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.strings

class EventDataset(
    helper: SQLiteOpenHelper,
    modifications: MutableSet<String>
) :
    Dataset<Event>(TABLE_EVENTS, helper, modifications) {

    override val keyColumns = strings(COLUMN_CREATE_TIME, COLUMN_ACCEPTOR)

    /**
     * Returns count of unread events.
     */
    val unreadCount: Long
        get() = read {
            count(tableName, "$COLUMN_READ<>1")
        }

    /**
     * Returns pending events.
     */
    val filterPending: Set<Event>
        get() = read {
            query(
                tableName,
                selection = "$COLUMN_STATE=${STATE_PENDING}",
                order = "$COLUMN_CREATE_TIME DESC"
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

    override fun query(): Cursor = read {
        query(tableName, order = "$COLUMN_CREATE_TIME DESC")
    }

    override fun keyOf(element: Event) = strings(element.createTime, element.acceptor)

    override fun get(cursor: Cursor): Event = cursor.run {
        Event(
            createTime = getLong(COLUMN_CREATE_TIME),
            processTime = getLongOrNull(COLUMN_PROCESS_TIME),
            acceptor = getString(COLUMN_ACCEPTOR),
            processState = getInt(COLUMN_STATE),
            bypassFlags = Bits(getInt(COLUMN_BYPASS)),
            processFlags = Bits(getInt(COLUMN_PROCESS)),
            isRead = getBoolean(COLUMN_READ),
            location = fromCoordinates(
                getDoubleOrNull(COLUMN_LATITUDE),
                getDoubleOrNull(COLUMN_LONGITUDE)
            )
        )
    }

    override fun values(element: Event): ContentValues = values {
        element.apply {
            put(COLUMN_CREATE_TIME, createTime)
            put(COLUMN_PROCESS_TIME, processTime)
            put(COLUMN_ACCEPTOR, acceptor)
            put(COLUMN_STATE, processState)
            put(COLUMN_BYPASS, bypassFlags.toInt())
            put(COLUMN_PROCESS, processFlags.toInt())
            put(COLUMN_READ, isRead)
            location?.apply {
                put(COLUMN_LATITUDE, latitude)
                put(COLUMN_LONGITUDE, longitude)
            }
        }
    }

    companion object {

        private val log = Logger("Database")
    }
}