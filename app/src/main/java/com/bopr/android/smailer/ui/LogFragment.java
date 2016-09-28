package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.ui.recycleview.DividerItemDecoration;

/**
 * Application activity log activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LogFragment extends Fragment {

    private Database database;
    private RecyclerView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        database = new Database(getActivity());
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        listView = (RecyclerView) view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        return view;
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

    public void showDetails(MailMessage message) {
        String details = message.getDetails();
        if (details != null) {
            // TODO: 04.04.2016 details dialog for any type of messages
            AndroidUtil.dialogBuilder(getActivity())
                    .setTitle(R.string.activity_log_title_details)
                    .setMessage(details)
                    .show();
        }
    }

    private void loadData() {
        listView.setAdapter(new ListAdapter(getActivity(), database.getMessages()));
        updateEmptyText();
    }

    protected void updateEmptyText() {
        View view = getView();
        if (view != null) {
            TextView text = (TextView) view.findViewById(R.id.text_empty);
            if (listView.getAdapter().getItemCount() == 0) {
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }
        }
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

    @NonNull
    private String formatMessageText(Context context, MailMessage message) {
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
                .format();
    }

    @NonNull
    private String formatResultText(Context context, MailMessage message) {
        return context.getString(message.isSent()
                ? R.string.activity_log_message_send_email_success
                : R.string.activity_log_message_send_email_failed);
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private int errorColor;
        private int defaultColor;
        private Context context;
        private Database.MailMessageCursor cursor;

        public ListAdapter(Context context, Database.MailMessageCursor cursor) {
            this.context = context;
            this.cursor = cursor;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            ItemViewHolder holder = new ItemViewHolder(inflater.inflate(R.layout.list_item_log, parent, false));

            errorColor = ContextCompat.getColor(context, R.color.errorForeground);
            defaultColor = holder.timeView.getCurrentTextColor();

            return holder;
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            MailMessage item = getItem(position);
            if (item != null) {
                final MailMessage message = cursor.get();

                if (!message.isSent()) {
                    holder.resultView.setTextColor(errorColor);
                } else {
                    holder.resultView.setTextColor(defaultColor);
                }

                holder.timeView.setText(DateFormat.format(context.getString(R.string.activity_log_time_pattern), message.getStartTime()));
                holder.messageView.setText(formatMessageText(context, message));
                holder.resultView.setText(formatResultText(context, message));
                holder.view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showDetails(message);
                    }
                });
            }
        }

        @Override
        public long getItemId(int position) {
            MailMessage item = getItem(position);
            return item != null ? item.getId() : -1;
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }

        public MailMessage getItem(int position) {
            cursor.moveToPosition(position);
            if (!cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                return cursor.get();
            }
            return null;
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final TextView timeView;
        public final TextView messageView;
        public final TextView resultView;

        public ItemViewHolder(View view) {
            super(view);
            this.view = view;
            timeView = (TextView) view.findViewById(R.id.list_item_date);
            messageView = (TextView) view.findViewById(R.id.list_item_message);
            resultView = (TextView) view.findViewById(R.id.list_item_result);
        }

    }

}
