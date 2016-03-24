package com.bopr.android.smailer.ui;


import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.R;
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
                        removeItem(reverseSortedPositions);
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
    public void onStart() {
        super.onStart();
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
                return view;
            }
        };
        return listAdapter;
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
        List<String> items = new ArrayList<>();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            items.add(listAdapter.getItem(i));
        }

        preferences.edit().putString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, Util.stringOf(", ", items)).apply();
    }

    private void addItem() {
        showItemEditor(null, new ItemEditorListener() {

            @Override
            public void onOkClick(String oldValue, String newValue) {
                if (isItemExists(newValue)) {
                    Toast.makeText(getActivity(), R.string.message_recipient_already_exists, Toast.LENGTH_LONG).show();
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

    private void removeItem(int[] positions) {
        for (int position : positions) {
            listAdapter.remove(listAdapter.getItem(position));
        }
        persistItems();
        listAdapter.notifyDataSetChanged();
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

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_dialog_title_recipient)
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
                .show();
    }

    private interface ItemEditorListener {

        void onOkClick(String oldValue, String newValue);
    }

}
