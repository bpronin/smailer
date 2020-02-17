package com.bopr.android.smailer.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.R;

import static com.bopr.android.smailer.util.TextUtil.unescapeRegex;

/**
 * Text filter list activity fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
abstract class TextFilterListFragment extends FilterListFragment {

    @NonNull
    @Override
    EditFilterListItemDialogFragment createEditItemDialog(@Nullable String text) {
        EditTextDialogFragment dialog = new EditTextDialogFragment();
        dialog.setTitle(text == null ? R.string.add : R.string.edit);
        dialog.setInitialValue(text);
        return dialog;
    }

    @Override
    @Nullable
    String getItemText(@Nullable String value) {
        String regex = unescapeRegex(value);
        return regex != null ? regex : value;
    }

}
