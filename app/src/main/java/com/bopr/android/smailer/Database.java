package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bopr.android.smailer.util.db.FieldDataConverter;
import com.bopr.android.smailer.util.db.RowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static com.bopr.android.smailer.PhoneEvent.STATE_IGNORED;
import static com.bopr.android.smailer.PhoneEvent.STATE_PENDING;
import static com.bopr.android.smailer.PhoneEvent.STATE_PROCESSED;
import static com.bopr.android.smailer.util.AndroidUtil.deviceName;
import static com.bopr.android.smailer.util.Util.requireNonNull;
import static com.bopr.android.smailer.util.db.DbUtil.replaceTable;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;

/**
 * Application database.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Database {

    private static Logger log = LoggerFactory.getLogger("Database");

    public static final String DATABASE_NAME = "smailer.sqlite";
    private static final int DB_VERSION = 5;

    private static final String DATABASE_EVENT = "database-event";

    private static final String TABLE_SYSTEM = "system_data";
    private static final String TABLE_EVENTS = "phone_events";

    public static final String COLUMN_COUNT = "COUNT(*)";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PURGE_TIME = "messages_purge_time";
    public static final String COLUMN_IS_INCOMING = "is_incoming";
    public static final String COLUMN_IS_MISSED = "is_missed";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TEXT = "message_text";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_STATE_REASON = "state_reason";
    public static final String COLUMN_LAST_LATITUDE = "last_latitude";
    public static final String COLUMN_LAST_LONGITUDE = "last_longitude";
    public static final String COLUMN_LAST_LOCATION_TIME = "last_location_time";
    public static final String COLUMN_READ = "message_read";
    public static final String COLUMN_RECIPIENT = "recipient";

    private final String name;
    private long purgePeriod = TimeUnit.DAYS.toMillis(7);
    private final DbHelper helper;
    private final Context context;
    private int capacity = 10000;
    private long updatesCounter;

    public Database(Context context) {
        this(context, DATABASE_NAME);
    }

    public Database(Context context, String name) {
        this.context = context;
        this.name = name;
        helper = new DbHelper(context);
    }

    /**
     * Returns maximum amount of records that the log may contain.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Set maximum amount of records that the log may contain.
     * All records that exceeds this value will be removed.
     *
     * @param capacity maximum amount of records
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns time period that should exceed until addition of new record triggers purge process
     * that removes stale records.
     */
    public long getPurgePeriod() {
        return purgePeriod;
    }

    /**
     * Sets time period that should exceed until addition of new record triggers purge process
     * that removes stale records.
     */
    public void setPurgePeriod(long purgePeriod) {
        this.purgePeriod = purgePeriod;
    }

    public PhoneEventRowSet getEvents() {
        return new PhoneEventRowSet(helper.getReadableDatabase().query(TABLE_EVENTS, null,
                null, null, null, null,
                COLUMN_START_TIME + " DESC")
        );
    }

    public PhoneEventRowSet getPendingEvents() {
        return new PhoneEventRowSet(helper.getReadableDatabase().query(TABLE_EVENTS, null,
                COLUMN_STATE + "=?", strings(STATE_PENDING), null, null,
                COLUMN_START_TIME + " DESC")
        );
    }

    public long getUnreadEventsCount() {
        return RowSet.forLong(helper.getReadableDatabase().query(TABLE_EVENTS, strings(COLUMN_COUNT),
                COLUMN_READ + "<>1", null, null, null, null));
    }

    public void putEvent(PhoneEvent event) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_STATE, event.getState());
        values.put(COLUMN_STATE_REASON, event.getStateReason());
        values.put(COLUMN_IS_INCOMING, event.isIncoming());
        values.put(COLUMN_IS_MISSED, event.isMissed());
        values.put(COLUMN_PHONE, event.getPhone());
        values.put(COLUMN_START_TIME, event.getStartTime());
        values.put(COLUMN_END_TIME, event.getEndTime());
        values.put(COLUMN_TEXT, event.getText());
        values.put(COLUMN_DETAILS, event.getDetails());
        values.put(COLUMN_READ, event.isRead());
        values.put(COLUMN_RECIPIENT, event.getRecipient());
        GeoCoordinates location = event.getLocation();
        if (location != null) {
            values.put(COLUMN_LATITUDE, location.getLatitude());
            values.put(COLUMN_LONGITUDE, location.getLongitude());
        }

        if (db.insertWithOnConflict(TABLE_EVENTS, null, values, CONFLICT_IGNORE) == -1) {
            db.update(TABLE_EVENTS, values, COLUMN_START_TIME + "=? AND " + COLUMN_RECIPIENT + "=?",
                    strings(event.getStartTime(), event.getRecipient()));
            log.debug("Updated: " + values);
        } else {
            log.debug("Inserted: " + values);
        }

        updatesCounter++;
    }

    /**
     * Removes all records from log.
     */
    public void clearEvents() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_EVENTS, null, null);
            updateLastPurgeTime(db);
            updatesCounter++;

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        log.debug("All events removed");
    }

    /**
     * Returns last saved geolocation.
     *
     * @return location
     */
    public GeoCoordinates getLastLocation() {
        return new GeoCoordinatesRowSet(helper.getReadableDatabase().query(TABLE_SYSTEM,
                strings(COLUMN_LAST_LATITUDE, COLUMN_LAST_LONGITUDE), COLUMN_ID + "=0",
                null, null, null, null)).findFirst();
    }

    /**
     * Saves geolocation.
     *
     * @param location location
     */
    public void saveLastLocation(GeoCoordinates location) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LATITUDE, location != null ? location.getLatitude() : null);
        values.put(COLUMN_LAST_LONGITUDE, location != null ? location.getLongitude() : null);
        values.put(COLUMN_LAST_LOCATION_TIME, currentTimeMillis());

        helper.getWritableDatabase().update(TABLE_SYSTEM, values, COLUMN_ID + "=0", null);
    }

    /**
     * Removes all stale records that exceeds specified capacity if given
     * period of time has elapsed.
     */
    public void purge() {
        log.debug("Purging");

        SQLiteDatabase db = helper.getWritableDatabase();
        if (currentTimeMillis() - getLastPurgeTime(db) >= purgePeriod && getCurrentSize(db) >= capacity) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM " + TABLE_EVENTS +
                        " WHERE " + COLUMN_ID + " NOT IN " +
                        "(" +
                        "SELECT " + COLUMN_ID + " FROM " + TABLE_EVENTS +
                        " ORDER BY " + COLUMN_ID + " DESC " +
                        "LIMIT " + capacity +
                        ")");

                updateLastPurgeTime(db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    /**
     * Close any open database object.
     */
    public void close() {
        helper.close();
        log.debug("Closed");
    }

    /**
     * Physically deletes database.
     */
    public void destroy() {
        context.deleteDatabase(name);
    }


    /* Content provider support */
    public Cursor query(String table, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        return helper.getReadableDatabase().query(table, projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    public long put(String table, ContentValues values) {
        return helper.getWritableDatabase().replace(table, null, values);
    }

    public int delete(String table, String selection, String[] selectionArgs) {
        return helper.getWritableDatabase().delete(table, selection, selectionArgs);
    }

    public int update(String table, ContentValues values, String selection, String[] selectionArgs) {
        return helper.getWritableDatabase().update(table, values, selection, selectionArgs);
    }

    public BroadcastReceiver registerListener(@NonNull BroadcastReceiver listener) {
        IntentFilter filter = new IntentFilter(DATABASE_EVENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(requireNonNull(listener), filter);
        return listener;
    }

    public void unregisterListener(@NonNull BroadcastReceiver listener) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(listener);
    }

    public void notifyChanged() {
        if (updatesCounter > 0) {
            log.debug("Broadcasting data changed");

            Intent intent = new Intent(DATABASE_EVENT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            updatesCounter = 0;
        }
    }

    private long getCurrentSize(SQLiteDatabase db) {
        return RowSet.forLong(db.query(TABLE_EVENTS, strings(COLUMN_COUNT), null, null,
                null, null, null));
    }

    private long getLastPurgeTime(SQLiteDatabase db) {
        return RowSet.forLong(db.query(TABLE_SYSTEM, strings(COLUMN_PURGE_TIME),
                COLUMN_ID + "=0", null, null, null, null));
    }

    private void updateLastPurgeTime(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PURGE_TIME, currentTimeMillis());

        db.update(TABLE_SYSTEM, values, COLUMN_ID + "=0", null);
    }

    private String[] strings(Object... values) {
        String[] strings = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strings[i] = valueOf(values[i]);
        }
        return strings;
    }

    private class DbHelper extends SQLiteOpenHelper {

        private static final String EVENTS_TABLE_SQL =
                "CREATE TABLE " + TABLE_EVENTS + " (" +
                        COLUMN_STATE + " INTEGER, " +
                        COLUMN_STATE_REASON + " INTEGER, " +
                        COLUMN_IS_INCOMING + " INTEGER, " +
                        COLUMN_IS_MISSED + " INTEGER, " +
                        COLUMN_START_TIME + " INTEGER NOT NULL, " +
                        COLUMN_END_TIME + " INTEGER, " +
                        COLUMN_LATITUDE + " REAL, " +
                        COLUMN_LONGITUDE + " REAL, " +
                        COLUMN_PHONE + " TEXT(25) NOT NULL," +
                        COLUMN_RECIPIENT + " TEXT(25) NOT NULL," +
                        COLUMN_TEXT + " TEXT(256)," +
                        COLUMN_READ + " INTEGER NOT NULL DEFAULT(0), " +
                        COLUMN_DETAILS + " TEXT(256), " +
                        "PRIMARY KEY (" + COLUMN_START_TIME + ", " + COLUMN_RECIPIENT + ")" +
                        ")";

        private static final String SYSTEM_TABLE_SQL =
                "CREATE TABLE " + TABLE_SYSTEM + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_PURGE_TIME + " INTEGER," +
                        COLUMN_LAST_LATITUDE + " REAL," +
                        COLUMN_LAST_LONGITUDE + " REAL," +
                        COLUMN_LAST_LOCATION_TIME + " INTEGER" +
                        ")";

        public DbHelper(Context context) {
            super(context, name, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(EVENTS_TABLE_SQL);
            db.execSQL(SYSTEM_TABLE_SQL);

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, 0);
            db.insert(TABLE_SYSTEM, null, values);

            updateLastPurgeTime(db);

            log.debug("Created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /* see https://www.techonthenet.com/sqlite/tables/alter_table.php */
            if (oldVersion < DB_VERSION) {
                replaceTable(db, TABLE_EVENTS, EVENTS_TABLE_SQL, new FieldDataConverter() {

                    @Override
                    public String convert(String column, Cursor cursor) {
                        String s = super.convert(column, cursor);
                        if (column.equals(COLUMN_STATE)) {
                            switch (s) {
                                case "PENDING":
                                    return valueOf(STATE_PENDING);
                                case "IGNORED":
                                    return valueOf(STATE_IGNORED);
                                case "PROCESSED":
                                    return valueOf(STATE_PROCESSED);
                                default:
                                    return s;
                            }
                        } else if (column.equals(COLUMN_RECIPIENT)) {
                            if (s == null) {
                                return deviceName();
                            } else {
                                return s;
                            }
                        }

                        return s;
                    }
                });
            }

            log.debug("Upgraded");
        }

    }

    /**
     * Cursor that returns values of {@link PhoneEvent}.
     */
    public static class PhoneEventRowSet extends RowSet<PhoneEvent> {

        public PhoneEventRowSet(Cursor cursor) {
            super(cursor);
        }

        @Override
        public PhoneEvent get() {
            PhoneEvent event = new PhoneEvent();
            event.setState(getInt(COLUMN_STATE));
            event.setStateReason(getInt(COLUMN_STATE_REASON));
            event.setPhone(getString(COLUMN_PHONE));
            event.setIncoming(getBoolean(COLUMN_IS_INCOMING));
            event.setStartTime(getLong(COLUMN_START_TIME));
            event.setEndTime(getLong(COLUMN_END_TIME));
            event.setMissed(getBoolean(COLUMN_IS_MISSED));
            event.setText(getString(COLUMN_TEXT));
            event.setDetails(getString(COLUMN_DETAILS));
            event.setRecipient(getString(COLUMN_RECIPIENT));
            event.setRead(getBoolean(COLUMN_READ));
            event.setLocation(new GeoCoordinates(
                    getDouble(COLUMN_LATITUDE),
                    getDouble(COLUMN_LONGITUDE)
            ));
            return event;
        }

    }

    private class GeoCoordinatesRowSet extends RowSet<GeoCoordinates> {

        public GeoCoordinatesRowSet(Cursor cursor) {
            super(cursor);
        }

        @Override
        public GeoCoordinates get() {
            if (!isNull(COLUMN_LAST_LATITUDE) && !isNull(COLUMN_LAST_LONGITUDE)) {
                return new GeoCoordinates(
                        getDouble(COLUMN_LAST_LATITUDE),
                        getDouble(COLUMN_LAST_LONGITUDE)
                );
            }
            return null;
        }
    }
}