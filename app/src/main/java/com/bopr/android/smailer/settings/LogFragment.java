package com.bopr.android.smailer.settings;


import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.R;

/**
 * Application activity log activity fragment.
 */
public class LogFragment extends ListFragment {

    private Database database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        database = new Database(getActivity());
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getResources().getString(R.string.activity_log_empty_log));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_log, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log_refresh) {
            refreshData();
        } else if (item.getItemId() == R.id.action_log_clear) {
            clearData();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        String details = database.getMessageDetails(id);
        if (details != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.activity_log_title_details)
                    .setMessage(details)
                    .show();
        }
    }

    private void refreshData() {
        setListAdapter(new LogListAdapter(getActivity(), database.getMessages()));
    }

    private void clearData() {
        new AlertDialog.Builder(getActivity())
                .setMessage("Clear log?")

                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.clear();
                        refreshData();
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

}
