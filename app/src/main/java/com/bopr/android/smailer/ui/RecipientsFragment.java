package com.bopr.android.smailer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.commaSeparated;
import static java.lang.String.valueOf;


/**
 * Recipients list activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RecipientsFragment extends BaseFragment {

    private ListAdapter listAdapter;
    private RecyclerView listView;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipients, container, false);

        listView = view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int swipeDir) {
                removeItems(new int[]{holder.getAdapterPosition()});
            }

        });
        itemTouchHelper.attachToRecyclerView(listView);

        FloatingActionButton addButton = view.findViewById(R.id.button_add);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addItem();
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
        if (view != null && listView.getAdapter() != null) {
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

        List<Item> items = new ArrayList<>();
        List<String> addresses = Util.parseSeparated(preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), ",", true);
        for (String address : addresses) {
            items.add(new Item(address));
        }
        listAdapter.setItems(items);
    }

    private void persistItems() {
        List<String> addresses = new ArrayList<>();
        for (Item item : listAdapter.getItems()) {
            addresses.add(item.address);
        }

        preferences.edit()
                .putString(KEY_PREF_RECIPIENTS_ADDRESS, commaSeparated(addresses))
                .apply();
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

    private boolean isItemExists(String address) {
        for (Item item : listAdapter.getItems()) {
            if (item.address.equals(address)) {
                return true;
            }
        }
        return false;
    }

    private void showItemEditor(final Item item) {
        EditEmailDialogFragment dialog = new EditEmailDialogFragment();
        dialog.setTitle(item == null ? R.string.title_add : R.string.title_edit);
        dialog.setInitialValue(item == null ? null : item.address);
        dialog.setCallback(new EditEmailDialogFragment.Callback() {

            @Override
            public void onOkClick(String address) {
                if (isItemExists(address) && (item == null || !item.address.equals(address))) {
                    Toast.makeText(getContext(), formatter(R.string.message_recipient_already_exists, getResources())
                            .put("name", address)
                            .format(), Toast.LENGTH_LONG).show();
                } else if (!Util.isTrimEmpty(address)) {
                    /* note: if we rotated device reference to "this" is changed here */
                    Item newItem = new Item(address);
                    listAdapter.replaceItem(item, newItem);
                    persistItems();
                }
            }
        });

        dialog.showDialog(getActivity());
    }

    private void showUndoAction(List<Item> removedItems, final List<Item> lastItems) {
        String title;
        if (removedItems.size() == 1) {
            title = getString(R.string.message_item_removed);
        } else {
            title = formatter(R.string.message_items_removed, getResources())
                    .put("count", valueOf(removedItems.size()))
                    .format();
        }

        Snackbar.make(listView, title, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(getContext(), R.color.dialogButtonText))
                .setAction(R.string.title_undo, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        undoRemove(lastItems);
                    }
                })
                .show();
    }

    private class Item {

        private final String address;

        private Item(String address) {
            this.address = address;
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private final List<Item> items = new ArrayList<>();

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_recipient, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
            final Item item = getItem(position);
            holder.textView.setText(item != null ? item.address : null);
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
