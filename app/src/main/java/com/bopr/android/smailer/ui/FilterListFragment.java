package com.bopr.android.smailer.ui;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static java.lang.String.valueOf;

/**
 * Base for black/whitelist fragments.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
abstract class FilterListFragment extends BaseFragment {

    private ListAdapter listAdapter;
    private RecyclerView listView;
    private int selectedListPosition = NO_POSITION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_list, container, false);

        listView = view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int swipeDir) {
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

        loadItems();

        return view;
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

    String getItemText(String value) {
        return value;
    }

    private void updateEmptyText() {
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

    void loadItems() {
        listAdapter = new ListAdapter();
        listAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                updateEmptyText();
            }
        });

        listView.setAdapter(listAdapter);

        List<String> list = new ArrayList<>(getItemsList(settings.getFilter()));
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
            items.add(item.value);
        }

        PhoneEventFilter filter = settings.getFilter();
        setItemsList(filter, items);
        settings.putFilter(filter);
    }

    private boolean isItemExists(String text) {
        for (Item item : listAdapter.getItems()) {
            if (text.equals(item.value)) {
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
        EditFilterListItemDialogFragment dialog = createEditItemDialog(item == null ? null : item.value);
        dialog.setCallback(new EditFilterListItemDialogFragment.Callback() {

            @Override
            public void onOkClick(String value) {
                if (isItemExists(value) && (item == null || !item.value.equals(value))) {
                    Toast.makeText(getContext(), formatter(R.string.item_already_exists, requireContext())
                            .put("item", getItemText(value))
                            .format(), Toast.LENGTH_LONG)
                            .show();
                } else if (!Util.isTrimEmpty(value)) {
                    /* note: if we rotated device reference to "this" is changed here */
                    listAdapter.replaceItem(item, new Item(value));
                    persistItems();
                }
            }
        });

        dialog.showDialog(getActivity());
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
            title = getString(R.string.item_removed);
        } else {
            title = formatter(R.string.items_removed, requireContext())
                    .put("count", valueOf(removedItems.size()))
                    .format();
        }

        Snackbar.make(listView, title, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .setAction(R.string.undo, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        undoRemoveItem(lastItems);
                    }
                })
                .show();
    }

    private class Item {

        private final String value;

        private Item(String value) {
            this.value = value;
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private final List<Item> items = new ArrayList<>();

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_filter, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
            Item item = getItem(position);
            holder.textView.setText(item != null ? getItemText(item.value) : null);

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
                    requireActivity().getMenuInflater().inflate(R.menu.menu_context_filters, menu);
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
