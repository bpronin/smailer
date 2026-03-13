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
import com.bopr.android.smailer.data.Database.Companion.TABLE_BATTERY
import com.bopr.android.smailer.data.Database.Companion.TABLE_EVENTS
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_CALLS
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.EventPayload
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.battery.BatteryData
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.GeoLocation.Companion.fromCoordinates
import com.bopr.android.smailer.util.stringArrayOf

class EventDataset(
    helper: SQLiteOpenHelper,
    modifications: MutableSet<String>
) : Dataset<Event>(TABLE_EVENTS, helper, modifications) {

    override val keyColumns = stringArrayOf(COLUMN_TIMESTAMP, COLUMN_TARGET)

    override fun keyOf(element: Event) = stringArrayOf(element.timestamp, element.target)

    /**
     * Returns count of unread events.
     */
    fun getUnreadCount() = read {
        count(tableName, "$COLUMN_READ<>1")
    }

    /**
     * Returns pending events.
     */
    fun drainPending() = read {
        queryRecords(
            table = tableName,
            where = "$COLUMN_STATE=${STATE_PENDING}",
            order = "$COLUMN_TIMESTAMP DESC"
        ).drainToSet(::get)
    }

    /**
     * Marks all events as read.
     */
    fun markAllAsRead(read: Boolean) = write {
        updateRecords(it, values {
            COLUMN_READ to read
        })
    }

    override fun query() = read {
        queryRecords(tableName, order = "$COLUMN_TIMESTAMP DESC")
    }

    override fun insert(element: Event) = write {
        batchUpdate {
            super.insert(element).also {
                insertPayload(element.timestamp, element.target, element.payload)
            }
        }
    }

    override fun delete(element: Event) = write {
        batchUpdate {
            super.delete(element).also {
                deletePayload(element.timestamp, element.target, element.payload)
            }
        }
    }

    override fun clear() = write {
        batchUpdate {
            super.clear().also {
                deleteRecords(TABLE_PHONE_CALLS)
                deleteRecords(TABLE_BATTERY)
            }
        }
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
            put(
                COLUMN_TYPE, when (payload) {
                    is PhoneCallData -> PAYLOAD_TYPE_PHONE_CALL
                    is BatteryData -> PAYLOAD_TYPE_BATTERY
                    else -> throw IllegalArgumentException("Unknown payload type")
                }
            )
        }
    }

    private fun getPayload(payloadType: Int, timestamp: Long, target: String): EventPayload {
        return when (payloadType) {
            PAYLOAD_TYPE_PHONE_CALL -> read {
                queryRecords(
                    table = TABLE_PHONE_CALLS,
                    where = "$COLUMN_TIMESTAMP=$timestamp AND $COLUMN_TARGET='$target'"
                ).withFirst {
                    PhoneCallData(
                        startTime = getLong(COLUMN_START_TIME),
                        phone = getString(COLUMN_PHONE),
                        isIncoming = getBoolean(COLUMN_IS_INCOMING),
                        endTime = getLongOrNull(COLUMN_END_TIME),
                        isMissed = getBoolean(COLUMN_IS_MISSED),
                        text = getStringOrNull(COLUMN_TEXT)
                    )
                }
            }

            PAYLOAD_TYPE_BATTERY -> read {
                queryRecords(
                    table = TABLE_BATTERY,
                    where = "$COLUMN_TIMESTAMP=$timestamp AND $COLUMN_TARGET='$target'"
                ).withFirst {
                    BatteryData(
                        text = getString(COLUMN_TEXT)
                    )
                }
            }

            else -> throw IllegalArgumentException("Unknown payload type")
        }
    }

    private fun insertPayload(timestamp: Long, target: String, payload: EventPayload) {
        when (payload) {
            is PhoneCallData -> write {
                insertRecord(TABLE_PHONE_CALLS, values {
                    payload.apply {
                        put(COLUMN_TIMESTAMP, timestamp)
                        put(COLUMN_TARGET, target)
                        put(COLUMN_PHONE, phone)
                        put(COLUMN_START_TIME, startTime)
                        put(COLUMN_IS_INCOMING, isIncoming)
                        put(COLUMN_IS_MISSED, isMissed)
                        put(COLUMN_END_TIME, endTime)
                        put(COLUMN_TEXT, text)
                    }
                })
            }

            is BatteryData -> write {
                insertRecord(TABLE_BATTERY, values {
                    payload.apply {
                        put(COLUMN_TIMESTAMP, timestamp)
                        put(COLUMN_TARGET, target)
                        put(COLUMN_TEXT, text)
                    }
                })
            }

            else -> throw IllegalArgumentException("Unknown payload type")
        }
    }

    private fun deletePayload(timestamp: Long, target: String, payload: EventPayload) {
        when (payload) {
            is PhoneCallData -> write {
                deleteRecords(
                    table = TABLE_PHONE_CALLS,
                    where = "$COLUMN_TIMESTAMP=$timestamp AND $COLUMN_TARGET='$target'"
                )
            }

            is BatteryData -> write {
                deleteRecords(
                    table = TABLE_BATTERY,
                    where = "$COLUMN_TIMESTAMP=$timestamp AND $COLUMN_TARGET='$target'"
                )
            }

            else -> throw IllegalArgumentException("Unknown payload type")
        }
    }

    companion object {

        private const val PAYLOAD_TYPE_PHONE_CALL = 0
        private const val PAYLOAD_TYPE_BATTERY = 1
    }
}