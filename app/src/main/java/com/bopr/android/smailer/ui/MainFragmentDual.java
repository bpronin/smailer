package com.bopr.android.smailer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bopr.android.smailer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;

import static com.bopr.android.smailer.Settings.KEY_PREF_LOG;
import static com.bopr.android.smailer.Settings.KEY_PREF_MORE;
import static com.bopr.android.smailer.Settings.KEY_PREF_OUTGOING_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RULES;

/**
 * Main settings fragment (dual pane layout)
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainFragmentDual extends BaseFragment {

    private Map<String, Fragment> detailFragments = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_master_detail, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        MainFragment fragment = (MainFragment) fragmentManager.findFragmentByTag("master");
        if (fragment == null) {
            fragment = new MainFragment();
            fragment.setAsListView(true);
            fragment.setPreferenceClickListener(new PreferenceClickListener());
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.master_content, fragment, "master")
                    .commit();
        }

        selectDetails(KEY_PREF_OUTGOING_SERVER);

        return view;
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
                    fragment = new HistoryFragment();
                    break;
                default:
                    throw new RuntimeException("Unregistered fragment");
            }
            detailFragments.put(key, fragment);
        }

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_content, fragment, "detail")
                .commit();
    }

    private class PreferenceClickListener implements OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            selectDetails(preference.getKey());
            return true;
        }
    }

}
