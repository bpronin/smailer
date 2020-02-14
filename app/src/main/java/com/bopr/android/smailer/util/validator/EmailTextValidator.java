package com.bopr.android.smailer.util.validator;

import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;

import com.bopr.android.smailer.util.AddressUtil;

/**
 * Checks that {@link EditText}'s input matches email address format.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EmailTextValidator extends TextValidator {

    public EmailTextValidator(TextView view) {
        super(view);
    }

    @Override
    public boolean isValidInput(TextView textView, Editable editable, String text) {
        return AddressUtil.isValidEmailAddress(text);
    }

}
