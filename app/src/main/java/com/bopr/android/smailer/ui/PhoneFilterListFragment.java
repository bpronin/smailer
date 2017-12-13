package com.bopr.android.smailer.ui;

import android.support.annotation.NonNull;
import com.bopr.android.smailer.R;

import static com.bopr.android.smailer.util.Util.normalizePhone;

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
        dialog.setTitle(text == null ? R.string.title_add_phone : R.string.title_edit_phone);
        dialog.setInitialValue(text);
        return dialog;
    }

    @Override
    String getItemText(String text) {
        return normalizePhone(text);
    }
}
