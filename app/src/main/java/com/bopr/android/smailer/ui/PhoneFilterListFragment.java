package com.bopr.android.smailer.ui;

import com.bopr.android.smailer.R;

import androidx.annotation.NonNull;

/**
 * Phone filter list activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
abstract class PhoneFilterListFragment extends FilterListFragment {

    @NonNull
    @Override
    EditFilterListItemDialogFragment createEditItemDialog(String text) {
        EditPhoneDialogFragment dialog = new EditPhoneDialogFragment();
        dialog.setTitle(text == null ? R.string.title_add : R.string.title_edit);
        dialog.setInitialValue(text);
        return dialog;
    }

}
