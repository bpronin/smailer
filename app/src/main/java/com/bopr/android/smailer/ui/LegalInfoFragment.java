package com.bopr.android.smailer.ui;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bopr.android.smailer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Legal info fragment. Displays list of used open source libs.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LegalInfoFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        List<Item> data = new ArrayList<>();
        for (String line : getResources().getStringArray(R.array.open_source)) {
            String[] s = line.split("\\|");
            data.add(new Item(s[0], Uri.parse(s[1])));
        }
        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, data));

        return inflater.inflate(android.R.layout.list_content, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item item = (Item) getListAdapter().getItem(position);
        startActivity(new Intent(Intent.ACTION_VIEW, item.url));
    }

    private class Item {

        private final String name;
        private final Uri url;

        public Item(String name, Uri url) {
            this.name = name;
            this.url = url;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
