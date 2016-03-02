package com.bopr.android.smailer.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.bopr.android.smailer.util.EncryptUtil;

/**
 * Class PasswordPreference.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PasswordPreference extends EditTextPreference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

//    @Override
//    protected boolean persistString(String value) {
//        return super.persistString(EncryptUtil.encrypt(getContext(), value));
//    }
//
//    @Override
//    protected String getPersistedString(String defaultReturnValue) {
//        return "";
//    }

}
