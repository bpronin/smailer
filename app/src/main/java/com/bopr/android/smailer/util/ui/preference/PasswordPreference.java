package com.bopr.android.smailer.util.ui.preference;


import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * A {@link EditTextPreference } for password input.
 * This preference will store an encrypted string into the {@link android.content.SharedPreferences}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("unused")
public class PasswordPreference extends EditTextPreference {

    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasswordPreference(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null; /* do not show anything. even the length of current password */
    }

}
