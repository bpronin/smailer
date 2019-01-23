package com.bopr.android.smailer.util.ui.preference;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

import com.bopr.android.smailer.Cryptor;

/**
 * A Preference for password input.
 * This preference will store an encrypted string into the {@link android.content.SharedPreferences}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PasswordPreference extends EditTextPreference {

    private Cryptor cryptor;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @SuppressWarnings("unused")
    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("unused")
    public PasswordPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        cryptor = new Cryptor(getContext());
        //// TODO: 23.01.2019 Migration
        //        getEditText().setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Override
    public String getText() {
        return null; /* do not show anything. even the length of current password */
    }

   /*
    //// TODO: 23.01.2019 Migration
//    public EditText getEditText() {
//        if (mFragment != null) {
//            final Dialog dialog = mFragment.getDialog();
//            if (dialog != null) {
//                return (EditText) dialog.findViewById(android.R.id.edit);
//            }
//        }
//        return null;
//    }

  /*
    //// TODO: 23.01.2019 Migration
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String value = cryptor.encrypt(getEditText().getText().toString());
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }*/
}
