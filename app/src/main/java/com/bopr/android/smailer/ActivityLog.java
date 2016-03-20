package com.bopr.android.smailer;

import android.content.ContentValues;
import android.content.Context;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bopr.android.smailer.util.TagFormatter;

import java.util.Date;

/**
 * Application activity log.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ActivityLog {

    private static final String TAG = "ActivityLog";

    public static final int LEVEL_ERROR = Log.ERROR;
    public static final int LEVEL_INFO = Log.INFO;
    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "smailer.sqlite";
    private static final String TABLE_LOG = "activity_log";
    private static final String TABLE_SYSTEM = "system_data";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LOG_TIMESTAMP = "timestamp";
    private static final String COLUMN_LOG_MESSAGE = "message";
    private static final String COLUMN_LOG_LEVEL = "level";
    private static final String COLUMN_LOG_DETAILS = "details";
    private static final String COLUMN_PURGE_TIME = "purge_time";

    public static ActivityLog instance;

    private final Context context;
    private final DbHelper helper;
    private int capacity = 10000;
    private long purgePeriod = 86400000;

    public static ActivityLog getInstance(Context context) {
        if (instance == null) {
            instance = new ActivityLog(context);
        } else {
            if (instance.context != context) {
                Log.w(TAG, "Accessing getInstance from different context");
            }
        }
        return instance;
    }

    protected ActivityLog(Context context) {
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

    /**
     * Adds success record.
     *
     * @param message log message
     */
    public synchronized void success(MailMessage message) {
        ActivityLogItem item = new ActivityLogItem(ActivityLog.LEVEL_INFO);
        item.setMessage(formatMessage(message, R.string.activity_log_message_send_email_success));
        add(item);
    }

    /**
     * Adds error record.
     *
     * @param message log message
     */
    public synchronized void error(MailMessage message, Exception error) {
        ActivityLogItem item = new ActivityLogItem(ActivityLog.LEVEL_ERROR);
        item.setMessage(formatMessage(message, R.string.activity_log_message_send_email_failed));
        item.setDetails(error.toString());
        add(item);
    }

    /**
     * Returns all records as a cursor.
     *
     * @return cursor for all records
     */
    public synchronized Cursor getAll() {
        return new Cursor(helper.getReadableDatabase().query(TABLE_LOG,
                null,
                null,
                null,
                null,
                null,
                COLUMN_LOG_TIMESTAMP + " desc")
        );
    }

    /**
     * Removes all records from log.
     */
    public synchronized void clear() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_LOG, null, null);
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
    private void purge() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (getCurrentSize(db) >= capacity
                    && System.currentTimeMillis() - getLastPurgeTime(db) >= purgePeriod) {
                db.execSQL("delete from " + TABLE_LOG +
                        " where " + COLUMN_ID + " not in " +
                        "(" +
                        "select " + COLUMN_ID + " from " + TABLE_LOG +
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
        android.database.Cursor cursor = db.query(TABLE_LOG,
                new String[]{"COUNT(*)"}, null, null, null, null, null);
        cursor.moveToFirst();
        try {
            return cursor.getLong(0);
        } finally {
            cursor.close();
        }
    }

    private long getLastPurgeTime(SQLiteDatabase db) {
        android.database.Cursor cursor = db.query(TABLE_SYSTEM,
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
        long value = System.currentTimeMillis();
        values.put(COLUMN_PURGE_TIME, value);
        db.replace(TABLE_SYSTEM, null, values);
    }

    private void add(ActivityLogItem item) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_TIMESTAMP, item.getTime().getTime());
        values.put(COLUMN_LOG_MESSAGE, item.getMessage());
        values.put(COLUMN_LOG_LEVEL, item.getLevel());
        values.put(COLUMN_LOG_DETAILS, item.getDetails());

        long id = helper.getWritableDatabase().insert(TABLE_LOG, null, values);
        item.setId(id);

        purge();
    }

    @NonNull
    private String formatMessage(MailMessage message, int resultText) {
        int messageText;

        if (message.isMissed()) {
            messageText = R.string.activity_log_message_missed_call;
        } else if (message.isSms()) {
            if (message.isIncoming()) {
                messageText = R.string.activity_log_message_incoming_sms;
            } else {
                messageText = R.string.activity_log_message_outgoing_sms;
            }
        } else {
            if (message.isIncoming()) {
                messageText = R.string.activity_log_message_incoming_call;
            } else {
                messageText = R.string.activity_log_message_outgoing_call;
            }
        }

        return TagFormatter.from(R.string.activity_log_message, context.getResources())
                .putResource("message", messageText)
                .put("phone", message.getPhone())
                .putResource("result", resultText)
                .format();
    }

    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_LOG + " (" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_LOG_TIMESTAMP + " integer, " +
                    COLUMN_LOG_LEVEL + " integer, " +
                    COLUMN_LOG_MESSAGE + " varchar(100)," +
                    COLUMN_LOG_DETAILS + " varchar(200)" +
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
     * Cursor that returns values of {@link ActivityLogItem}.
     */
    public class Cursor extends CursorWrapper {

        public Cursor(android.database.Cursor cursor) {
            super(cursor);
        }

        public ActivityLogItem get() {
            ActivityLogItem item = null;

            if (!isBeforeFirst() && !isAfterLast()) {
                item = new ActivityLogItem(LEVEL_INFO);
                item.setId(getLong(getColumnIndex(COLUMN_ID)));
                item.setTime(new Date(getLong(getColumnIndex(COLUMN_LOG_TIMESTAMP))));
                item.setMessage(getString(getColumnIndex(COLUMN_LOG_MESSAGE)));
                item.setLevel(getInt(getColumnIndex(COLUMN_LOG_LEVEL)));

                int detailsColumn = getColumnIndex(COLUMN_LOG_DETAILS);
                if (detailsColumn != -1) {
                    item.setDetails(getString(getColumnIndex(COLUMN_LOG_DETAILS)));
                }
            }

            return item;
        }
    }

}