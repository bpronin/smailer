package com.bopr.android.smailer.ui;

import android.support.annotation.NonNull;
import com.bopr.android.smailer.R;

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
        dialog.setTitle(text == null ? R.string.title_add_text_fragment : R.string.title_edit_text_fragment);
        dialog.setInitialValue(text);
        return dialog;
    }
}
