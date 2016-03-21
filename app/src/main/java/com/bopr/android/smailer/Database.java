package com.bopr.android.smailer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bopr.android.smailer.util.StringUtil;
import com.bopr.android.smailer.util.db.ExCursorWrapper;

import java.util.concurrent.TimeUnit;

/**
 * Application database.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Database {

    private static final String TAG = "Database";

    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "smailer.sqlite";
    private static final String TABLE_SYSTEM = "system_data";
    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_MESSAGE_TEXT = "message_text";
    private static final String TABLE_MESSAGE_DETAILS = "message_details";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_MESSAGE_ID = "message_id";
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

    private final DbHelper helper;
    private final Context context;
    private int capacity = 10000;
    private long purgePeriod = TimeUnit.DAYS.toMillis(7);

    public Database(Context context) {
        this.context = context;
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

    public MailMessageCursor getMessages() {
        return new MailMessageCursor(helper.getReadableDatabase().query(TABLE_MESSAGES,
                null, null, null, null, null, null));
    }

    public String getMessageText(long messageId) {
        return new ExCursorWrapper(helper.getReadableDatabase().query(TABLE_MESSAGE_TEXT,
                new String[]{COLUMN_TEXT},
                COLUMN_MESSAGE_ID + "=" + messageId,
                null, null, null, null)
        ).getStringAndClose(COLUMN_TEXT);
    }

    public String getMessageDetails(long messageId) {
        return new ExCursorWrapper(helper.getReadableDatabase().query(TABLE_MESSAGE_DETAILS,
                new String[]{COLUMN_DETAILS},
                COLUMN_MESSAGE_ID + "=" + messageId,
                null, null, null, null)
        ).getStringAndClose(COLUMN_DETAILS);
    }

    public void addMessage(MailMessage item, Throwable error) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_IS_SENT, item.isSent());
            values.put(COLUMN_IS_INCOMING, item.isIncoming());
            values.put(COLUMN_IS_MISSED, item.isMissed());
            values.put(COLUMN_IS_SMS, item.isSms());
            values.put(COLUMN_PHONE, item.getPhone());
            values.put(COLUMN_LATITUDE, item.getLatitude());
            values.put(COLUMN_LONGITUDE, item.getLongitude());
            values.put(COLUMN_START_TIME, item.getStartTime());
            values.put(COLUMN_END_TIME, item.getEndTime());

            long id = db.insert(TABLE_MESSAGES, null, values);
            item.setId(id);

            String text = item.getText();
            if (!StringUtil.isEmpty(text)) {
                values = new ContentValues();
                values.put(COLUMN_MESSAGE_ID, id);
                values.put(COLUMN_TEXT, text);

                db.insert(TABLE_MESSAGE_TEXT, null, values);
            }

            if (error!= null) {
                values = new ContentValues();
                values.put(COLUMN_MESSAGE_ID, id);
                values.put(COLUMN_DETAILS, error.toString());

                db.insert(TABLE_MESSAGE_DETAILS, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Removes all records from log.
     */
    public void clear() {
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

    private long getCurrentSize(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_MESSAGES,
                new String[]{"COUNT(*)"}, null, null, null, null, null);
        cursor.moveToFirst();
        try {
            return cursor.getLong(0);
        } finally {
            cursor.close();
        }
    }

    private long getLastPurgeTime(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_SYSTEM,
                new String[]{COLUMN_PURGE_TIME}, COLUMN_ID + "=0", null, null, null, null);
        cursor.moveToFirst();
        try {
            return cursor.getLong(cursor.getColumnIndex(COLUMN_PURGE_TIME));
        } finally {
            cursor.close();
        }
    }

    private void updateLastPurgeTime(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, 0);
        values.put(COLUMN_PURGE_TIME, System.currentTimeMillis());

        db.replace(TABLE_SYSTEM, null, values);
    }

    public void destroy() {
        context.deleteDatabase(DB_NAME);
    }

    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_IS_SENT + " integer, " +
                    COLUMN_IS_INCOMING + " integer, " +
                    COLUMN_IS_MISSED + " integer, " +
                    COLUMN_IS_SMS + " integer, " +
                    COLUMN_START_TIME + " integer, " +
                    COLUMN_END_TIME + " integer, " +
                    COLUMN_LATITUDE + " real, " +
                    COLUMN_LONGITUDE + " real, " +
                    COLUMN_PHONE + " text(25)" +
                    ")");
            db.execSQL("create table " + TABLE_MESSAGE_TEXT + " (" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_MESSAGE_ID + " integer references " + TABLE_MESSAGES + "(" + COLUMN_ID + ") on delete cascade, " +
                    COLUMN_TEXT + " text(256)" +
                    ")");
            db.execSQL("create table " + TABLE_MESSAGE_DETAILS + " (" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_MESSAGE_ID + " integer references " + TABLE_MESSAGES + "(" + COLUMN_ID + ") on delete cascade, " +
                    COLUMN_DETAILS + " text(256)" +
                    ")");
            db.execSQL("create table " + TABLE_SYSTEM + " (" +
                    COLUMN_ID + " integer primary key, " +
                    COLUMN_PURGE_TIME + " integer" +
                    ")");
            updateLastPurgeTime(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /* do nothing */
        }
    }

    /**
     * Cursor that returns values of {@link MailMessage}.
     */
    public class MailMessageCursor extends ExCursorWrapper {

        public MailMessageCursor(Cursor cursor) {
            super(cursor);
        }

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
                message.setLatitude(getDouble(COLUMN_LATITUDE));
                message.setLongitude(getDouble(COLUMN_LONGITUDE));
            }
            return message;
        }
    }

}