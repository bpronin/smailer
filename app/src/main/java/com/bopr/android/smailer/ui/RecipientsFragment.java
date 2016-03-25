package com.bopr.android.smailer.ui;


import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.Util;
import com.bopr.android.smailer.util.ui.swipedismiss.SwipeDismissListViewTouchListener;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import java.util.ArrayList;
import java.util.List;

import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.Settings.getPreferences;

/**
 * Application activity log activity fragment.
 */
public class RecipientsFragment extends ListFragment {

    private SharedPreferences preferences;
    private ArrayAdapter<String> listAdapter;

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
        editItem((String) listView.getItemAtPosition(position));
    }

    private ListAdapter createListAdapter(List<String> items) {
        listAdapter = new ArrayAdapter<String>(RecipientsFragment.this.getActivity(), R.layout.list_item_recipient, R.id.text, items) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(R.id.text);
                String text = textView.getText().toString();
                textView.setText(Util.validatedText(getActivity(), text, EmailTextValidator.isValidValue(text)));

//                CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box);
//                checkBox.setChecked();
//                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        updateItemEbabled(getItem(position), isChecked);
//                    }
//                });
                return view;
            }
        };
        return listAdapter;
    }

    @NonNull
    private List<String> getItems() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            items.add(listAdapter.getItem(i));
        }
        return items;
    }

    private void loadItems() {
        String preference = preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, null);

        List<String> items = new ArrayList<>();
        if (preference != null) {
            items.addAll(Util.listOf(preference, ",", true));
        }

        setListAdapter(createListAdapter(items));
    }

    private void persistItems() {
        preferences.edit()
                .putString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, Util.stringOf(", ", getItems()))
                .apply();
    }

    private void addItem() {
        showItemEditor(null, new ItemEditorListener() {

            @Override
            public void onOkClick(String oldValue, String newValue) {
                if (isItemExists(newValue)) {
                    String message = TagFormatter.from(R.string.message_recipient_already_exists, getResources())
                            .put("name", newValue)
                            .format();
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                } else if (!Util.isTrimEmpty(newValue)) {
                    listAdapter.add(newValue);
                    persistItems();
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void editItem(String item) {
        if (item != null) {
            showItemEditor(item, new ItemEditorListener() {

                @Override
                public void onOkClick(String oldValue, String newValue) {
                    if (!Util.isTrimEmpty(newValue)) {
                        listAdapter.remove(oldValue);
                        listAdapter.add(newValue);
                        persistItems();
                        listAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void removeItems(int[] positions) {
        List<String> lastItems = getItems();
        List<String> removedItems = new ArrayList<>();

        for (int position : positions) {
            String item = listAdapter.getItem(position);
            listAdapter.remove(item);
            removedItems.add(item);
        }

        persistItems();
        listAdapter.notifyDataSetChanged();

        showUndoAction(removedItems, lastItems);
    }

    private void undoRemove(List<String> lastItems) {
        setListAdapter(createListAdapter(lastItems));
        persistItems();
    }

    private boolean isItemExists(String value) {
        for (int i = 0; i < listAdapter.getCount(); i++) {
            if (listAdapter.getItem(i).equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void showItemEditor(final String initialValue, final ItemEditorListener listener) {
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.addTextChangedListener(new EmailTextValidator(editText));
        editText.setSelectAllOnFocus(true);
        editText.setText(initialValue);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(initialValue == null ? R.string.pref_dialog_title_add_recipient : R.string.pref_dialog_title_edit_recipient)
                .setMessage(R.string.pref_dialog_message_recipient)
                .setView(editText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onOkClick(initialValue, editText.getText().toString());
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

    private void showUndoAction(List<String> removedItems, final List<String> lastItems) {
        String title;
        if (removedItems.size() == 1) {
            title = getString(R.string.message_item_removed);
        } else {
            title = TagFormatter.from(R.string.message_items_removed, getResources())
                    .put("count", removedItems.size())
                    .format();
        }

        Snackbar.make(getListView(), title, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        undoRemove(lastItems);
                    }
                })
                .show();
    }

    private interface ItemEditorListener {

        void onOkClick(String oldValue, String newValue);
    }

}
