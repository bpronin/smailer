package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.util.TagFormatter.from;
import static com.bopr.android.smailer.util.Util.normalizePhone;

/**
 * Blacklist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class BlacklistFragment extends Fragment {

    private ListAdapter listAdapter;
    private RecyclerView listView;
    private FloatingActionButton addButton;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getPreferences(getActivity());
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                loadItems();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);

        listView = view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder holder, int swipeDir) {
                removeItems(new int[]{holder.getAdapterPosition()});
            }

        });
        itemTouchHelper.attachToRecyclerView(listView);

        addButton = view.findViewById(R.id.button_add);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        view.post(new Runnable() {

            @Override
            public void run() {
                addButton.show();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems();
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

    private void loadItems() {
        listAdapter = new ListAdapter();
        listAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                updateEmptyText();
            }
        });

        listView.setAdapter(listAdapter);

        List<String> blacklist = new ArrayList<>(Settings.loadFilter(getActivity()).getBlacklist());
        Collections.sort(blacklist);

        List<Item> items = new ArrayList<>();
        for (String address : blacklist) {
            items.add(new Item(address));
        }
        listAdapter.setItems(items);
    }

    private void persistItems() {
        List<String> addresses = new ArrayList<>();
        for (Item item : listAdapter.getItems()) {
            addresses.add(item.phone);
        }

        PhoneEventFilter filter = Settings.loadFilter(getActivity());
        filter.setBlacklist(addresses);
        Settings.saveFilter(getActivity(), filter);
    }

    private void addItem() {
        showItemEditor(null);
    }

    private void editItem(Item item) {
        if (item != null) {
            showItemEditor(item);
        }
    }

    private void removeItems(int[] positions) {
        List<Item> savedItems = new ArrayList<>(listAdapter.getItems());
        List<Item> removedItems = listAdapter.removeItems(positions);
        persistItems();
        showUndoAction(removedItems, savedItems);
    }

    private void undoRemove(List<Item> lastItems) {
        listAdapter.setItems(lastItems);
        persistItems();
    }

    private boolean isItemExists(String phone) {
        String p = normalizePhone(phone);
        for (Item item : listAdapter.getItems()) {
            if (p.equals(normalizePhone(item.phone))) {
                return true;
            }
        }
        return false;
    }

    private void showItemEditor(final Item item) {
        EditPhoneDialogFragment dialog = new EditPhoneDialogFragment();
        dialog.setTitle(item == null ? R.string.pref_dialog_title_add_phone : R.string.pref_dialog_title_edit_phone);
        dialog.setInitialValue(item == null ? null : item.phone);
        dialog.setCallback(new EditPhoneDialogFragment.Callback() {

            @Override
            public void onOkClick(String number) {
                if (isItemExists(number) && (item == null || !item.phone.equals(number))) {
                    Toast.makeText(getActivity(), from(R.string.list_item_already_exists, getResources())
                            .put("item", number)
                            .format(), Toast.LENGTH_LONG).show();
                } else if (!Util.isTrimEmpty(number)) {
                    /* note: if we rotated device reference to "this" is changed here */
                    Item newItem = new Item(number);
                    listAdapter.replaceItem(item, newItem);
                    persistItems();
                }
            }
        });

        dialog.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "edit_phone_dialog");
    }

    private void showUndoAction(List<Item> removedItems, final List<Item> lastItems) {
        String title;
        if (removedItems.size() == 1) {
            title = getString(R.string.message_item_removed);
        } else {
            title = from(R.string.message_items_removed, getResources())
                    .put("count", removedItems.size())
                    .format();
        }

        Snackbar.make(listView, title, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.dialogButtonText))
                .setAction(R.string.action_undo, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        undoRemove(lastItems);
                    }
                })
                .show();
    }

    private class Item {

        private final String phone;

        private Item(String phone) {
            this.phone = phone;
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private final List<Item> items = new ArrayList<>();

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_blacklist, parent, false));
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, int position) {
            final Item item = getItem(position);
            holder.textView.setText(item.phone);
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    editItem(item);
                }
            });
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private Item getItem(int position) {
            return position != -1 ? items.get(position) : null;
        }

        private List<Item> getItems() {
            return Collections.unmodifiableList(items);
        }

        private void setItems(List<Item> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }

        private List<Item> removeItems(int[] positions) {
            List<Item> removedItems = new ArrayList<>();
            for (int position : positions) {
                Item item = getItem(position);
                removedItems.add(item);
                items.remove(item);
            }
            notifyDataSetChanged();
            return removedItems;
        }

        private void replaceItem(Item oldItem, Item newItem) {
            int position = items.indexOf(oldItem);
            if (position < 0) {
                items.add(newItem);
            } else {
                items.set(position, newItem);
            }
            notifyDataSetChanged();
        }

    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        private ItemViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text);
        }

    }
}
