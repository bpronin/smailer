package com.bopr.android.smailer

import android.content.*
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
import java.util.concurrent.TimeUnit

/**
 * Application database.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Database constructor(private val context: Context, private val name: String = DATABASE_NAME) : Closeable {

    private var updatesCounter: Long = 0
    private val helper: DbHelper = DbHelper(context)

    init {
        log.debug("Open")
    }

    /**
     * Time period that should exceed until addition of new record triggers purge process
     * that removes stale records.
     */
    var purgePeriod: Long = TimeUnit.DAYS.toMillis(7)

    /**
     * Maximum amount of records that the log may contain.
     * All records that exceeds this value will be removed.
     */
    var capacity: Long = 10000

    val events: PhoneEventRowSet
        get() = PhoneEventRowSet(query(
                table = TABLE_EVENTS,
                order = "$COLUMN_START_TIME DESC"
        ))

    val pendingEvents: PhoneEventRowSet
        get() = PhoneEventRowSet(query(
                table = TABLE_EVENTS,
                selection = "$COLUMN_STATE=?",
                args = strings(STATE_PENDING),
                order = "$COLUMN_START_TIME DESC"
        ))

    val unreadEventsCount: Long
        get() = query(
                table = TABLE_EVENTS,
                columns = strings(COLUMN_COUNT),
                selection = "$COLUMN_READ<>1"
        ).useFirst {
            it.getLong(0)
        }!!

    /**
     * Returns last saved geolocation.
     *
     * @return location
     */
    val lastLocation: GeoCoordinates?
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

    fun putEvent(event: PhoneEvent) {
        putEvent(event, helper.writableDatabase)
    }

    fun putEvents(events: Collection<PhoneEvent>) {
        helper.writableDatabase.batch {
            for (event in events) {
                putEvent(event)
            }
        }
    }

    private fun putEvent(event: PhoneEvent, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COLUMN_PHONE, event.phone)
            put(COLUMN_RECIPIENT, event.acceptor)
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

        if (db.insertWithOnConflict(TABLE_EVENTS, null, values, CONFLICT_IGNORE) == -1L) {
            db.update(TABLE_EVENTS, values, "$COLUMN_START_TIME=? AND $COLUMN_RECIPIENT=?",
                    strings(event.startTime, event.acceptor))

            log.debug("Updated: $values")
        } else {
            log.debug("Inserted: $values")
        }

        updatesCounter++
    }

    /**
     * Removes all records from log.
     */
    fun clearEvents() {
        helper.writableDatabase.batch {
            delete(TABLE_EVENTS, null, null)
            updatesCounter++
        }

        log.debug("All events removed")
    }

    /**
     * Marks all events as read.
     */
    fun markAllAsRead(read: Boolean) {
        helper.writableDatabase.batch {
            val values = ContentValues().apply {
                put(COLUMN_READ, read)
            }

            update(TABLE_EVENTS, values, null, null)

            updatesCounter++
        }

        log.debug("All events marked as read")
    }

    /**
     * Saves geolocation.
     *
     * @param coordinates location
     */
    fun putLastLocation(coordinates: GeoCoordinates) {
        val values = ContentValues().apply {
            put(COLUMN_LAST_LATITUDE, coordinates.latitude)
            put(COLUMN_LAST_LONGITUDE, coordinates.longitude)
            put(COLUMN_LAST_LOCATION_TIME, currentTimeMillis())
        }

        helper.writableDatabase.update(TABLE_SYSTEM, values, "$COLUMN_ID=0", null)
    }

    /**
     * Fires database changed event.
     */
    fun notifyChanged() {
        if (updatesCounter > 0) {
            log.debug("Broadcasting data changed")

            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(DATABASE_EVENT))
            updatesCounter = 0
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
     * Physically deletes database.
     */
    fun destroy() {
        context.deleteDatabase(name)

        log.debug("Destroyed")
    }

    /**
     * Removes all stale records that exceeds specified capacity if given
     * period of time has elapsed.
     */
/*
    fun purge() {
        log.debug("Purging")

        helper.writableDatabase.batch {
            if (currentTimeMillis() - lastPurgeTime(this) >= purgePeriod && currentSize(this) >= capacity) {
                execSQL("DELETE FROM " + TABLE_EVENTS +
                        " WHERE " + COLUMN_ID + " NOT IN " +
                        "(" +
                        "SELECT " + COLUMN_ID + " FROM " + TABLE_EVENTS +
                        " ORDER BY " + COLUMN_ID + " DESC " +
                        "LIMIT " + capacity +
                        ")")
                updateLastPurgeTime(this)
            }
        }
    }

    private fun currentSize(db: SQLiteDatabase): Long {
        return forLong(db.query(TABLE_EVENTS, strings(COLUMN_COUNT),
                null, null, null, null, null))!!
    }

    private fun lastPurgeTime(db: SQLiteDatabase): Long {
        return forLong(db.query(TABLE_SYSTEM, strings(COLUMN_PURGE_TIME),
                "$COLUMN_ID=0", null, null, null, null))!!
    }

    private fun updateLastPurgeTime(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put(COLUMN_PURGE_TIME, currentTimeMillis())
        db.update(TABLE_SYSTEM, values, "$COLUMN_ID=0", null)
    }
*/

    private fun query(table: String, columns: Array<String>? = null, selection: String? = null,
                      args: Array<String>? = null, groupBy: String? = null,
                      having: String? = null, order: String? = null): Cursor {
        return helper.readableDatabase.query(table, columns, selection, args,
                groupBy, having, order)
    }

    private fun strings(vararg values: Any): Array<String> {
        return Array(values.size) { values[it].toString() }
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
        const val COLUMN_RECIPIENT = "recipient"
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
                COLUMN_RECIPIENT + " TEXT(25) NOT NULL," +
                COLUMN_TEXT + " TEXT(256)," +
                COLUMN_STATE + " INTEGER, " +
                COLUMN_PROCESS_STATUS + " INTEGER, " +
                COLUMN_PROCESS_TIME + " INTEGER, " +
                COLUMN_READ + " INTEGER NOT NULL DEFAULT(0), " +
                COLUMN_DETAILS + " TEXT(256), " +
                "PRIMARY KEY (" + COLUMN_START_TIME + ", " + COLUMN_RECIPIENT + ")" +
                ")"

        fun registerDatabaseListener(context: Context, onChange: () -> Unit): BroadcastReceiver {
            val listener = object : BroadcastReceiver() {

                override fun onReceive(context: Context?, intent: Intent?) {
                    onChange()
                }
            }
            LocalBroadcastManager.getInstance(context).registerReceiver(listener, IntentFilter(DATABASE_EVENT))

            log.debug("Listener registered")

            return listener
        }

        fun unregisterDatabaseListener(context: Context, listener: BroadcastReceiver) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(listener)

            log.debug("Listener unregistered")
        }
    }

    private inner class DbHelper(context: Context) : SQLiteOpenHelper(context, name, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_SYSTEM)
            db.execSQL(SQL_CREATE_EVENTS)

            val values = ContentValues()
            values.put(COLUMN_ID, 0)
            db.insert(TABLE_SYSTEM, null, values)

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
                COLUMN_RECIPIENT -> {
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
                        acceptor = getString(COLUMN_RECIPIENT)!!,
                        processStatus = getInt(COLUMN_PROCESS_STATUS),
                        processTime = getLong(COLUMN_PROCESS_TIME),
                        isRead = getBoolean(COLUMN_READ)
                )
            }
        }
    }

}