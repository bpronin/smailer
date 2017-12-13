package com.bopr.android.smailer.ui;

import com.bopr.android.smailer.PhoneEventFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Text blacklist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TextBlacklistFragment extends TextFilterListFragment {

    @Override
    Set<String> getItemsList(PhoneEventFilter filter) {
        return filter.getTextBlacklist();
    }

    @Override
    void setItemsList(PhoneEventFilter filter, List<String> list) {
        filter.setTextBlacklist(new HashSet<>(list));
    }

}
