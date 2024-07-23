package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.util.GeoLocation
import com.bopr.android.smailer.util.strings
import org.slf4j.LoggerFactory

class PhoneEventsDataset(helper: SQLiteOpenHelper, modifications: MutableSet<String>) :
    Dataset<PhoneEventData>(Database.TABLE_PHONE_EVENTS, helper, modifications) {

    override val keyColumns = strings(Database.COLUMN_START_TIME, Database.COLUMN_ACCEPTOR)

    /**
     * Returns count of unread phone events.
     */
    val unreadCount: Long
        get() = read {
            count(tableName, "${Database.COLUMN_READ}<>1")
        }

    /**
     * Returns pending phone events.
     */
    val filterPending: Set<PhoneEventData>
        get() = read {
            query(
                tableName,
                selection = "${Database.COLUMN_STATE}=${STATE_PENDING}",
                order = "${Database.COLUMN_START_TIME} DESC"
            )
        }.toSet(::get)

    /**
     * Marks all events as read.
     */
    fun markAllAsRead(read: Boolean) {
        write {
            update(tableName, values {
                put(Database.COLUMN_READ, read)
            })
        }

        log.debug("All events marked as read")
    }

    override fun query(): Cursor {
        return read {
            query(tableName, order = "${Database.COLUMN_START_TIME} DESC")
        }
    }

    override fun key(element: PhoneEventData): Array<String> {
        return strings(element.startTime, element.acceptor)
    }

    override fun get(cursor: Cursor): PhoneEventData {
        return cursor.run {
            PhoneEventData(
                phone = getString(Database.COLUMN_PHONE),
                isIncoming = getBoolean(Database.COLUMN_IS_INCOMING),
                startTime = getLong(Database.COLUMN_START_TIME),
                endTime = getLongOrNull(Database.COLUMN_END_TIME),
                isMissed = getBoolean(Database.COLUMN_IS_MISSED),
                text = getStringOrNull(Database.COLUMN_TEXT),
                location = GeoLocation.fromCoordinates(
                    getDoubleOrNull(Database.COLUMN_LATITUDE),
                    getDoubleOrNull(Database.COLUMN_LONGITUDE)
                ),
                details = getStringOrNull(Database.COLUMN_DETAILS),
                state = getInt(Database.COLUMN_STATE),
                acceptor = getString(Database.COLUMN_ACCEPTOR),
                processStatus = getInt(Database.COLUMN_PROCESS_STATUS),
                processTime = getLongOrNull(Database.COLUMN_PROCESS_TIME),
                isRead = getBoolean(Database.COLUMN_READ)
            )
        }
    }

    override fun values(element: PhoneEventData): ContentValues {
        return values {
            put(Database.COLUMN_PHONE, element.phone)
            put(Database.COLUMN_ACCEPTOR, element.acceptor)
            put(Database.COLUMN_START_TIME, element.startTime)
            put(Database.COLUMN_STATE, element.state)
            put(Database.COLUMN_PROCESS_STATUS, element.processStatus)
            put(Database.COLUMN_PROCESS_TIME, element.processTime)
            put(Database.COLUMN_IS_INCOMING, element.isIncoming)
            put(Database.COLUMN_IS_MISSED, element.isMissed)
            put(Database.COLUMN_END_TIME, element.endTime)
            put(Database.COLUMN_TEXT, element.text)
            put(Database.COLUMN_DETAILS, element.details)
            put(Database.COLUMN_READ, element.isRead)
            element.location?.run {
                put(Database.COLUMN_LATITUDE, latitude)
                put(Database.COLUMN_LONGITUDE, longitude)
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("Database")
    }
}