package com.bopr.android.smailer

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bopr.android.smailer.sync.SyncWorker.Companion.requestDataSync
import com.bopr.android.smailer.util.database.*
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.System.currentTimeMillis

/**
 * Application database.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Database(private val context: Context) : Closeable {

    private val helper: DbHelper = DbHelper(context)
    private val modifiedTables = mutableSetOf<String>()

    /**
     * Returns all phone events.
     */
    val events = EventsDataset(helper, modifiedTables)

    /**
     * Phone numbers blacklist.
     */
    val phoneBlacklist = StringDataset(TABLE_PHONE_BLACKLIST, helper, modifiedTables)

    /**
     * Phone numbers whitelist.
     */
    val phoneWhitelist = StringDataset(TABLE_PHONE_WHITELIST, helper, modifiedTables)

    /**
     * SMS text blacklist.
     */
    val textBlacklist = StringDataset(TABLE_TEXT_BLACKLIST, helper, modifiedTables)

    /**
     * SMS text whitelist.
     */
    val textWhitelist = StringDataset(TABLE_TEXT_WHITELIST, helper, modifiedTables)

    val eventFilterList = mapOf(
            TABLE_PHONE_BLACKLIST to phoneBlacklist,
            TABLE_PHONE_WHITELIST to phoneWhitelist,
            TABLE_TEXT_BLACKLIST to textBlacklist,
            TABLE_TEXT_WHITELIST to textWhitelist
    )

    /**
     * Returns last saved geolocation.
     */
    var lastLocation: GeoCoordinates?
        get() = querySystemTable(COLUMN_LAST_LATITUDE, COLUMN_LAST_LONGITUDE).useFirst {
            GeoCoordinates(
                    getDouble(COLUMN_LAST_LATITUDE),
                    getDouble(COLUMN_LAST_LONGITUDE)
            )
        }
        set(value) = updateSystemTable(values {
            put(COLUMN_LAST_LATITUDE, value?.latitude)
            put(COLUMN_LAST_LONGITUDE, value?.longitude)
            put(COLUMN_LAST_LOCATION_TIME, currentTimeMillis())
        }).also {
            log.debug("Updated last location to: {}", value)
        }

    /**
     * Returns last database modification time.
     * @see [commit]
     */
    var updateTime: Long
        get() = querySystemTable(COLUMN_UPDATE_TIME).useFirst {
            getLong(COLUMN_UPDATE_TIME)
        }
        internal set(value) = updateSystemTable(values {
            put(COLUMN_UPDATE_TIME, value)
        }).also {
            log.debug("Update time: %tF %tT".format(value, value))
        }

    /**
     * Performs write transaction. Rollback it when failed.
     */
    fun batch(action: Database.() -> Unit) {
        log.debug("Begin transaction")
        helper.writableDatabase.batch { this@Database.action() }
        log.debug("End transaction")
    }

    /**
     * Performs specified action then updates modification time, fires change event
     * and requests google drive synchronization.
     */
    fun <T> commit(syncRequired: Boolean = true, action: Database.() -> T): T {
        val result = action(this)
        if (modifiedTables.isNotEmpty()) {
            updateTime = currentTimeMillis()
            context.sendDatabaseBroadcast(modifiedTables)
            modifiedTables.clear()
            if (syncRequired) {
                context.requestDataSync()
            }
        }
        return result
    }

    /**
     * Closes open database object.
     */
    override fun close() {
        helper.close()

        log.debug("Closed")
    }

    private fun querySystemTable(vararg columns: String): Cursor {
        return helper.readableDatabase.query(TABLE_SYSTEM, columns, "$COLUMN_ID=0")
    }

    private fun updateSystemTable(values: ContentValues) {
        helper.writableDatabase.update(TABLE_SYSTEM, values, "$COLUMN_ID=0")
    }

    inner class DbHelper(context: Context) : SQLiteOpenHelper(context, databaseName, null, DB_VERSION) {

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

    companion object {

        private val log = LoggerFactory.getLogger("Database")

        var databaseName = "smailer.sqlite"

        private const val DB_VERSION = 9

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
        const val COLUMN_UPDATE_TIME = "last_update_time"
        const val COLUMN_READ = "message_read"
        const val COLUMN_ACCEPTOR = "recipient"

        private const val ACTION_DATABASE_CHANGED = "database_changed"
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
                COLUMN_LAST_LOCATION_TIME + " INTEGER," +
                COLUMN_UPDATE_TIME + " INTEGER" +
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

        @Suppress("FunctionName")
        private fun SQL_CREATE_LIST(tableName: String) = "CREATE TABLE " + tableName + " (" +
                COLUMN_VALUE + " TEXT(256) NOT NULL PRIMARY KEY" +
                ")"

        /**
         * Sends database broadcast.
         */
        fun Context.sendDatabaseBroadcast(tables: Set<String>) {
            log.debug("Broadcasting data changed: {}", tables)

            val intent = Intent(ACTION_DATABASE_CHANGED)
                    .putExtra(EXTRA_TABLES, tables.toTypedArray())
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        /**
         * Creates and registers database broadcast receiver.
         */
        fun Context.registerDatabaseListener(onChange: (Set<String>) -> Unit)
                : BroadcastReceiver {
            val listener = object : BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    onChange(intent.getStringArrayExtra(EXTRA_TABLES)?.toSet() ?: emptySet())
                }
            }
            LocalBroadcastManager.getInstance(this).registerReceiver(listener,
                    IntentFilter(ACTION_DATABASE_CHANGED))

            log.debug("Listener registered: [${listener.hashCode()}]")
            return listener
        }

        /**
         * Unregisters database broadcast receiver.
         */
        fun Context.unregisterDatabaseListener(listener: BroadcastReceiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(listener)

            log.debug("Listener unregistered: [${listener.hashCode()}]")
        }
    }

}