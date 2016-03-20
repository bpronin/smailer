package com.bopr.android.smailer.util.validator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

/**
 * Abstract {@link EditText} validator.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class TextValidator implements TextWatcher {

    private final TextView view;
    private Object span;

    public TextValidator(TextView view, int errorColor) {
        this.view = view;
        span = new WavyUnderlineSpan(errorColor);
    }

    /**
     * Returns true if input is valid.
     */
    public abstract boolean isValidInput(TextView textView, Editable editable, String text);

    @Override
    public void afterTextChanged(Editable editable) {
        if (isValidInput(view, editable, editable.toString())) {
            editable.removeSpan(span);
        } else {
            editable.setSpan(span, 0, editable.length(), 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) { /* do nothing */ }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { /* do nothing */ }

}