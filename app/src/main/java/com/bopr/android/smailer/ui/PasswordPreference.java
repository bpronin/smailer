package com.bopr.android.smailer.ui;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import com.bopr.android.smailer.Cryptor;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

/**
 * A {@link Preference} for password input.
 * <p/>
 * This preference will store an encrypted string into the {@link android.content.SharedPreferences}.
 */
public class PasswordPreference extends EditTextPreference {

    private Cryptor cryptor;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PasswordPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        cryptor = new Cryptor(getContext());
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        EditText editText = getEditText();
        editText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
        editText.setText(null); /* do not show anything. even the length of current password */

        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String value = cryptor.encrypt(getEditText().getText().toString());
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }
}