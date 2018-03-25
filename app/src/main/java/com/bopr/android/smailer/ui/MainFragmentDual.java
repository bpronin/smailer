package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.bopr.android.smailer.R;

import java.util.HashMap;
import java.util.Map;

import static com.bopr.android.smailer.Settings.*;

/**
 * Main settings fragment (dual pane layout)
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainFragmentDual extends Fragment {

    private Map<String, Fragment> detailFragments = new HashMap<>();
    private MainFragment masterFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_master_detail, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        masterFragment = (MainFragment) fragmentManager.findFragmentByTag("master");
        if (masterFragment == null) {
            masterFragment = new MainFragment();
            masterFragment.setPreferenceClickListener(new PreferenceClickListener());
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.master_content, masterFragment, "master")
                    .commit();
        }

        selectDetails(KEY_PREF_OUTGOING_SERVER);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        ListView listView = masterFragment.getView().findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        /* listView.getSelectedItem() is not null at startup but null when fragment's activity restored */
        if (listView.getSelectedItem() != null) {
            listView.setItemChecked(listView.getSelectedItemPosition(), true);
        }
    }

    private void selectDetails(String key) {
        Fragment fragment = detailFragments.get(key);
        if (fragment == null) {
            switch (key) {
                case KEY_PREF_OUTGOING_SERVER:
                    fragment = new ServerFragment();
                    break;
                case KEY_PREF_RECIPIENTS_ADDRESS:
                    fragment = new RecipientsFragment();
                    break;
                case KEY_PREF_MORE:
                    fragment = new MoreFragment();
                    break;
                case KEY_PREF_RULES:
                    fragment = new RulesFragment();
                    break;
                case KEY_PREF_LOG:
                    fragment = new LogFragment();
                    break;
            }
            detailFragments.put(key, fragment);
        }

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_content, fragment, "detail")
                .commit();
    }

    private class PreferenceClickListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            selectDetails(preference.getKey());
            return true;
        }
    }

}
