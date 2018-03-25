package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bopr.android.smailer.R;

import static com.bopr.android.smailer.Settings.*;

/**
 * Created by bo on 25.03.2018.
 */

public class MainFragmentLarge extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_master_detail, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        MainFragment fragment = (MainFragment) fragmentManager.findFragmentByTag("master");
        if (fragment == null) {
            fragment = new MainFragment();
            fragment.setPreferenceClickListener(new PreferenceClickListener());
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.master_content, fragment, "master")
                    .commit();
        }

        return view;
    }

    private void setDetailFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_content, fragment, "detail")
                .commit();
    }

    private class PreferenceClickListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_PREF_OUTGOING_SERVER:
                    setDetailFragment(new ServerFragment());
                    break;
                case KEY_PREF_RECIPIENTS_ADDRESS:
                    setDetailFragment(new RecipientsFragment());
                    break;
                case KEY_PREF_MORE:
                    setDetailFragment(new MoreFragment());
                    break;
                case KEY_PREF_RULES:
                    setDetailFragment(new RulesFragment());
                    break;
                case KEY_PREF_LOG:
                    setDetailFragment(new LogFragment());
                    break;
            }
            return true;
        }
    }

}
