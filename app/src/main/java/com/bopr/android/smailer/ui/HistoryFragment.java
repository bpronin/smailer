package com.bopr.android.smailer.ui;

import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ui.EditFilterListItemDialogFragment.OnClose;
import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.TextUtil;

import java.util.List;
import java.util.Set;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.bopr.android.smailer.Database.registerDatabaseListener;
import static com.bopr.android.smailer.Database.unregisterDatabaseListener;
import static com.bopr.android.smailer.PhoneEvent.REASON_NUMBER_BLACKLISTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TEXT_BLACKLISTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TRIGGER_OFF;
import static com.bopr.android.smailer.PhoneEvent.STATE_PENDING;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_WHITELIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_WHITELIST;
import static com.bopr.android.smailer.util.AddressUtil.containsPhone;
import static com.bopr.android.smailer.util.AddressUtil.findPhone;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.TextUtil.formatDuration;
import static com.bopr.android.smailer.util.TextUtil.isNullOrBlank;
import static com.bopr.android.smailer.util.UiUtil.eventDirectionImage;
import static com.bopr.android.smailer.util.UiUtil.eventStateImage;
import static com.bopr.android.smailer.util.UiUtil.eventTypeImage;
import static com.bopr.android.smailer.util.UiUtil.showToast;


/**
 * Application activity log activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class HistoryFragment extends BaseFragment {

    private Database database;
    private RecyclerView listView;
    private ListAdapter listAdapter;
    private PhoneEventFilter callFilter;
    private int selectedListItemPosition = NO_POSITION;
    private SharedPreferences.OnSharedPreferenceChangeListener settingsChangeListener;
    private BroadcastReceiver databaseListener;
    private TagFormatter formatter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formatter = new TagFormatter(requireContext());

        settingsChangeListener = settings.registerChangeListener(new SettingsListener());

        database = new Database(getContext());
        databaseListener = registerDatabaseListener(requireContext(), this::reloadItems);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        listView = view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadItems();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settings.unregisterChangeListener(settingsChangeListener);
        unregisterDatabaseListener(requireContext(), databaseListener);
        database.close();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_clear:
                onClearData();
                return true;
            case R.id.action_log_mar_all_read:
                onMarkAllAsRead();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_to_blacklist:
                onAddToBlacklist();
                return true;
            case R.id.action_add_to_whitelist:
                onAddToWhitelist();
                return true;
            case R.id.action_remove_from_lists:
                onRemoveFromLists();
                return true;
            case R.id.action_ignore:
                onMarkAsIgnored();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void reloadItems() {
        listAdapter = new ListAdapter(database.getEvents().toList());
        listView.setAdapter(listAdapter);
        callFilter = settings.getFilter();
        updateEmptyText();
    }

    private void updateEmptyText() {
        View view = getView();
        if (view != null) {
            TextView text = view.findViewById(R.id.text_empty);
            if (listAdapter.getItemCount() == 0) {
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }
        }
    }

    private void showDetails() {
        if (selectedListItemPosition != NO_POSITION) {
            HistoryDetailsDialogFragment fragment = new HistoryDetailsDialogFragment();
            fragment.setValue(listAdapter.getItem(selectedListItemPosition));
            fragment.showDialog(requireActivity());
        }
    }

    private void onClearData() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.ask_clear_history)
                .setPositiveButton(R.string.clear, (dialog, which) -> {
                    database.clearEvents();
                    database.notifyChanged();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onMarkAllAsRead() {
        database.markAllAsRead(true);
        database.notifyChanged();
    }

    private void onAddToBlacklist() {
        if (selectedListItemPosition != NO_POSITION) {
            String phone = listAdapter.getItem(selectedListItemPosition).getPhone();
            createEditPhoneDialog(phone, number -> {
                if (!TextUtil.isNullOrEmpty(number)) {
                    Set<String> blacklist = callFilter.getPhoneBlacklist();
                    if (blacklist.contains(number)) {
                        showToast(requireContext(), formatter.pattern(R.string.item_already_exists)
                                .put("item", number).format()
                        );
                    } else if (!isNullOrBlank(number)) {
                        blacklist.add(number);
                        settings.edit().putFilter(callFilter).apply();
                    }
                }
            }).showDialog(requireActivity());
        }
    }

    private void onAddToWhitelist() {
        if (selectedListItemPosition != NO_POSITION) {
            String phone = listAdapter.getItem(selectedListItemPosition).getPhone();
            createEditPhoneDialog(phone, number -> {
                if (!TextUtil.isNullOrEmpty(number)) {
                    Set<String> whitelist = callFilter.getPhoneWhitelist();
                    if (whitelist.contains(number)) {
                        showToast(requireContext(), formatter.pattern(R.string.item_already_exists)
                                .put("item", number).format()
                        );
                    } else if (!isNullOrBlank(number)) {
                        whitelist.add(number);
                        settings.edit().putFilter(callFilter).apply();
                    }
                }
            }).showDialog(requireActivity());
        }
    }

    private void onRemoveFromLists() {
        if (selectedListItemPosition != NO_POSITION) {
            String number = listAdapter.getItem(selectedListItemPosition).getPhone();

            removeFromCallFilter(callFilter.getPhoneWhitelist(), number);
            removeFromCallFilter(callFilter.getPhoneBlacklist(), number);
            settings.edit().putFilter(callFilter).apply();

            showToast(requireContext(), formatter.pattern(R.string.phone_removed_from_filter)
                    .put("number", number)
                    .format());
        }
    }

    private void onMarkAsIgnored() {
        if (selectedListItemPosition != NO_POSITION) {
            PhoneEvent event = listAdapter.getItem(selectedListItemPosition);
            event.setState(PhoneEvent.STATE_IGNORED);
            database.putEvent(event);
            database.notifyChanged();
        }
    }

    private void removeFromCallFilter(Set<String> list, String number) {
        list.remove(findPhone(list, number));
    }

    private EditFilterListItemDialogFragment createEditPhoneDialog(String number, OnClose onClose) {
        EditPhoneDialogFragment dialog = new EditPhoneDialogFragment();
        dialog.setTitle(R.string.add);
        dialog.setInitialValue(number);
        dialog.setOnClose(onClose);
        return dialog;
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private final List<PhoneEvent> items;

        private ListAdapter(List<PhoneEvent> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_log, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
            final PhoneEvent event = getItem(position);
            if (event != null) {
                holder.timeView.setText(DateFormat.format(getString(R.string._time_pattern), event.getStartTime()));
                holder.textView.setText(formatSummary(event));
                holder.phoneView.setText(event.getPhone());
                holder.typeView.setImageResource(eventTypeImage(event));
                holder.directionView.setImageResource(eventDirectionImage(event));
                holder.stateView.setImageResource(eventStateImage(event));

                holder.typeView.setEnabled((event.getStateReason() & REASON_TRIGGER_OFF) == 0);
                holder.directionView.setEnabled((event.getStateReason() & REASON_TRIGGER_OFF) == 0);
                holder.textView.setEnabled((event.getStateReason() & REASON_TEXT_BLACKLISTED) == 0);
                holder.phoneView.setEnabled((event.getStateReason() & REASON_NUMBER_BLACKLISTED) == 0);

                holder.itemView.setOnClickListener(v -> {
                    selectedListItemPosition = holder.getAdapterPosition();
                    showDetails();
                });
                holder.itemView.setOnLongClickListener(v -> {
                    selectedListItemPosition = holder.getAdapterPosition();
                    return false;
                });
                holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    requireActivity().getMenuInflater().inflate(R.menu.menu_context_history, menu);

                    if (event.getState() != STATE_PENDING) {
                        menu.removeItem(R.id.action_ignore);
                    }

                    boolean blacklisted = containsPhone(callFilter.getPhoneBlacklist(), event.getPhone());
                    boolean whitelisted = containsPhone(callFilter.getPhoneWhitelist(), event.getPhone());

                    if (blacklisted) {
                        menu.removeItem(R.id.action_add_to_blacklist);
                    }

                    if (whitelisted) {
                        menu.removeItem(R.id.action_add_to_whitelist);
                    }

                    if (!blacklisted && !whitelisted) {
                        menu.removeItem(R.id.action_remove_from_lists);
                    }
                });

                markAsRead(event);
            }
        }

        private void markAsRead(PhoneEvent event) {
            if (!event.isRead()) {
                event.setRead(true);
                database.putEvent(event);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        PhoneEvent getItem(int position) {
            return items.get(position);
        }

        private CharSequence formatSummary(PhoneEvent event) {
            if (event.isSms()) {
                return event.getText();
            } else if (event.isMissed()) {
                return getString(R.string.missed_call);
            } else {
                int pattern = R.string.call_of_duration_short;
                return formatter(requireContext())
                        .pattern(pattern)
                        .put("duration", formatDuration(event.getCallDuration()))
                        .format();
            }
        }

    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView typeView;
        private final ImageView directionView;
        private final TextView phoneView;
        private final TextView timeView;
        private final TextView textView;
        private final ImageView stateView;

        private ItemViewHolder(View view) {
            super(view);
            timeView = view.findViewById(R.id.list_item_time);
            textView = view.findViewById(R.id.list_item_text);
            typeView = view.findViewById(R.id.list_item_type);
            directionView = view.findViewById(R.id.list_item_direction);
            phoneView = view.findViewById(R.id.list_item_phone);
            stateView = view.findViewById(R.id.list_item_state);
        }
    }

    private class SettingsListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PREF_EMAIL_TRIGGERS:
                case PREF_FILTER_PHONE_BLACKLIST:
                case PREF_FILTER_PHONE_WHITELIST:
                case PREF_FILTER_TEXT_BLACKLIST:
                case PREF_FILTER_TEXT_WHITELIST:
                    reloadItems();
            }
        }
    }

}
