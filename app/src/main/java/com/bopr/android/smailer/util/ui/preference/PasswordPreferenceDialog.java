package com.bopr.android.smailer.util.ui.preference;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;

import com.bopr.android.smailer.Cryptor;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

/**
 * A {@link PasswordPreference } dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PasswordPreferenceDialog extends EditTextPreferenceDialogFragmentCompat {

    private Cryptor cryptor;
    private EditText editText;

    public static PasswordPreferenceDialog newInstance(String key) {
        PasswordPreferenceDialog fragment = new PasswordPreferenceDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cryptor = new Cryptor(getContext());
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        editText = view.findViewById(android.R.id.edit);
        editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = cryptor.encrypt(editText.getText().toString());
            PasswordPreference preference = (PasswordPreference) getPreference();
            if (preference.callChangeListener(value)) {
                preference.setText(value);
            }
        }
    }

}
