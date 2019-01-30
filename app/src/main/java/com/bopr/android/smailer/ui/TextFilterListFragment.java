package com.bopr.android.smailer.ui;

import android.support.annotation.NonNull;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.Util;

/**
 * Text filter list activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
abstract class TextFilterListFragment extends FilterListFragment {

    @NonNull
    @Override
    EditFilterListItemDialogFragment createEditItemDialog(String text) {
        EditTextDialogFragment dialog = new EditTextDialogFragment();
        dialog.setTitle(text == null ? R.string.title_add : R.string.title_edit);
        dialog.setInitialValue(text);
        return dialog;
    }

    @Override
    String getItemText(String value) {
        String regex = Util.unquoteRegex(value);
        return regex != null ? regex : value;
    }

}
