package com.bopr.android.smailer;

import android.content.Context;

/**
 * Miscellaneous formats.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Formats {

    public static int eventTypeImage(PhoneEvent event) {
        if (event.isSms()) {
            return R.drawable.ic_message;
        } else {
            return R.drawable.ic_call;
        }
    }

    public static int eventTypeText(Context context, PhoneEvent event) {
        if (event.isSms()) {
            if (event.isIncoming()) {
                return R.string.event_type_incoming_sms;
            } else {
                return R.string.event_type_outgoing_sms;
            }
        } else if (event.isMissed()) {
            return R.string.event_type_missed_call;
        } else {
            if (event.isIncoming()) {
                return R.string.event_type_incoming_call;
            } else {
                return R.string.event_type_outgoing_call;
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
            case PENDING:
                return R.drawable.ic_state_pending;
            case PROCESSED:
                return R.drawable.ic_state_done;
            case IGNORED:
                return R.drawable.ic_state_block;
        }
        return -1;
    }

    public static int eventStateText(PhoneEvent event) {
        switch (event.getState()) {
            case PENDING:
                return R.string.event_state_pending;
            case PROCESSED:
                return R.string.event_state_done;
            case IGNORED:
                return R.string.event_state_ignored;
        }
        return -1;
    }

}
