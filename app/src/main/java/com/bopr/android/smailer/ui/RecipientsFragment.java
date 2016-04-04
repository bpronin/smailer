package com.bopr.android.smailer.ui;

import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.Util;
import com.bopr.android.smailer.util.ui.swipedismiss.SwipeDismissListViewTouchListener;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.util.TagFormatter.from;

/**
 * Recipients list fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RecipientsFragment extends ListFragment {

    private RecipientListAdapter listAdapter;
    private ListView listView;
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
        View view = inflater.inflate(R.layout.fragment_recipients, container, false);

        listView = (ListView) view.findViewById(android.R.id.list);
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {

                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        removeItems(reverseSortedPositions);
                    }
                }
        );
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());

        addButton = (FloatingActionButton) view.findViewById(R.id.button_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        loadItems();

        return view;
    }

    public void onShow() {
        addButton.show();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        editItem(position);
    }

    private void loadItems() {
        listAdapter = new RecipientListAdapter();
        List<String> addresses = Util.listOf(preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), ",", true);
        for (String address : addresses) {
            listAdapter.addItem(new Item(address));
        }
        setListAdapter(listAdapter);
    }

    private void persistItems() {
        List<String> addresses = new ArrayList<>();
        for (Item item : listAdapter.getItems()) {
            addresses.add(item.address);
        }

        preferences.edit()
                .putString(KEY_PREF_RECIPIENTS_ADDRESS, Util.stringOf(", ", addresses))
                .apply();
    }

    private void addItem() {
        showItemEditor(-1);
    }

    private void editItem(int position) {
        if (position != -1) {
            showItemEditor(position);
        }
    }

    private void removeItems(int[] positions) {
        List<Item> savedItems = new ArrayList<>(listAdapter.getItems());
        List<Item> removedItems = new ArrayList<>();

        for (int position : positions) {
            Item item = listAdapter.getItem(position);
            listAdapter.removeItem(item);
            removedItems.add(item);
        }

        persistItems();
        listAdapter.notifyDataSetChanged();

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

    private void showItemEditor(final int position) {
        final Item item = listAdapter.getItem(position);

        EditEmailDialogFragment dialog = new EditEmailDialogFragment();
        dialog.setTitle(item == null ? R.string.pref_dialog_title_add_recipient : R.string.pref_dialog_title_edit_recipient);
        dialog.setInitialValue(item == null ? null : item.address);
        dialog.setCallback(new EditEmailDialogFragment.Callback() {

            @Override
            public void onOkClick(String address) {
                if (isItemExists(address) && (item == null || !item.address.equals(address))) {
                    Toast.makeText(getActivity(), from(R.string.recipient_message_already_exists, getResources())
                            .put("name", address)
                            .format(), Toast.LENGTH_LONG).show();
                } else if (!Util.isTrimEmpty(address)) {
                    /* note: if we rotated device reference to "this" is changed here */
                    Item newItem = new Item(address);
                    listAdapter.replaceItem(position, newItem);
                    listAdapter.notifyDataSetChanged();
                    persistItems();
                }
            }
        });

        dialog.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "edit_recipient_dialog");
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

        private final String address;

        public Item(String address) {
            this.address = address;
        }
    }

    private class RecipientListAdapter extends BaseAdapter {

        private final LayoutInflater inflater;
        private final List<Item> items = new ArrayList<>();

        public RecipientListAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_recipient, parent, false);
            }

            Item item = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(AndroidUtil.validatedUnderlinedText(getActivity(), item.address,
                    EmailTextValidator.isValidValue(item.address)));

            return view;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return position != -1 ? items.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public List<Item> getItems() {
            return Collections.unmodifiableList(items);
        }

        public void setItems(List<Item> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }

        public void removeItem(Item item) {
            items.remove(item);
        }

        public void addItem(Item item) {
            items.add(item);
        }

        public void replaceItem(int position, Item item) {
            if (position < 0) {
                items.add(item);
            } else {
                items.set(position, item);
            }
        }

    }

}
