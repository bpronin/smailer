package com.bopr.android.smailer.util;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import com.bopr.android.smailer.R;

import static android.view.LayoutInflater.from;

/**
 * UI utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class UiUtil {

    private UiUtil() {
    }

    public static View alertDialogView(View view) {
        @SuppressLint("InflateParams")
        ViewGroup container = (ViewGroup) from(view.getContext()).inflate(R.layout.alert_dialog_view_container, null);
        container.addView(view);
        return container;
    }

}
