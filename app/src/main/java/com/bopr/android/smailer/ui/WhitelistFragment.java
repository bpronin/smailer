package com.bopr.android.smailer.ui;

import com.bopr.android.smailer.PhoneEventFilter;

import java.util.List;
import java.util.Set;

/**
 * Whitelist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class WhitelistFragment extends PhonesFragment {

    @Override
    Set<String> getPhoneList(PhoneEventFilter filter) {
        return filter.getWhitelist();
    }

    @Override
    void setPhoneList(PhoneEventFilter filter, List<String> list) {
        filter.setWhitelist(list);
    }
}
