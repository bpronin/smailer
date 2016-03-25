package com.bopr.android.smailer.ui;


import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.R;
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
        showItemEditor(null);
    }

    private void editItem(int position) {
        Item item = listAdapter.getItem(position);
        if (item != null) {
            showItemEditor(item);
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
        listAdapter.replaceItem(item, new Item(item.address, enabled));
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

    private void showItemEditor(final Item item) {
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.addTextChangedListener(new EmailTextValidator(editText));
        editText.setSelectAllOnFocus(true);
        if (item != null) {
            editText.setText(item.address);
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(item == null ? R.string.pref_dialog_title_add_recipient : R.string.pref_dialog_title_edit_recipient)
                .setMessage(R.string.pref_dialog_message_recipient)
                .setView(editText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = editText.getText().toString();
                        if (isItemExists(address)) {
                            Toast.makeText(getActivity(), from(R.string.message_recipient_already_exists, getResources())
                                    .put("name", address)
                                    .format(), Toast.LENGTH_LONG).show();
                        } else if (!Util.isTrimEmpty(address)) {
                            Item newItem = new Item(address, item == null || item.enabled);
                            listAdapter.replaceItem(item, newItem);
                            listAdapter.notifyDataSetChanged();
                            persistItems();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        /* this is to show soft keyboard when dialog is open */
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
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
                .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.snackBarButtonText))
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
            textView.setText(Util.validatedText(getActivity(), item.address,
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
            return items.get(position);
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

        public void replaceItem(Item oldItem, Item newItem) {
            int position = items.indexOf(oldItem);
            if (position < 0) {
                items.add(newItem);
            } else {
                items.set(position, newItem);
            }
        }
    }

}
