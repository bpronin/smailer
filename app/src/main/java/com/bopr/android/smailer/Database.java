package com.bopr.android.smailer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bopr.android.smailer.util.db.XCursor;

import java.util.concurrent.TimeUnit;

/**
 * Application database.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Database {

    private static final String TAG = "Database";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "smailer.sqlite";
    private static final String TABLE_SYSTEM = "system_data";
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_COUNT = "COUNT(*)";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PURGE_TIME = "messages_purge_time";
    private static final String COLUMN_IS_INCOMING = "is_incoming";
    private static final String COLUMN_IS_MISSED = "is_missed";
    private static final String COLUMN_IS_SMS = "is_sms";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TEXT = "message_text";
    private static final String COLUMN_DETAILS = "details";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_IS_SENT = "is_sent";
    private static final String COLUMN_LAST_LATITUDE = "last_latitude";
    private static final String COLUMN_LAST_LONGITUDE = "last_longitude";
    private static final String COLUMN_LAST_LOCATION_TIME = "last_location_time";

    private final String name;
    private int capacity = 10000;
    private long purgePeriod = TimeUnit.DAYS.toMillis(7);
    private final DbHelper helper;
    private final Context context;

    public Database(Context context) {
        this(context, DB_NAME);
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

    public MailMessage getMessage(long id) {
        return new MailMessageCursor(helper.getReadableDatabase().query(TABLE_MESSAGES,
                null, COLUMN_ID + "=" + id, null, null, null, null)
        ).getAndClose();
    }

    public MailMessageCursor getMessages() {
        return new MailMessageCursor(helper.getReadableDatabase().query(TABLE_MESSAGES,
                null, null, null, null, null,
                COLUMN_START_TIME + " DESC")
        );
    }

    public long updateMessage(MailMessage message) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, message.getId());
        values.put(COLUMN_IS_SENT, message.isSent());
        values.put(COLUMN_IS_INCOMING, message.isIncoming());
        values.put(COLUMN_IS_MISSED, message.isMissed());
        values.put(COLUMN_IS_SMS, message.isSms());
        values.put(COLUMN_PHONE, message.getPhone());
        values.put(COLUMN_START_TIME, message.getStartTime());
        values.put(COLUMN_END_TIME, message.getEndTime());
        values.put(COLUMN_TEXT, message.getText());
        values.put(COLUMN_DETAILS, message.getDetails());
        GeoCoordinates location = message.getLocation();
        if (location != null) {
            values.put(COLUMN_LATITUDE, location.getLatitude());
            values.put(COLUMN_LONGITUDE, location.getLongitude());
        }

        long id = db.replace(TABLE_MESSAGES, null, values);
        message.setId(id);
        return id;
    }

    public MailMessageCursor getUnsentMessages() {
        return new MailMessageCursor(helper.getReadableDatabase().query(TABLE_MESSAGES,
                null, COLUMN_IS_SENT + "=0", null, null, null,
                COLUMN_START_TIME + " DESC")
        );
    }

    public void updateSent(long messageId, boolean sent) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_SENT, sent);

        helper.getWritableDatabase().update(TABLE_MESSAGES, values, COLUMN_ID + "=" + messageId, null);
    }

    public boolean hasUnsentMessages() {
        return XCursor.forLong(helper.getReadableDatabase().query(TABLE_MESSAGES,
                new String[]{COLUMN_COUNT}, COLUMN_IS_SENT + "=0", null, null, null, null))
                .getAndClose() > 0;
    }

    /**
     * Removes all records from log.
     */
    public void clearMessages() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_MESSAGES, null, null);
            updateLastPurgeTime(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Removes all stale records that exceeds specified capacity if given
     * period of time has elapsed.
     */
    public void purge() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (System.currentTimeMillis() - getLastPurgeTime(db) >= purgePeriod
                    && getCurrentSize(db) >= capacity) {

                db.execSQL("delete from " + TABLE_MESSAGES +
                        " where " + COLUMN_ID + " not in " +
                        "(" +
                        "select " + COLUMN_ID + " from " + TABLE_MESSAGES +
                        " order by " + COLUMN_ID + " desc " +
                        "limit " + capacity +
                        ")");

                updateLastPurgeTime(db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void saveLastLocation(GeoCoordinates location) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LATITUDE, location != null ? location.getLatitude() : null);
        values.put(COLUMN_LAST_LONGITUDE, location != null ? location.getLongitude() : null);
        values.put(COLUMN_LAST_LOCATION_TIME, System.currentTimeMillis());

        helper.getWritableDatabase().update(TABLE_SYSTEM, values, COLUMN_ID + "=0", null);
    }

    private long getCurrentSize(SQLiteDatabase db) {
        return XCursor.forLong(db.query(TABLE_MESSAGES, new String[]{COLUMN_COUNT}, null, null,
                null, null, null)).getAndClose();
    }

    private long getLastPurgeTime(SQLiteDatabase db) {
        return XCursor.forLong(db.query(TABLE_SYSTEM, new String[]{COLUMN_PURGE_TIME},
                COLUMN_ID + "=0", null, null, null, null)).getAndClose();
    }

    private void updateLastPurgeTime(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PURGE_TIME, System.currentTimeMillis());

        db.update(TABLE_SYSTEM, values, COLUMN_ID + "=0", null);
    }

    public void destroy() {
        context.deleteDatabase(name);
    }

    public GeoCoordinates getLastLocation() {
        return new XCursor<GeoCoordinates>(helper.getReadableDatabase().query(TABLE_SYSTEM,
                new String[]{COLUMN_LAST_LATITUDE, COLUMN_LAST_LONGITUDE}, COLUMN_ID + "=0",
                null, null, null, null)) {

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
        }.getAndClose();
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
            db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_IS_SENT + " INTEGER, " +
                    COLUMN_IS_INCOMING + " INTEGER, " +
                    COLUMN_IS_MISSED + " INTEGER, " +
                    COLUMN_IS_SMS + " INTEGER, " +
                    COLUMN_START_TIME + " INTEGER, " +
                    COLUMN_END_TIME + " INTEGER, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_PHONE + " TEXT(25)," +
                    COLUMN_TEXT + " TEXT(256)," +
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
            /* no updates yet - do nothing */
        }

    }

    /**
     * Cursor that returns values of {@link MailMessage}.
     */
    public class MailMessageCursor extends XCursor<MailMessage> {

        public MailMessageCursor(Cursor cursor) {
            super(cursor);
        }

        @Override
        public MailMessage get() {
            MailMessage message = null;
            if (!isBeforeFirst() && !isAfterLast()) {
                message = new MailMessage();
                message.setId(getLong(COLUMN_ID));
                message.setSent(getBoolean(COLUMN_IS_SENT));
                message.setPhone(getString(COLUMN_PHONE));
                message.setIncoming(getBoolean(COLUMN_IS_INCOMING));
                message.setStartTime(getLong(COLUMN_START_TIME));
                message.setEndTime(getLong(COLUMN_END_TIME));
                message.setMissed(getBoolean(COLUMN_IS_MISSED));
                message.setSms(getBoolean(COLUMN_IS_SMS));
                message.setText(getString(COLUMN_TEXT));
                message.setDetails(getString(COLUMN_DETAILS));
                message.setLocation(new GeoCoordinates(
                        getDouble(COLUMN_LATITUDE),
                        getDouble(COLUMN_LONGITUDE)
                ));
            }
            return message;
        }

    }

}