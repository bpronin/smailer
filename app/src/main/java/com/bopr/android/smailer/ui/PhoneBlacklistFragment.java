package com.bopr.android.smailer.ui;

import com.bopr.android.smailer.PhoneEventFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Phone number blacklist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class PhoneBlacklistFragment extends PhoneFilterListFragment {

    @Override
    Set<String> getItemsList(PhoneEventFilter filter) {
        return filter.getPhoneBlacklist();
    }

    @Override
    void setItemsList(PhoneEventFilter filter, List<String> list) {
        filter.setPhoneBlacklist(new HashSet<>(list));
    }

}
