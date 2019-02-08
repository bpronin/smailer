package com.bopr.android.smailer.util.ui.preference;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.R;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

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

        setUpButton(view);
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

    @SuppressLint("ClickableViewAccessibility")
    private void setUpButton(View view) {
        ImageButton button = view.findViewById(R.id.button);
        button.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        break;
                    case MotionEvent.ACTION_UP:
                        editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }
                return false;
            }
        });
    }

}
