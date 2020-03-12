package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.util.*
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.System.currentTimeMillis

/**
 * Application database.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Database constructor(private val context: Context, private val name: String = DATABASE_NAME) : Closeable {

    private val helper: DbHelper = DbHelper(context)
    private var modified = false

    init {
        log.debug("Open")
    }

    /**
     * Returns all events.
     */
    val events: PhoneEventRowSet
        get() = PhoneEventRowSet(query(
                table = TABLE_EVENTS,
                order = "$COLUMN_START_TIME DESC"
        ))

    /**
     * Returns pending events.
     */
    val pendingEvents: PhoneEventRowSet
        get() = PhoneEventRowSet(query(
                table = TABLE_EVENTS,
                selection = "$COLUMN_STATE=?",
                selectionArgs = strings(STATE_PENDING),
                order = "$COLUMN_START_TIME DESC"
        ))

    /**
     * Returns count of unread events.
     */
    val unreadEventsCount: Long
        get() = query(
                table = TABLE_EVENTS,
                projection = strings(COLUMN_COUNT),
                selection = "$COLUMN_READ<>1"
        ).useFirst {
            it.getLong(0)
        }!!

    /**
     * Returns last saved geolocation.
     */
    var lastLocation: GeoCoordinates?
        get() = query(
                table = TABLE_SYSTEM,
                columns = strings(COLUMN_LAST_LATITUDE, COLUMN_LAST_LONGITUDE),
                selection = "$COLUMN_ID=0"
        ).useFirst {
            GeoCoordinates(
                    it.getDouble(COLUMN_LAST_LATITUDE),
                    it.getDouble(COLUMN_LAST_LONGITUDE)
            )
        }
        set(value) {
            helper.writableDatabase.update(TABLE_SYSTEM, values {
                put(COLUMN_LAST_LATITUDE, value?.latitude)
                put(COLUMN_LAST_LONGITUDE, value?.longitude)
                put(COLUMN_LAST_LOCATION_TIME, currentTimeMillis())
            }, "$COLUMN_ID=0", null)
            modified = true

            log.debug("Updated last location")
        }

    fun putEvent(event: PhoneEvent) {
        val values = values {
            put(COLUMN_PHONE, event.phone)
            put(COLUMN_ACCEPTOR, event.acceptor)
            put(COLUMN_START_TIME, event.startTime)
            put(COLUMN_STATE, event.state)
            put(COLUMN_PROCESS_STATUS, event.processStatus)
            put(COLUMN_PROCESS_TIME, event.processTime)
            put(COLUMN_IS_INCOMING, event.isIncoming)
            put(COLUMN_IS_MISSED, event.isMissed)
            put(COLUMN_END_TIME, event.endTime)
            put(COLUMN_TEXT, event.text)
            put(COLUMN_DETAILS, event.details)
            put(COLUMN_READ, event.isRead)
            event.location?.let {
                put(COLUMN_LATITUDE, it.latitude)
                put(COLUMN_LONGITUDE, it.longitude)
            }
        }
        if (helper.writableDatabase.insertWithOnConflict(TABLE_EVENTS, null, values, CONFLICT_IGNORE) == -1L) {
            helper.writableDatabase.update(TABLE_EVENTS, values, "$COLUMN_START_TIME=? AND $COLUMN_ACCEPTOR=?",
                    strings(event.startTime, event.acceptor))

                log.debug("Updated: $values")
            } else {
                log.debug("Inserted: $values")
            }
        }
        modified = true
    }

    fun putEvents(events: Collection<PhoneEvent>) {
        helper.writableDatabase.batch {
            for (event in events) {
                putEvent(event)
            }
        }
    }

    /**
     * Removes records from log.
     */
    fun deleteEvents(events: Collection<PhoneEvent>) {
        helper.writableDatabase.batch {
            for (event in events) {
                delete(TABLE_EVENTS, "$COLUMN_ACCEPTOR=? AND $COLUMN_START_TIME=?",
                        strings(event.acceptor, event.startTime))
            }
        }
        modified = true

        log.debug("${events.size} event(s) removed")
    }

    /**
     * Removes all events from database.
     */
    fun clearEvents() {
        helper.writableDatabase.batch {
            delete(TABLE_EVENTS, null, null)
        }
        modified = true

        log.debug("All events removed")
    }

    /**
     * Marks all events as read.
     */
    fun markAllEventsAsRead(read: Boolean) {
        helper.writableDatabase.batch {
            update(TABLE_EVENTS, values {
                put(COLUMN_READ, read)
            }, null, null)
        }
        modified = true

        log.debug("All events marked as read")
    }

    /**
     * Fires database changed event.
     */
    fun notifyChanged() {
        if (modified) {
            log.debug("Broadcasting data changed")

            modified = false
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(DATABASE_EVENT))
        }
    }

    /**
     * Close any open database object.
     */
    override fun close() {
        helper.close()

        log.debug("Closed")
    }

    /**
     * Physically deletes database file.
     */
    fun destroy() {
        context.deleteDatabase(name)

        log.debug("Destroyed")
    }

    private fun query(table: String, projection: Array<String>? = null, selection: String? = null,
                      selectionArgs: Array<String>? = null, groupBy: String? = null,
                      having: String? = null, order: String? = null): Cursor {
        return helper.readableDatabase.query(table, projection, selection, selectionArgs,
                groupBy, having, order)
    }

    private inner class DbHelper(context: Context) : SQLiteOpenHelper(context, name, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_SYSTEM)
            db.execSQL(SQL_CREATE_EVENTS)

            db.insert(TABLE_SYSTEM, null, values {
                put(COLUMN_ID, 0)
            })

            log.debug("Created")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { /* see https://www.techonthenet.com/sqlite/tables/alter_table.php */
            if (oldVersion < DB_VERSION) {
                log.warn("Database upgrade from $oldVersion to: $DB_VERSION")

                db.replaceTable(TABLE_EVENTS, SQL_CREATE_EVENTS, ::convertEventsColumns)
            }

            log.debug("Upgraded")
        }

        private fun convertEventsColumns(column: String, cursor: Cursor): String? {
            val value = cursor.getString(column)

            when (column) {
                COLUMN_STATE -> {
                    when (value) {
                        "PENDING" ->
                            return STATE_PENDING.toString()
                        "IGNORED" ->
                            return STATE_IGNORED.toString()
                        "PROCESSED" ->
                            return STATE_PROCESSED.toString()
                    }
                }
                COLUMN_ACCEPTOR -> {
                    return value ?: deviceName()
                }
            }

            return value
        }
    }

    /**
     * Phone events row set.
     */
    class PhoneEventRowSet(cursor: Cursor) : RowSet<PhoneEvent>(cursor) {

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
    }

    companion object {

        private val log = LoggerFactory.getLogger("Database")

        const val DATABASE_NAME = "smailer.sqlite"
        const val COLUMN_COUNT = "COUNT(*)"
        const val COLUMN_ID = "_id"
        const val COLUMN_IS_INCOMING = "is_incoming"
        const val COLUMN_IS_MISSED = "is_missed"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_TEXT = "message_text"
        const val COLUMN_DETAILS = "details"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_STATE = "state"
        const val COLUMN_PROCESS_STATUS = "state_reason"
        const val COLUMN_PROCESS_TIME = "process_time"
        const val COLUMN_LAST_LATITUDE = "last_latitude"
        const val COLUMN_LAST_LONGITUDE = "last_longitude"
        const val COLUMN_LAST_LOCATION_TIME = "last_location_time"
        const val COLUMN_READ = "message_read"
        const val COLUMN_ACCEPTOR = "recipient"
        private const val COLUMN_PURGE_TIME = "messages_purge_time"

        private const val DB_VERSION = 7
        private const val DATABASE_EVENT = "database-event"
        private const val TABLE_SYSTEM = "system_data"
        private const val TABLE_EVENTS = "phone_events"

        private const val SQL_CREATE_SYSTEM = "CREATE TABLE " + TABLE_SYSTEM + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_PURGE_TIME + " INTEGER," +
                COLUMN_LAST_LATITUDE + " REAL," +
                COLUMN_LAST_LONGITUDE + " REAL," +
                COLUMN_LAST_LOCATION_TIME + " INTEGER" +
                ")"

        private const val SQL_CREATE_EVENTS = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_PHONE + " TEXT(25) NOT NULL," +
                COLUMN_IS_INCOMING + " INTEGER, " +
                COLUMN_IS_MISSED + " INTEGER, " +
                COLUMN_START_TIME + " INTEGER NOT NULL, " +
                COLUMN_END_TIME + " INTEGER, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_ACCEPTOR + " TEXT(25) NOT NULL," +
                COLUMN_TEXT + " TEXT(256)," +
                COLUMN_STATE + " INTEGER, " +
                COLUMN_PROCESS_STATUS + " INTEGER, " +
                COLUMN_PROCESS_TIME + " INTEGER, " +
                COLUMN_READ + " INTEGER NOT NULL DEFAULT(0), " +
                COLUMN_DETAILS + " TEXT(256), " +
                "PRIMARY KEY (" + COLUMN_START_TIME + ", " + COLUMN_ACCEPTOR + ")" +
                ")"

        /**
         * Registers database broadcast receiver.
         */
        fun Context.registerDatabaseListener(listener: BroadcastReceiver) {
            LocalBroadcastManager.getInstance(this).registerReceiver(listener, IntentFilter(DATABASE_EVENT))

            log.debug("Listener registered")
        }

        /**
         * Creates and registers database broadcast receiver.
         */
        fun Context.registerDatabaseListener(onChange: () -> Unit): BroadcastReceiver {
            val listener = object : BroadcastReceiver() {

                override fun onReceive(context: Context?, intent: Intent?) {
                    onChange()
                }
            }
            registerDatabaseListener(listener)
            return listener
        }

        /**
         * Unregisters database broadcast receiver.
         */
        fun Context.unregisterDatabaseListener(listener: BroadcastReceiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(listener)

            log.debug("Listener unregistered")
        }
    }

}