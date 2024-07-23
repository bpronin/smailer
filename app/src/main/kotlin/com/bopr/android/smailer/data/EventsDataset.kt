package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.os.Parcelable
import com.bopr.android.smailer.data.Database.Companion.COLUMN_ACCEPTOR
import com.bopr.android.smailer.data.Database.Companion.COLUMN_LATITUDE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_LONGITUDE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_PROCESS_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_READ
import com.bopr.android.smailer.data.Database.Companion.COLUMN_START_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_STATE
import com.bopr.android.smailer.data.Database.Companion.TABLE_EVENTS
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.GeoLocation
import com.bopr.android.smailer.util.strings
import org.slf4j.LoggerFactory

// TODO: Draft
class EventsDataset(
    helper: SQLiteOpenHelper,
    modifications: MutableSet<String>
) :
    Dataset<Event>(TABLE_EVENTS, helper, modifications) {

    override val keyColumns = strings(COLUMN_START_TIME, COLUMN_ACCEPTOR)

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

    override fun key(element: Event): Array<String> {
        return strings(element.time, element.device)
    }

    override fun get(cursor: Cursor): Event {
        return cursor.run {
            Event(
                time = getLong(COLUMN_START_TIME),
                location = GeoLocation.fromCoordinates(
                    getDoubleOrNull(COLUMN_LATITUDE),
                    getDoubleOrNull(COLUMN_LONGITUDE)
                ),
                state = getInt(COLUMN_STATE),
                device = getString(COLUMN_ACCEPTOR),
                processTime = getLongOrNull(COLUMN_PROCESS_TIME),
                isRead = getBoolean(COLUMN_READ),
                payload = getPayload()
            )
        }
    }

    override fun values(element: Event): ContentValues {
        return values {
            put(COLUMN_START_TIME, element.time)
            put(COLUMN_ACCEPTOR, element.device)
            put(COLUMN_STATE, element.state)
            put(COLUMN_PROCESS_TIME, element.processTime)
            put(COLUMN_READ, element.isRead)
            element.location?.run {
                put(COLUMN_LATITUDE, latitude)
                put(COLUMN_LONGITUDE, longitude)
            }
        }
    }

    private fun getPayload(): Parcelable {
        TODO("Not yet implemented")
    }

    companion object {

        private val log = LoggerFactory.getLogger("Database")
    }
}