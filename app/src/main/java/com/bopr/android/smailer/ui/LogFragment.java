package com.bopr.android.smailer.ui;

import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.TagFormatter;

/**
 * Application activity log activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LogFragment extends ListFragment {

    private Database database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        database = new Database(getActivity());
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getResources().getString(R.string.activity_log_empty_log));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_log, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log_clear) {
            clearData();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);

        Database.MailMessageCursor cursor = (Database.MailMessageCursor) list.getAdapter().getItem(position);
        String details = cursor.get().getDetails();
        if (details != null) {
            AndroidUtil.dialogBuilder(getActivity())
                    .setTitle(R.string.activity_log_title_details)
                    .setMessage(details)
                    .show();
        }
    }

    private void loadData() {
        setListAdapter(new ListAdapter(getActivity(), database.getMessages()));
    }

    private void clearData() {
        AndroidUtil.dialogBuilder(getActivity())
                .setMessage(R.string.activity_log_ask_clear)
                .setPositiveButton(R.string.action_clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.clearMessages();
                        loadData();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private class ListAdapter extends CursorAdapter {

        private final LayoutInflater inflater;
        private final int errorColor;
        private int defaultColor;
        private TextView timeView;
        private TextView messageView;

        public ListAdapter(Context context, Database.MailMessageCursor cursor) {
            super(context, cursor, 0);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            errorColor = ContextCompat.getColor(context, R.color.errorForeground);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = inflater.inflate(R.layout.list_item_log, parent, false);
            timeView = (TextView) view.findViewById(R.id.list_item_date);
            messageView = (TextView) view.findViewById(R.id.list_item_message);
            defaultColor = timeView.getCurrentTextColor();
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            MailMessage message = ((Database.MailMessageCursor) cursor).get();

            if (!message.isSent()) {
                timeView.setTextColor(errorColor);
                messageView.setTextColor(errorColor);
            } else {
                timeView.setTextColor(defaultColor);
                messageView.setTextColor(defaultColor);
            }

            //            android.R.drawable.stat_notify_error;
            //            android.R.drawable.stat_notify_missed_call;
            //            android.R.drawable.ic_menu_call;

            timeView.setText(DateFormat.format(context.getString(R.string.activity_log_time_pattern), message.getStartTime()));
            messageView.setText(formatLogMessage(context, message));
        }

        @NonNull
        private String formatLogMessage(Context context, MailMessage message) {
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

            int result = message.isSent() ? R.string.activity_log_message_send_email_success
                    : R.string.activity_log_message_send_email_failed;
            return TagFormatter.from(R.string.activity_log_message, context.getResources())
                    .putResource("message", messageText)
                    .put("phone", message.getPhone())
                    .putResource("result", result)
                    .format();
        }

    }
}
