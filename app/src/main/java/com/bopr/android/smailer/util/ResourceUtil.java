package com.bopr.android.smailer.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ParagraphStyle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import static androidx.core.content.ContextCompat.getColor;
import static com.bopr.android.smailer.PhoneEvent.STATE_IGNORED;
import static com.bopr.android.smailer.PhoneEvent.STATE_PENDING;
import static com.bopr.android.smailer.PhoneEvent.STATE_PROCESSED;

/**
 * Miscellaneous resource utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class ResourceUtil {

    private ResourceUtil() {
    }

    public static int eventTypeImage(PhoneEvent event) {
        if (event.isSms()) {
            return R.drawable.ic_message;
        } else {
            return R.drawable.ic_call;
        }
    }

    public static int eventTypeText(PhoneEvent event) {
        if (event.isSms()) {
            if (event.isIncoming()) {
                return R.string.incoming_sms;
            } else {
                return R.string.outgoing_sms;
            }
        } else if (event.isMissed()) {
            return R.string.missed_call;
        } else {
            if (event.isIncoming()) {
                return R.string.incoming_call;
            } else {
                return R.string.outgoing_call;
            }
        }
    }

    public static int eventDirectionImage(PhoneEvent event) {
        if (event.isMissed()) {
            return R.drawable.ic_call_missed;
        } else if (event.isIncoming()) {
            return R.drawable.ic_call_in;
        } else {
            return R.drawable.ic_call_out;
        }
    }

    public static int eventStateImage(PhoneEvent event) {
        switch (event.getState()) {
            case STATE_PENDING:
                return R.drawable.ic_state_pending;
            case STATE_PROCESSED:
                return R.drawable.ic_state_done;
            case STATE_IGNORED:
                return R.drawable.ic_state_block;
        }
        return -1;
    }

    public static int eventStateText(PhoneEvent event) {
        switch (event.getState()) {
            case STATE_PENDING:
                return R.string.pending;
            case STATE_PROCESSED:
                return R.string.sent_email;
            case STATE_IGNORED:
                return R.string.ignored;
        }
        return -1;
    }

    /**
     * Returns text underlined with wavy red line.
     */
    public static Spannable underwivedText(Context context, String value) {
        Spannable spannable = new SpannableString(value);
        ParagraphStyle span = new WavyUnderlineSpan(context);
        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Returns text of accent color.
     */
    public static Spannable accentedText(Context context, String value) {
        Spannable spannable = new SpannableString(value);
        CharacterStyle span = new ForegroundColorSpan(getColor(context, R.color.colorAccent));
        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static void showToast(Context context, String text) {
        updateToastStyle(context, Toast.makeText(context, text, Toast.LENGTH_LONG)).show();
    }

    public static void showToast(Context context, int textRes) {
        updateToastStyle(context, Toast.makeText(context, textRes, Toast.LENGTH_LONG)).show();
    }

    private static Toast updateToastStyle(Context context, Toast toast) {
        View view = toast.getView();
        TextView textView = view.findViewById(android.R.id.message);
        view.setBackgroundResource(R.drawable.toast_frame);
        textView.setTextColor(getColor(context, R.color.toastForeground));
//        toast.setGravity(Gravity.CENTER, 0, 0);
        return toast;
    }

}
