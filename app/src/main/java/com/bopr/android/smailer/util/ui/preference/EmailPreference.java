package com.bopr.android.smailer.util.ui.preference;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * A {@link EditTextPreference } for email input.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("unused")
public class EmailPreference extends EditTextPreference {

    public EmailPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EmailPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EmailPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmailPreference(Context context) {
        super(context);
    }
}
