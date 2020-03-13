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
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
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
    private val modifiedTables = mutableSetOf<String>()

    init {
        log.debug("Open")
    }

    /**
     * Returns all events.
     */
    val events: PhoneEventRowSet
        get() = PhoneEventRowSet(helper.readableDatabase.query(
                table = TABLE_EVENTS,
                order = "$COLUMN_START_TIME DESC"
        ))

    /**
     * Returns pending events.
     */
    val pendingEvents: PhoneEventRowSet
        get() = PhoneEventRowSet(helper.readableDatabase.query(
                table = TABLE_EVENTS,
                selection = "$COLUMN_STATE=?",
                selectionArgs = strings(STATE_PENDING),
                order = "$COLUMN_START_TIME DESC"
        ))

    /**
     * Returns count of unread events.
     */
    val unreadEventsCount: Long
        get() = helper.readableDatabase.query(
                table = TABLE_EVENTS,
                projection = strings(COLUMN_COUNT),
                selection = "$COLUMN_READ<>1"
        ).useFirst {
            getLong(0)
        }!!

    /**
     * Returns last saved geolocation.
     */
    var lastLocation: GeoCoordinates?
        get() = helper.readableDatabase.query(
                table = TABLE_SYSTEM,
                projection = strings(COLUMN_LAST_LATITUDE, COLUMN_LAST_LONGITUDE),
                selection = "$COLUMN_ID=0"
        ).useFirst {
            GeoCoordinates(
                    getDouble(COLUMN_LAST_LATITUDE),
                    getDouble(COLUMN_LAST_LONGITUDE)
            )
        }
        set(value) {
            helper.writableDatabase.update(TABLE_SYSTEM, values {
                put(COLUMN_LAST_LATITUDE, value?.latitude)
                put(COLUMN_LAST_LONGITUDE, value?.longitude)
                put(COLUMN_LAST_LOCATION_TIME, currentTimeMillis())
            }, "$COLUMN_ID=0", null)
            modifiedTables.add(TABLE_SYSTEM)

            log.debug("Updated last location")
        }

    var phoneBlacklist: List<String>
        get() = getFilterList(TABLE_PHONE_BLACKLIST)
        set(value) = replaceFilterList(TABLE_PHONE_BLACKLIST, value)
    var phoneWhitelist: List<String>
        get() = getFilterList(TABLE_PHONE_WHITELIST)
        set(value) = replaceFilterList(TABLE_PHONE_WHITELIST, value)
    var textBlacklist: List<String>
        get() = getFilterList(TABLE_TEXT_BLACKLIST)
        set(value) = replaceFilterList(TABLE_TEXT_BLACKLIST, value)
    var textWhitelist: List<String>
        get() = getFilterList(TABLE_TEXT_WHITELIST)
        set(value) = replaceFilterList(TABLE_TEXT_WHITELIST, value)

    /**
     * Puts event to database.
     */
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

        helper.writableDatabase.run {
            if (insertWithOnConflict(TABLE_EVENTS, null, values, CONFLICT_IGNORE) == -1L) {
                update(TABLE_EVENTS, values, "$COLUMN_START_TIME=? AND $COLUMN_ACCEPTOR=?",
                        strings(event.startTime, event.acceptor))

                log.debug("Updated: $values")
            } else {
                log.debug("Inserted: $values")
            }
        }
        modifiedTables.add(TABLE_EVENTS)
    }

    /**
     * Puts specified events to database.
     */
    fun putEvents(events: Collection<PhoneEvent>) {
        helper.writableDatabase.batch {
            for (event in events) {
                putEvent(event)
            }
        }
    }

    /**
     * Removes specified events from database.
     */
    fun deleteEvents(events: Collection<PhoneEvent>) {
        helper.writableDatabase.batch {
            for (event in events) {
                delete(TABLE_EVENTS, "$COLUMN_ACCEPTOR=? AND $COLUMN_START_TIME=?",
                        strings(event.acceptor, event.startTime))
            }
        }
        modifiedTables.add(TABLE_EVENTS)

        log.debug("${events.size} event(s) removed")
    }

    /**
     * Removes all events from database.
     */
    fun clearEvents() {
        helper.writableDatabase.batch {
            delete(TABLE_EVENTS, null, null)
        }
        modifiedTables.add(TABLE_EVENTS)

        log.debug("Removed all from $TABLE_EVENTS")
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
        modifiedTables.add(TABLE_EVENTS)

        log.debug("All events marked as read")
    }

    fun getFilterList(listName: String): List<String> {
        return helper.readableDatabase.query(listName).useToList { getString(COLUMN_VALUE)!! }
    }

    fun replaceFilterList(listName: String, items: Collection<String>) {
        helper.writableDatabase.batch {
            delete(listName, null, null)
            log.debug("Removed all from $listName")

            for (item in items) {
                putFilterListItem(listName, item)
            }
        }
    }

    fun putFilterListItem(listName: String, item: String): Boolean {
        helper.writableDatabase.run {
            val values = values {
                put(COLUMN_VALUE, item)
            }
            return if (insertWithOnConflict(listName, null, values, CONFLICT_IGNORE) == -1L) {
                log.debug("Already exists: $values")
                false
            } else {
                log.debug("Inserted: $values")

                modifiedTables.add(listName)
                true
            }
        }
    }

    fun deleteFilterListItem(listName: String, item: String): Boolean {
        var affected = 0
        helper.writableDatabase.batch {
            affected = delete(listName, "$COLUMN_VALUE=?", strings(item))
        }
        modifiedTables.add(listName)
        return affected != 0
    }

    /**
     * Fires database "changed" event.
     */
    fun notifyChanged() {
        if (modifiedTables.isNotEmpty()) {
            log.debug("Broadcasting data changed: $modifiedTables")

            val intent = Intent(DATABASE_EVENT).putExtra(EXTRA_TABLES, modifiedTables.toTypedArray())
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            modifiedTables.clear()
        }
    }

    /**
     * Performs given action then fires "changed" event
     */
    inline fun <T> notifyOf(action: Database.() -> T): T {
        val result = action.invoke(this)
        notifyChanged()
        return result
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
    fun clean() {
        context.deleteDatabase(name)

        log.debug("Destroyed")
    }

    private inner class DbHelper(context: Context) : SQLiteOpenHelper(context, name, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.batch {
                execSQL(SQL_CREATE_SYSTEM)
                execSQL(SQL_CREATE_EVENTS)
                execSQL(SQL_CREATE_LIST(TABLE_PHONE_BLACKLIST))
                execSQL(SQL_CREATE_LIST(TABLE_PHONE_WHITELIST))
                execSQL(SQL_CREATE_LIST(TABLE_TEXT_BLACKLIST))
                execSQL(SQL_CREATE_LIST(TABLE_TEXT_WHITELIST))

                insert(TABLE_SYSTEM, null, values { put(COLUMN_ID, 0) })

                log.debug("Created")
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { /* see https://www.techonthenet.com/sqlite/tables/alter_table.php */
            if (DB_VERSION > oldVersion) {
                db.batch {
                    alterTable(TABLE_SYSTEM, SQL_CREATE_SYSTEM)
                    alterTable(TABLE_EVENTS, SQL_CREATE_EVENTS)
                    alterTable(TABLE_PHONE_BLACKLIST, SQL_CREATE_LIST(TABLE_PHONE_BLACKLIST))
                    alterTable(TABLE_PHONE_WHITELIST, SQL_CREATE_LIST(TABLE_PHONE_WHITELIST))
                    alterTable(TABLE_TEXT_BLACKLIST, SQL_CREATE_LIST(TABLE_TEXT_BLACKLIST))
                    alterTable(TABLE_TEXT_WHITELIST, SQL_CREATE_LIST(TABLE_TEXT_WHITELIST))
                }

                log.warn("Database upgraded from $oldVersion to: $DB_VERSION")
            }
        }
    }

    /**
     * Phone events row set.
     */
    inner class PhoneEventRowSet(cursor: Cursor) : RowSet<PhoneEvent>(cursor) {

        override fun Cursor.get(): PhoneEvent {
            return PhoneEvent(
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
        const val COLUMN_VALUE = "value"
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

        private const val DB_VERSION = 7
        private const val DATABASE_EVENT = "database-event"
        private const val EXTRA_TABLES = "tables"

        private const val TABLE_SYSTEM = "system_data"
        const val TABLE_EVENTS = "phone_events"
        const val TABLE_PHONE_BLACKLIST = "phone_blacklist"
        const val TABLE_PHONE_WHITELIST = "phone_whitelist"
        const val TABLE_TEXT_BLACKLIST = "text_blacklist"
        const val TABLE_TEXT_WHITELIST = "text_whitelist"

        private const val SQL_CREATE_SYSTEM = "CREATE TABLE " + TABLE_SYSTEM + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
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

        private fun SQL_CREATE_LIST(tableName: String) = "CREATE TABLE " + tableName + " (" +
                COLUMN_VALUE + " TEXT(256) NOT NULL PRIMARY KEY" +
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
        fun Context.registerDatabaseListener(onChange: (Set<String>) -> Unit): BroadcastReceiver {
            val listener = object : BroadcastReceiver() {

                override fun onReceive(context: Context?, intent: Intent?) {
                    onChange(intent!!.getStringArrayExtra(EXTRA_TABLES)!!.toSet())
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