package com.bopr.android.smailer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.TextUtil.isNullOrBlank;
import static com.bopr.android.smailer.util.UiUtil.showToast;
import static java.lang.String.valueOf;

/**
 * Base for black/whitelist fragments.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
abstract class CallFilterListFragment extends BaseFragment {

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
        addButton.setOnClickListener(v -> addItem());

        reloadItems();

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_log_clear) {
            clearData();
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    abstract Set<String> getItemsList(@NonNull PhoneEventFilter filter);

    abstract void setItemsList(@NonNull PhoneEventFilter filter, @NonNull List<String> list);

    @NonNull
    abstract EditFilterListItemDialogFragment createEditItemDialog(@Nullable String text);

    @Nullable
    abstract String getItemText(@Nullable String value);

    void reloadItems() {
        listAdapter = new ListAdapter();
        listAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                updateEmptyText();
            }
        });

        listView.setAdapter(listAdapter);

        List<String> values = new ArrayList<>(getItemsList(settings.getFilter()));
        Collections.sort(values);

        List<String> items = new ArrayList<>(values);
        listAdapter.setItems(items);
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

    private void clearData() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.ask_clear_list)
                .setPositiveButton(R.string.clear, (dialog, which) -> {
                    listAdapter.setItems(ImmutableList.of());
                    persistItems();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    private void persistItems() {
        List<String> items = new ArrayList<>(listAdapter.getItems());

        PhoneEventFilter filter = settings.getFilter();
        setItemsList(filter, items);
        settings.edit().putFilter(filter).apply();
    }

    private boolean isItemExists(String text) {
        for (String item : listAdapter.getItems()) {
            if (text.equals(item)) {
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
            String item = listAdapter.getItem(selectedListPosition);
            editItem(item);
        }
    }

    private void editItem(final String item) {
        EditFilterListItemDialogFragment dialog = createEditItemDialog(item);
        dialog.setOnClose(value -> {
            if (isItemExists(value) && (item == null || !item.equals(value))) {
                showToast(requireContext(), formatter(requireContext())
                        .pattern(R.string.item_already_exists)
                        .put("item", getItemText(value))
                        .format());
            } else if (!isNullOrBlank(getItemText(value))) {
                listAdapter.replaceItem(item, value);
                persistItems();
            }
        });

        dialog.showDialog(requireActivity());
    }

    private void removeSelectedItem() {
        if (selectedListPosition != NO_POSITION) {
            List<String> savedItems = new ArrayList<>(listAdapter.getItems());
            List<String> removedItems = listAdapter.removeItems(new int[]{selectedListPosition});
            persistItems();
            showUndoAction(removedItems, savedItems);
        }
    }

    private void undoRemoveItem(List<String> lastItems) {
        listAdapter.setItems(lastItems);
        persistItems();
    }

    private void showUndoAction(List<String> removedItems, final List<String> lastItems) {
        String title;
        if (removedItems.size() == 1) {
            title = getString(R.string.item_removed);
        } else {
            title = formatter(requireContext())
                    .pattern(R.string.items_removed)
                    .put("count", valueOf(removedItems.size()))
                    .format();
        }

        Snackbar.make(listView, title, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .setAction(R.string.undo, v -> undoRemoveItem(lastItems))
                .show();
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private final List<String> items = new ArrayList<>();

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_filter, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
            String item = getItem(position);
            holder.textView.setText(item != null ? getItemText(item) : null);

            holder.itemView.setOnClickListener(v -> {
                selectedListPosition = holder.getAdapterPosition();
                editSelectedItem();
            });
            holder.itemView.setOnLongClickListener(v -> {
                selectedListPosition = holder.getAdapterPosition();
                return false;
            });
            holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) ->
                    requireActivity().getMenuInflater().inflate(R.menu.menu_context_filters, menu));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String getItem(int position) {
            return position != -1 ? items.get(position) : null;
        }

        private List<String> getItems() {
            return Collections.unmodifiableList(items);
        }

        private void setItems(List<String> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }

        private List<String> removeItems(int[] positions) {
            List<String> removedItems = new ArrayList<>();
            for (int position : positions) {
                String item = getItem(position);
                removedItems.add(item);
                items.remove(item);
            }
            notifyDataSetChanged();
            return removedItems;
        }

        private void replaceItem(String oldItem, String newItem) {
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