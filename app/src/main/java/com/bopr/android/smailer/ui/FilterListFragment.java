package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static java.lang.String.valueOf;

/**
 * Base for black/whitelist fragments.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
abstract class FilterListFragment extends Fragment {

    private ListAdapter listAdapter;
    private RecyclerView listView;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private int selectedListPosition = NO_POSITION;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_list, container, false);

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
                selectedListPosition = holder.getAdapterPosition();
                removeSelectedItem();
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

//        view.post(new Runnable() {
//
//            @Override
//            public void run() {
//                addButton.show();
//            }
//        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_item:
                editSelectedItem();
                return true;
            case R.id.action_remove_item:
                removeSelectedItem();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    abstract Set<String> getItemsList(PhoneEventFilter filter);

    abstract void setItemsList(PhoneEventFilter filter, List<String> list);

    @NonNull
    abstract EditFilterListItemDialogFragment createEditItemDialog(String text);

    String getItemText(String text) {
        return text;
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

        List<String> list = new ArrayList<>(getItemsList(Settings.loadFilter(getActivity())));
        Collections.sort(list);

        List<Item> items = new ArrayList<>();
        for (String item : list) {
            items.add(new Item(item));
        }
        listAdapter.setItems(items);
    }

    private void persistItems() {
        List<String> items = new ArrayList<>();
        for (Item item : listAdapter.getItems()) {
            items.add(item.text);
        }

        PhoneEventFilter filter = Settings.loadFilter(getActivity());
        setItemsList(filter, items);
        Settings.saveFilter(getActivity(), filter);
    }

    private boolean isItemExists(String text) {
        String p = getItemText(text);
        for (Item item : listAdapter.getItems()) {
            if (p.equals(getItemText(item.text))) {
                return true;
            }
        }
        return false;
    }

    private void addItem() {
        editItem(null);
    }

    private void editSelectedItem() {
        if (selectedListPosition != NO_POSITION) {
            Item item = listAdapter.getItem(selectedListPosition);
            editItem(item);
        }
    }

    private void editItem(final Item item) {
        EditFilterListItemDialogFragment dialog = createEditItemDialog(item == null ? null : item.text);
        dialog.setCallback(new EditFilterListItemDialogFragment.Callback() {

            @Override
            public void onOkClick(String text) {
                if (isItemExists(text) && (item == null || !item.text.equals(text))) {
                    Toast.makeText(getActivity(), formatter(R.string.message_list_item_already_exists, getResources())
                            .put("item", text)
                            .format(), Toast.LENGTH_LONG).show();
                } else if (!Util.isTrimEmpty(text)) {
                    /* note: if we rotated device reference to "this" is changed here */
                    Item newItem = new Item(text);
                    listAdapter.replaceItem(item, newItem);
                    persistItems();
                }
            }
        });

        dialog.showDialog(((FragmentActivity) getActivity()));
    }

    private void removeSelectedItem() {
        if (selectedListPosition != NO_POSITION) {
            List<Item> savedItems = new ArrayList<>(listAdapter.getItems());
            List<Item> removedItems = listAdapter.removeItems(new int[]{selectedListPosition});
            persistItems();
            showUndoAction(removedItems, savedItems);
        }
    }

    private void undoRemoveItem(List<Item> lastItems) {
        listAdapter.setItems(lastItems);
        persistItems();
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
                .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.dialogButtonText))
                .setAction(R.string.title_undo, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        undoRemoveItem(lastItems);
                    }
                })
                .show();
    }

    private class Item {

        private final String text;

        private Item(String text) {
            this.text = text;
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private final List<Item> items = new ArrayList<>();

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_filter, parent, false));
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, final int position) {
            Item item = getItem(position);
            holder.textView.setText(item != null ? item.text : null);

            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    selectedListPosition = holder.getAdapterPosition();
                    editSelectedItem();
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    selectedListPosition = holder.getAdapterPosition();
                    return false;
                }
            });
            holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    getActivity().getMenuInflater().inflate(R.menu.menu_item_filter_list, menu);
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
