package com.bopr.android.smailer.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.TextUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bopr.android.smailer.Settings.PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.util.AddressUtil.isValidEmailAddress;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.UiUtil.showToast;
import static com.bopr.android.smailer.util.UiUtil.underwivedText;
import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;


/**
 * Recipients list activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RecipientsFragment extends BaseFragment {

    private ListAdapter listAdapter;
    private RecyclerView listView;
    private SettingsListener settingsListener;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        settingsListener = settings.registerChangeListener(new SettingsListener());
    }

    @Override
    public void onDestroy() {
        settings.unregisterChangeListener(settingsListener);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipients, container, false);

        listView = view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));

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
        addButton.setOnClickListener(v -> addItem());

        loadItems();

        return view;
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        view.post(new Runnable() {
//
//            @Override
//            public void run() {
//                addButton.show();
//            }
//        });
//    }

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
        List<String> addresses = settings.getCommaList(PREF_RECIPIENTS_ADDRESS);
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

        settings.edit()
                .putCommaSet(PREF_RECIPIENTS_ADDRESS, addresses)
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
        dialog.setTitle(item == null ? R.string.add : R.string.edit);
        dialog.setInitialValue(item == null ? null : item.address);
        dialog.setCallback(address -> {
            Log.d("", "onOkClick: ");
            if (isItemExists(address) && (item == null || !item.address.equals(address))) {
                showToast(requireContext(), formatter(requireContext())
                        .pattern(R.string.recipient_already_exists)
                        .put("name", address)
                        .format());
            } else if (!TextUtil.isNullOrBlank(address)) {
                /* note: if we rotated device reference to "this" is changed here */
                Item newItem = new Item(address);
                listAdapter.replaceItem(item, newItem);
                persistItems();
            }
        });

        dialog.showDialog(requireActivity());
    }

    private void showUndoAction(List<Item> removedItems, final List<Item> lastItems) {
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
                .setActionTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                .setAction(R.string.undo, v -> undoRemove(lastItems))
                .show();
    }

    private class Item {
        // TODO: 22.02.2020 remove it/ make string
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
            final Item item = requireItem(position);

            String address = item.address;
            holder.textView.setText(isValidEmailAddress(address)
                    ? address :
                    underwivedText(requireContext(), address));

            holder.itemView.setOnClickListener(v -> editItem(item));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Nullable
        private Item getItem(int position) {
            return position != -1 ? items.get(position) : null;
        }

        @NonNull
        private Item requireItem(int position) {
            return requireNonNull(getItem(position));
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

    private class SettingsListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Settings.PREF_RECIPIENTS_ADDRESS)) {
                loadItems();
            }

        }
    }

}
