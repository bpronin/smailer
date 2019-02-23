package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bopr.android.smailer.util.db.XCursor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static java.lang.System.currentTimeMillis;

/**
 * Application database.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
// TODO: 18.02.2019 migrate to Room. see https://developer.android.com/training/data-storage/room/
public class Database {

    private static Logger log = LoggerFactory.getLogger("Database");

    private static final int DB_VERSION = 2;

    private static final String DATABASE_EVENT = "database-event";

    private static final String TABLE_SYSTEM = "system_data";
    private static final String TABLE_EVENTS = "phone_events";

    private static final String COLUMN_COUNT = "COUNT(*)";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PURGE_TIME = "messages_purge_time";
    private static final String COLUMN_IS_INCOMING = "is_incoming";
    private static final String COLUMN_IS_MISSED = "is_missed";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TEXT = "message_text";
    private static final String COLUMN_DETAILS = "details";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_LAST_LATITUDE = "last_latitude";
    private static final String COLUMN_LAST_LONGITUDE = "last_longitude";
    private static final String COLUMN_LAST_LOCATION_TIME = "last_location_time";
    private static final String COLUMN_READ = "message_read";

    private final String name;
    private int capacity = 10000;
    private long purgePeriod = TimeUnit.DAYS.toMillis(7);
    private final DbHelper helper;
    private final Context context;

    public Database(Context context) {
        this(context, Settings.DB_NAME);
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

    public PhoneEventCursor getEvents() {
        return new PhoneEventCursor(helper.getReadableDatabase().query(TABLE_EVENTS, null,
                null, null, null, null,
                COLUMN_START_TIME + " DESC")
        );
    }

    public PhoneEventCursor getPendingEvents() {
        return new PhoneEventCursor(helper.getReadableDatabase().query(TABLE_EVENTS, null,
                COLUMN_STATE + "=?", new String[]{PhoneEvent.STATE_PENDING}, null, null,
                COLUMN_START_TIME + " DESC")
        );
    }

    public long getUnreadEventsCount() {
        return XCursor.forLong(helper.getReadableDatabase().query(TABLE_EVENTS, new String[]{COLUMN_COUNT},
                COLUMN_READ + "<>1", null, null, null, null));
    }

    public void putEvent(PhoneEvent event) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, event.getId());
        values.put(COLUMN_STATE, event.getState());
        values.put(COLUMN_IS_INCOMING, event.isIncoming());
        values.put(COLUMN_IS_MISSED, event.isMissed());
        values.put(COLUMN_PHONE, event.getPhone());
        values.put(COLUMN_START_TIME, event.getStartTime());
        values.put(COLUMN_END_TIME, event.getEndTime());
        values.put(COLUMN_TEXT, event.getText());
        values.put(COLUMN_DETAILS, event.getDetails());
        values.put(COLUMN_READ, event.isRead());
        GeoCoordinates location = event.getLocation();
        if (location != null) {
            values.put(COLUMN_LATITUDE, location.getLatitude());
            values.put(COLUMN_LONGITUDE, location.getLongitude());
        }

        long id = db.replace(TABLE_EVENTS, null, values);
        event.setId(id);

        log.debug("Put record: " + values);
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
        return new GeoCoordinatesCursor(helper.getReadableDatabase().query(TABLE_SYSTEM,
                new String[]{COLUMN_LAST_LATITUDE, COLUMN_LAST_LONGITUDE}, COLUMN_ID + "=0",
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

    public void registerListener(BroadcastReceiver listener) {
        LocalBroadcastManager.getInstance(context).registerReceiver(listener, new IntentFilter(DATABASE_EVENT));
    }

    public void unregisterListener(BroadcastReceiver listener) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(listener);
    }

    private long getCurrentSize(SQLiteDatabase db) {
        return XCursor.forLong(db.query(TABLE_EVENTS, new String[]{COLUMN_COUNT}, null, null,
                null, null, null));
    }

    private long getLastPurgeTime(SQLiteDatabase db) {
        return XCursor.forLong(db.query(TABLE_SYSTEM, new String[]{COLUMN_PURGE_TIME},
                COLUMN_ID + "=0", null, null, null, null));
    }

    private void updateLastPurgeTime(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PURGE_TIME, currentTimeMillis());

        db.update(TABLE_SYSTEM, values, COLUMN_ID + "=0", null);
    }

    public void notifyChanged() {
        log.debug("Broadcasting data changed");

        Intent intent = new Intent(DATABASE_EVENT);
        intent.putExtra("message", "changed");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, name, null, DB_VERSION);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys = ON");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STATE + " INTEGER, " +
                    COLUMN_IS_INCOMING + " INTEGER, " +
                    COLUMN_IS_MISSED + " INTEGER, " +
                    COLUMN_START_TIME + " INTEGER, " +
                    COLUMN_END_TIME + " INTEGER, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_PHONE + " TEXT(25)," +
                    COLUMN_TEXT + " TEXT(256)," +
                    COLUMN_READ + " INTEGER NOT NULL DEFAULT(0), " +
                    COLUMN_DETAILS + " TEXT(256)" +
                    ")");

            db.execSQL("CREATE TABLE " + TABLE_SYSTEM + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_PURGE_TIME + " INTEGER," +
                    COLUMN_LAST_LATITUDE + " REAL," +
                    COLUMN_LAST_LONGITUDE + " REAL," +
                    COLUMN_LAST_LOCATION_TIME + " INTEGER" +
                    ")");

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, 0);
            db.insert(TABLE_SYSTEM, null, values);

            updateLastPurgeTime(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion == 2 && oldVersion == 1) {
                db.execSQL("ALTER TABLE " + TABLE_EVENTS +
                        " ADD " + COLUMN_READ + " INTEGER NOT NULL DEFAULT(0)");
            }
        }

    }

    /**
     * Cursor that returns values of {@link PhoneEvent}.
     */
    public class PhoneEventCursor extends XCursor<PhoneEvent> {

        public PhoneEventCursor(Cursor cursor) {
            super(cursor);
        }

        @Override
        public PhoneEvent getRow() {
            PhoneEvent event = new PhoneEvent();
            event.setId(getLong(COLUMN_ID));
            event.setState(getString(COLUMN_STATE));
            event.setPhone(getString(COLUMN_PHONE));
            event.setIncoming(getBoolean(COLUMN_IS_INCOMING));
            event.setStartTime(getLong(COLUMN_START_TIME));
            event.setEndTime(getLong(COLUMN_END_TIME));
            event.setMissed(getBoolean(COLUMN_IS_MISSED));
            event.setText(getString(COLUMN_TEXT));
            event.setDetails(getString(COLUMN_DETAILS));
            event.setLocation(new GeoCoordinates(
                    getDouble(COLUMN_LATITUDE),
                    getDouble(COLUMN_LONGITUDE)
            ));
            return event;
        }

    }

    private class GeoCoordinatesCursor extends XCursor<GeoCoordinates> {

        public GeoCoordinatesCursor(Cursor cursor) {
            super(cursor);
        }

        @Override
        public GeoCoordinates getRow() {
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