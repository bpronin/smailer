package com.bopr.android.smailer.ui;

import com.bopr.android.smailer.PhoneEventFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Phone number whitelist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class PhoneWhitelistFragment extends PhoneFilterListFragment {

    @Override
    Set<String> getItemsList(PhoneEventFilter filter) {
        return filter.getPhoneWhitelist();
    }

    @Override
    void setItemsList(PhoneEventFilter filter, List<String> list) {
        filter.setPhoneWhitelist(new HashSet<>(list));
    }

}
