package com.bopr.android.smailer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableString;

import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

/**
 * Utilities dependent of android app context .
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AndroidUtil {

    private AndroidUtil() {
    }

    /**
     * If {@code valid} parameter is false returns text underlined with wavy red line.
     */
    public static Spannable validatedText(Context context, String value, boolean valid) {
        Spannable result = new SpannableString(value);
        if (!valid) {
            WavyUnderlineSpan span = new WavyUnderlineSpan(context);
            result.setSpan(span, 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }

    /**
     * Returns true if device is connected ty internet.
     */
    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
