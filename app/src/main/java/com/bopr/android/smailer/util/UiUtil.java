package com.bopr.android.smailer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ParagraphStyle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import static android.view.LayoutInflater.from;
import static androidx.core.content.ContextCompat.getColor;
import static com.bopr.android.smailer.PhoneEvent.STATE_IGNORED;
import static com.bopr.android.smailer.PhoneEvent.STATE_PENDING;
import static com.bopr.android.smailer.PhoneEvent.STATE_PROCESSED;

/**
 * Miscellaneous resource utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class UiUtil {

    private UiUtil() {
    }

    @DrawableRes
    public static int eventTypeImage(@NonNull PhoneEvent event) {
        if (event.isSms()) {
            return R.drawable.ic_message;
        } else {
            return R.drawable.ic_call;
        }
    }

    @StringRes
    public static int eventTypeText(@NonNull PhoneEvent event) {
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

    @DrawableRes
    public static int eventDirectionImage(@NonNull PhoneEvent event) {
        if (event.isMissed()) {
            return R.drawable.ic_call_missed;
        } else if (event.isIncoming()) {
            return R.drawable.ic_call_in;
        } else {
            return R.drawable.ic_call_out;
        }
    }

    @DrawableRes
    public static int eventStateImage(@NonNull PhoneEvent event) {
        switch (event.getState()) {
            case STATE_PENDING:
                return R.drawable.ic_hourglass;
            case STATE_PROCESSED:
                return R.drawable.ic_state_done;
            case STATE_IGNORED:
                return R.drawable.ic_state_block;
        }
        throw new IllegalArgumentException("Unknown state");
    }

    @StringRes
    public static int eventStateText(@NonNull PhoneEvent event) {
        switch (event.getState()) {
            case STATE_PENDING:
                return R.string.pending;
            case STATE_PROCESSED:
                return R.string.sent_email;
            case STATE_IGNORED:
                return R.string.ignored;
        }
        throw new IllegalArgumentException("Unknown state");
    }

    /**
     * Returns text underlined with wavy red line.
     */
    public static Spannable underwivedText(@NonNull Context context, String value) {
        Spannable spannable = new SpannableString(value);
        ParagraphStyle span = new WavyUnderlineSpan(context);
        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Returns text of accent color.
     */
    public static Spannable accentedText(@NonNull Context context, String value) {
        Spannable spannable = new SpannableString(value);
        CharacterStyle span = new ForegroundColorSpan(getColor(context, R.color.colorAccent));
        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static void showToast(@NonNull Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void showToast(@NonNull Context context, int textRes) {
        Toast.makeText(context, textRes, Toast.LENGTH_LONG).show();
    }

    public static View alertDialogView(@NonNull View view) {
        @SuppressLint("InflateParams")
        ViewGroup container = (ViewGroup) from(view.getContext()).inflate(R.layout.alert_dialog_view_container, null);
        container.addView(view);
        return container;
    }
}
