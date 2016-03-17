package com.bopr.android.smailer.settings;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bopr.android.smailer.ActivityLog;
import com.bopr.android.smailer.ActivityLogItem;
import com.bopr.android.smailer.R;

/**
 * Class LogListAdapter.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class LogListAdapter extends CursorAdapter {

    private static final String DATE_PATTERN = "yyyy-MM-dd\nHH:mm:ss";
    private final LayoutInflater inflater;
    private final int errorColor;
    private int defaultColor;
    private TextView timeView;
    private TextView messageView;

    public LogListAdapter(Context context, ActivityLog.Cursor cursor) {
        super(context, cursor, 0);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        errorColor = ContextCompat.getColor(context, R.color.errorForeground);
    }

    @Override
    public View newView(Context context, android.database.Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item_log, parent, false);
        timeView = (TextView) view.findViewById(R.id.list_item_date);
        messageView = (TextView) view.findViewById(R.id.list_item_message);
        defaultColor = timeView.getCurrentTextColor();
        return view;
    }

    @Override
    public void bindView(View view, Context context, android.database.Cursor cursor) {
        ActivityLogItem item = ((ActivityLog.Cursor) cursor).get();

        if (item.getLevel() == ActivityLog.LEVEL_ERROR) {
            timeView.setTextColor(errorColor);
            messageView.setTextColor(errorColor);
        } else {
            timeView.setTextColor(defaultColor);
            messageView.setTextColor(defaultColor);
        }

//            android.R.drawable.stat_notify_error;
//            android.R.drawable.stat_notify_missed_call;
//            android.R.drawable.ic_menu_call;

        timeView.setText(DateFormat.format(DATE_PATTERN, item.getTime()));
        messageView.setText(item.getMessage());
    }

}
