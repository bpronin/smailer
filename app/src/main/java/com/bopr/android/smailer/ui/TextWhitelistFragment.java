package com.bopr.android.smailer.ui;

import com.bopr.android.smailer.PhoneEventFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Text whitelist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TextWhitelistFragment extends TextFilterListFragment {

    @Override
    Set<String> getItemsList(PhoneEventFilter filter) {
        return filter.getTextWhitelist();
    }

    @Override
    void setItemsList(PhoneEventFilter filter, List<String> list) {
        filter.setTextWhitelist(new HashSet<>(list));
    }
}
