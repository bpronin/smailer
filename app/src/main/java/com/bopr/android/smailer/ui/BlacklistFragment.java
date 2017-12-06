package com.bopr.android.smailer.ui;

import com.bopr.android.smailer.PhoneEventFilter;

import java.util.List;
import java.util.Set;

/**
 * Blacklist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class BlacklistFragment extends PhonesFragment {

    @Override
    Set<String> getPhoneList(PhoneEventFilter filter) {
        return filter.getBlacklist();
    }

    @Override
    void setPhoneList(PhoneEventFilter filter, List<String> list) {
        filter.setBlacklist(list);
    }
}
