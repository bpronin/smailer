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
 * Class ActivityLog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ActivityLog {

    private static final String TAG = "ActivityLog";

    public static final int LEVEL_ERROR = Log.ERROR;
    public static final int LEVEL_INFO = Log.INFO;

    public static ActivityLog instance;

    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "smailer.sqlite";
    private static final String TABLE_LOG = "activity_log";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LOG_TIMESTAMP = "timestamp";
    private static final String COLUMN_LOG_MESSAGE = "message";
    private static final String COLUMN_LOG_LEVEL = "level";
    private static final String COLUMN_LOG_DETAILS = "details";

    private final Context context;
    private final DbHelper helper;

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

    private ActivityLog(Context context) {
        this.context = context;
        helper = new DbHelper(context);
    }

    public void success(MailMessage message) {
        ActivityLogItem item = new ActivityLogItem(ActivityLog.LEVEL_INFO);
        item.setMessage(formatMessage(message, R.string.activity_log_message_send_email_success));
        add(item);
    }

    public void error(MailMessage message, Exception error) {
        ActivityLogItem item = new ActivityLogItem(ActivityLog.LEVEL_ERROR);
        item.setMessage(formatMessage(message, R.string.activity_log_message_send_email_failed));
        item.setDetails(error.toString());
        add(item);
    }

    public Cursor getAll() {
        return new Cursor(helper.getReadableDatabase().query(TABLE_LOG,
                null,
                null,
                null,
                null,
                null,
                COLUMN_LOG_TIMESTAMP + " desc")
        );
    }

    public void clear() {
        helper.getWritableDatabase().execSQL("delete from " + TABLE_LOG);
    }

    private void add(ActivityLogItem item) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_TIMESTAMP, item.getTime().getTime());
        values.put(COLUMN_LOG_MESSAGE, item.getMessage());
        values.put(COLUMN_LOG_LEVEL, item.getLevel());
        values.put(COLUMN_LOG_DETAILS, item.getDetails());

        long id = helper.getWritableDatabase().insert(TABLE_LOG, null, values);
        item.setId(id);
    }

    @NonNull
    private String formatMessage(MailMessage message, int resultText) {
        int messageText;

        if (message.isMissed()) {
            messageText = R.string.email_subject_missed_call;
        } else if (message.isSms()) {
            if (message.isIncoming()) {
                messageText = R.string.email_subject_incoming_sms;
            } else {
                messageText = R.string.email_subject_outgoing_sms;
            }
        } else {
            if (message.isIncoming()) {
                messageText = R.string.email_subject_incoming_call;
            } else {
                messageText = R.string.email_subject_outgoing_call;
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /* do nothing */
        }
    }

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