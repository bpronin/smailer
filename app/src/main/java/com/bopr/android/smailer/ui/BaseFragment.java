package com.bopr.android.smailer.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.bopr.android.smailer.ui.AboutDialogFragment.showAboutDialog;

public class BaseFragment extends Fragment {

    Settings settings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(requireContext());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            showAboutDialog(requireActivity());
        }

        return super.onOptionsItemSelected(item);
    }
}
