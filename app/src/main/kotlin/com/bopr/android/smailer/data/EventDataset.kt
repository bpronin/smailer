package com.bopr.android.smailer.data

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.data.Database.Companion.COLUMN_BYPASS
import com.bopr.android.smailer.data.Database.Companion.COLUMN_END_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_IS_INCOMING
import com.bopr.android.smailer.data.Database.Companion.COLUMN_IS_MISSED
import com.bopr.android.smailer.data.Database.Companion.COLUMN_LATITUDE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_LONGITUDE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_PHONE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_PROCESS
import com.bopr.android.smailer.data.Database.Companion.COLUMN_PROCESS_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_READ
import com.bopr.android.smailer.data.Database.Companion.COLUMN_START_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_STATE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_TARGET
import com.bopr.android.smailer.data.Database.Companion.COLUMN_TEXT
import com.bopr.android.smailer.data.Database.Companion.COLUMN_TIMESTAMP
import com.bopr.android.smailer.data.Database.Companion.COLUMN_TYPE
import com.bopr.android.smailer.data.Database.Companion.TABLE_EVENTS
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_CALLS
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.EventPayload
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.battery.BatteryInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.GeoLocation.Companion.fromCoordinates
import com.bopr.android.smailer.util.stringArrayOf

class EventDataset(
    helper: SQLiteOpenHelper,
    modifications: MutableSet<String>
) :
    Dataset<Event>(TABLE_EVENTS, helper, modifications) {

    override val keyColumns = stringArrayOf(COLUMN_TIMESTAMP, COLUMN_TARGET)

    override fun keyOf(element: Event) = stringArrayOf(element.timestamp, element.target)

    /**
     * Returns count of unread events.
     */
    val unreadCount
        get() = read {
            count(tableName, "$COLUMN_READ<>1")
        }

    /**
     * Returns pending events.
     */
    val pending
        get() = read {
            queryRecords(
                table = tableName,
                selection = "$COLUMN_STATE=${STATE_PENDING}",
                order = "$COLUMN_TIMESTAMP DESC"
            )
        }.drainToSet(::get)

    /**
     * Marks all events as read.
     */
    fun markAllAsRead(read: Boolean) = write(tableName) {
        updateRecords(it, values {
            put(COLUMN_READ, read)
        })
    }

    override fun query() = read {
        queryRecords(tableName, order = "$COLUMN_TIMESTAMP DESC")
    }

    override fun get(cursor: Cursor) = cursor.run {
        val timestamp = getLong(COLUMN_TIMESTAMP)
        val target = getString(COLUMN_TARGET)
        Event(
            timestamp = timestamp,
            target = target,
            bypassFlags = Bits(getInt(COLUMN_BYPASS)),
            processFlags = Bits(getInt(COLUMN_PROCESS)),
            processState = getInt(COLUMN_STATE),
            processTime = getLongOrNull(COLUMN_PROCESS_TIME),
            isRead = getBoolean(COLUMN_READ),
            location = fromCoordinates(
                getDoubleOrNull(COLUMN_LATITUDE),
                getDoubleOrNull(COLUMN_LONGITUDE)
            ),
            payload = getPayload(
                getInt(COLUMN_TYPE),
                timestamp,
                target
            )
        )
    }

    override fun values(element: Event) = values {
        element.apply {
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_TARGET, target)
            put(COLUMN_BYPASS, bypassFlags.toInt())
            put(COLUMN_PROCESS, processFlags.toInt())
            put(COLUMN_STATE, processState)
            put(COLUMN_PROCESS_TIME, processTime)
            put(COLUMN_READ, isRead)
            location?.apply {
                put(COLUMN_LATITUDE, latitude)
                put(COLUMN_LONGITUDE, longitude)
            }

            putPayload(timestamp, target, payload).also {
                put(COLUMN_TYPE, it)
            }
        }
    }

    private fun getPayload(payloadType: Int, timestamp: Long, target: String): EventPayload {
        return when (payloadType) {
            PAYLOAD_TYPE_PHONE_CALL -> read {
                queryRecords(
                    table = TABLE_PHONE_CALLS,
                    selection = "$COLUMN_TIMESTAMP=$timestamp AND $COLUMN_TARGET='$target'"
                ).withFirst {
                    PhoneCallInfo(
                        phone = getString(COLUMN_PHONE),
                        isIncoming = getBoolean(COLUMN_IS_INCOMING),
                        startTime = getLong(COLUMN_START_TIME),
                        endTime = getLongOrNull(COLUMN_END_TIME),
                        isMissed = getBoolean(COLUMN_IS_MISSED),
                        text = getStringOrNull(COLUMN_TEXT)
                    )
                }
            }

            //PAYLOAD_BATTERY -> read {
            //TODO:
            // }

            else -> throw IllegalArgumentException("Unknown payload type")
        }
    }

    private fun putPayload(timestamp: Long, target: String, payload: EventPayload): Int {
        when (payload) {
            is PhoneCallInfo -> payload.apply {
                write(TABLE_PHONE_CALLS) {
                    replaceRecords(it, values {
                        put(COLUMN_TIMESTAMP, timestamp)
                        put(COLUMN_TARGET, target)
                        put(COLUMN_PHONE, phone)
                        put(COLUMN_START_TIME, startTime)
                        put(COLUMN_IS_INCOMING, isIncoming)
                        put(COLUMN_IS_MISSED, isMissed)
                        put(COLUMN_END_TIME, endTime)
                        put(COLUMN_TEXT, text)
                    })
                }

                return PAYLOAD_TYPE_PHONE_CALL
            }

            is BatteryInfo -> payload.apply {
//                write {
//                    // TODO: implement
//                }
                return PAYLOAD_TYPE_BATTERY
            }

            else -> throw IllegalArgumentException("Unknown payload type")
        }
    }

    companion object {

        private const val PAYLOAD_TYPE_PHONE_CALL = 0
        private const val PAYLOAD_TYPE_BATTERY = 1
    }
}