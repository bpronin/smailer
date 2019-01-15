package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.Formats;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.AndroidUtil;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.bopr.android.smailer.util.TagFormatter.formatter;


/**
 * Application activity log activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LogFragment extends Fragment {

    private Database database;
    private RecyclerView listView;
    private ListAdapter listAdapter;
    private PhoneEventFilter phoneEventFilter;
    private int selectedListItemPosition = NO_POSITION;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private BroadcastReceiver databaseListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                loadData();
            }
        };
        Settings.getPreferences(getActivity()).registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        databaseListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadData();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.removeListener(databaseListener);
        Settings.getPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = new Database(getActivity());
        database.addListener(databaseListener);
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        listView = view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        return view;
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
            case R.id.action_remove_from_lists:
                removeFromLists();
                return true;
            case R.id.action_log_clear:
                clearData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    private void loadData() {
        listAdapter = new ListAdapter(getActivity(), database.getEvents());
        listView.setAdapter(listAdapter);
        phoneEventFilter = Settings.loadFilter(getActivity());
        updateEmptyText();
    }

    private void updateEmptyText() {
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

    private void showDetails() {
        if (selectedListItemPosition != NO_POSITION) {
            LogDetailsDialogFragment fragment = new LogDetailsDialogFragment();
            fragment.setValue(listAdapter.getItem(selectedListItemPosition));
            fragment.showDialog(getActivity());
        }
    }

    private void clearData() {
        AndroidUtil.dialogBuilder(getActivity())
                .setMessage(R.string.message_activity_log_ask_clear)
                .setPositiveButton(R.string.title_clear_log, new DialogInterface.OnClickListener() {

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
        if (selectedListItemPosition != NO_POSITION) {
            String number = listAdapter.getItem(selectedListItemPosition).getPhone();

            phoneEventFilter.getPhoneBlacklist().add(number);
            Settings.saveFilter(getActivity(), phoneEventFilter);

            Toast.makeText(getActivity(),
                    formatter(R.string.message_added_to_blacklist, getActivity())
                            .put("number", number)
                            .format(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void addToWhitelist() {
        if (selectedListItemPosition != NO_POSITION) {
            String number = listAdapter.getItem(selectedListItemPosition).getPhone();

            phoneEventFilter.getPhoneWhitelist().add(number);
            Settings.saveFilter(getActivity(), phoneEventFilter);

            Toast.makeText(getActivity(),
                    formatter(R.string.message_added_to_whitelist, getActivity())
                            .put("number", number)
                            .format(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFromLists() {
        if (selectedListItemPosition != NO_POSITION) {
            String number = listAdapter.getItem(selectedListItemPosition).getPhone();

            phoneEventFilter.getPhoneWhitelist().remove(number);
            phoneEventFilter.getPhoneBlacklist().remove(number);
            Settings.saveFilter(getActivity(), phoneEventFilter);

            Toast.makeText(getActivity(),
                    formatter(R.string.message_phone_removed_from_filter, getActivity())
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

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_log, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
            PhoneEvent item = getItem(position);
            if (item != null) {
                final PhoneEvent event = cursor.found();

                holder.timeView.setText(DateFormat.format(getString(R.string.event_time_pattern), event.getStartTime()));
                holder.textView.setText(event.getText());

                holder.phoneView.setText(event.getPhone());
                if (phoneEventFilter.acceptPhone(event.getPhone())) {
                    holder.phoneView.setPaintFlags(holder.defaultPhoneFlags);
                } else {
                    holder.phoneView.setPaintFlags(holder.defaultPhoneFlags | Paint.STRIKE_THRU_TEXT_FLAG);
                }

                holder.typeView.setImageResource(Formats.eventTypeImage(event));
                holder.directionView.setImageResource(Formats.eventDirectionImage(event));
                holder.stateView.setImageResource(Formats.eventStateImage(event));

                holder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        selectedListItemPosition = holder.getAdapterPosition();
                        showDetails();
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        selectedListItemPosition = holder.getAdapterPosition();
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
                return cursor.found();
            }
            return null;
        }

    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView typeView;
        private final ImageView directionView;
        private final TextView phoneView;
        private final TextView timeView;
        private final TextView textView;
        private final ImageView stateView;
        private final int defaultPhoneFlags;

        private ItemViewHolder(View view) {
            super(view);
            timeView = view.findViewById(R.id.list_item_time);
            textView = view.findViewById(R.id.list_item_text);
            typeView = view.findViewById(R.id.list_item_type);
            directionView = view.findViewById(R.id.list_item_direction);
            phoneView = view.findViewById(R.id.list_item_phone);
            stateView = view.findViewById(R.id.list_item_state);
            defaultPhoneFlags = phoneView.getPaintFlags();
        }
    }

}
