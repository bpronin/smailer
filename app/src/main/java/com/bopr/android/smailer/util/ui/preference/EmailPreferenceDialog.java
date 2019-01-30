package com.bopr.android.smailer.util.ui.preference;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;

import com.bopr.android.smailer.util.validator.EmailTextValidator;

/**
 * A {@link EmailPreference } dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EmailPreferenceDialog extends EditTextPreferenceDialogFragmentCompat {

    public static EmailPreferenceDialog newInstance(String key) {
        EmailPreferenceDialog fragment = new EmailPreferenceDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        EditText editText = view.findViewById(android.R.id.edit);
        editText.addTextChangedListener(new EmailTextValidator(editText));
    }
}
