package com.bopr.android.smailer.util.validator;

import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Checks that {@link EditText}'s input matches email list address format.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EmailListTextValidator extends TextValidator {

    public EmailListTextValidator(TextView view) {
        super(view);
    }

    @Override
    public boolean isValidInput(TextView textView, Editable editable, String text) {
        return isValidValue(text);
    }

    public static boolean isValidValue(String text) {
        try {
            for (InternetAddress address : InternetAddress.parse(text)) {
                address.validate();
            }
            return true;
        } catch (AddressException x) {
            return false;
        }
    }

}
