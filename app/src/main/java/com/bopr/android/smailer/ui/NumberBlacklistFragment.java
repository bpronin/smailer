package com.bopr.android.smailer.ui;

import android.support.annotation.NonNull;

import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Number blacklist activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class NumberBlacklistFragment extends FilterListFragment {

    @Override
    Set<String> getItemsList(PhoneEventFilter filter) {
        return filter.getPhoneBlacklist();
    }

    @Override
    void setItemsList(PhoneEventFilter filter, List<String> list) {
        filter.setPhoneBlacklist(new HashSet<>(list));
    }

    @NonNull
    @Override
    EditFilterListItemDialogFragment createEditItemDialog(String text) {
        EditPhoneDialogFragment dialog = new EditPhoneDialogFragment();
        dialog.setTitle(text == null ? R.string.title_add : R.string.title_edit);
        dialog.setInitialValue(text);
        return dialog;
    }

    @Override
    String getItemText(String text) {
        //return normalizePhone(text);
        return text;
    }

}
