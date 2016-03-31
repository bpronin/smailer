package com.bopr.android.smailer.ui;


import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import static com.bopr.android.smailer.Settings.KEY_PREF_AVAILABLE_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.util.TagFormatter.from;

/**
 * Application activity log activity fragment.
 */
public class RecipientsFragment extends ListFragment {

    private SharedPreferences preferences;
    private RecipientListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipients, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
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

        view.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItems();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        editItem(position);
    }

    private void loadItems() {
        List<String> availableAddresses = new ArrayList<>();
        List<String> enabledAddresses = new ArrayList<>();

        String enabledPref = preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, "");
        enabledAddresses.addAll(Util.listOf(enabledPref, ",", true));

        String availablePref = preferences.getString(KEY_PREF_AVAILABLE_RECIPIENTS_ADDRESS, "");
        availableAddresses.addAll(Util.listOf(availablePref, ",", true));

        /* add lost items */
        for (String address : enabledAddresses) {
            if (!availableAddresses.contains(address)) {
                availableAddresses.add(address);
            }
        }

        listAdapter = new RecipientListAdapter();
        for (String address : availableAddresses) {
            listAdapter.addItem(new Item(address, enabledAddresses.contains(address)));
        }

        setListAdapter(listAdapter);
    }

    private void persistItems() {
        List<String> availableAddresses = new ArrayList<>();
        List<String> enabledAddresses = new ArrayList<>();
        for (Item item : listAdapter.getItems()) {
            String address = item.address;
            availableAddresses.add(address);
            if (item.enabled) {
                enabledAddresses.add(address);
            }
        }

        preferences.edit()
                .putString(KEY_PREF_AVAILABLE_RECIPIENTS_ADDRESS, Util.stringOf(", ", availableAddresses))
                .putString(KEY_PREF_RECIPIENTS_ADDRESS, Util.stringOf(", ", enabledAddresses))
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

    private void enableItem(int position, boolean enabled) {
        Item item = listAdapter.getItem(position);
        listAdapter.replaceItem(position, new Item(item.address, enabled));
        listAdapter.notifyDataSetChanged();
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
                    Item newItem = new Item(address, item == null || item.enabled);
                    listAdapter.replaceItem(position, newItem);
                    listAdapter.notifyDataSetChanged();

                  //  getListView().invalidateViews(); //todo: NPE when rotating during edit

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

        Snackbar.make(getListView(), title, Snackbar.LENGTH_LONG)
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

        private final boolean enabled;
        private final String address;

        public Item(String address, boolean enabled) {
            this.address = address;
            this.enabled = enabled;
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
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_item_recipient, parent, false);
            } else {
                view = convertView;
            }

            final Item item = getItem(position);

            final TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(AndroidUtil.validatedText(getActivity(), item.address,
                    EmailTextValidator.isValidValue(item.address)));
            textView.setEnabled(item.enabled);

            CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box);
            checkBox.setChecked(item.enabled);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    enableItem(position, isChecked);
                }
            });

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
