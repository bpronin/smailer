package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bopr.android.smailer.*;
import com.bopr.android.smailer.util.AndroidUtil;

import static com.bopr.android.smailer.util.TagFormatter.formatFrom;


/**
 * Application activity log activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LogFragment extends Fragment {

    private Database database;
    private RecyclerView listView;
    private PhoneEvent selectedListItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = new Database(getActivity());
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        listView = view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_log, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_to_blacklist:
                addToBlacklist();
                return true;
            case R.id.action_add_to_whitelist:
                addToWhitelist();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

    public void showDetails() {
        if (selectedListItem != null) {
            LogDetailsDialogFragment fragment = new LogDetailsDialogFragment();
            fragment.setValue(selectedListItem);
            fragment.showDialog((FragmentActivity) getActivity());
        }
    }

    private void loadData() {
        listView.setAdapter(new ListAdapter(getActivity(), database.getEvents()));
        updateEmptyText();
    }

    protected void updateEmptyText() {
        View view = getView();
        if (view != null) {
            TextView text = view.findViewById(R.id.text_empty);
            if (listView.getAdapter().getItemCount() == 0) {
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }
        }
    }

    private void clearData() {
        AndroidUtil.dialogBuilder(getActivity())
                .setMessage(R.string.message_activity_log_ask_clear)
                .setPositiveButton(R.string.title_clear, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.clearEvents();
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

    private void addToBlacklist() {
        if (selectedListItem != null) {
            String number = selectedListItem.getPhone();

            PhoneEventFilter filter = Settings.loadFilter(getActivity());
            filter.getNumberBlacklist().add(number);
            Settings.saveFilter(getActivity(), filter);

            Toast.makeText(getActivity(),
                    formatFrom(R.string.message_added_to_blacklist, getActivity())
                            .put("number", number)
                            .format(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void addToWhitelist() {
        if (selectedListItem != null) {
            String number = selectedListItem.getPhone();

            PhoneEventFilter filter = Settings.loadFilter(getActivity());
            filter.getNumberWhitelist().add(number);
            Settings.saveFilter(getActivity(), filter);

            Toast.makeText(getActivity(),
                    formatFrom(R.string.message_added_to_whitelist, getActivity())
                            .put("number", number)
                            .format(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private Context context;
        private Database.PhoneEventCursor cursor;

        private ListAdapter(Context context, Database.PhoneEventCursor cursor) {
            this.context = context;
            this.cursor = cursor;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_log, parent, false));
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            PhoneEvent item = getItem(position);
            if (item != null) {
                final PhoneEvent event = cursor.get();

                holder.timeView.setText(DateFormat.format(getString(R.string.event_time_pattern), event.getStartTime()));
                holder.phoneView.setText(event.getPhone());
                holder.typeView.setImageResource(Formats.eventTypeImage(event));
                holder.directionView.setImageResource(Formats.eventDirectionImage(event));
                holder.stateView.setImageResource(Formats.eventStateImage(event));

                holder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        selectedListItem = event;
                        showDetails();
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        selectedListItem = event;
                        return false;
                    }
                });
                holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        getActivity().getMenuInflater().inflate(R.menu.menu_item_log, menu);
                    }
                });
            }
        }

        @Override
        public long getItemId(int position) {
            PhoneEvent item = getItem(position);
            return item != null ? item.getId() : -1;
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }

        PhoneEvent getItem(int position) {
            cursor.moveToPosition(position);
            if (!cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                return cursor.get();
            }
            return null;
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView typeView;
        private final ImageView directionView;
        private final TextView phoneView;
        private final TextView timeView;
        private final ImageView stateView;

        private ItemViewHolder(View view) {
            super(view);
            timeView = view.findViewById(R.id.list_item_time);
            typeView = view.findViewById(R.id.list_item_type);
            directionView = view.findViewById(R.id.list_item_direction);
            phoneView = view.findViewById(R.id.list_item_phone);
            stateView = view.findViewById(R.id.list_item_state);
        }
    }

}
