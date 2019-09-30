package com.bopr.android.smailer.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.R;

import static com.bopr.android.smailer.PhoneEvent.REASON_NUMBER_BLACKLISTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TEXT_BLACKLISTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TRIGGER_OFF;
import static com.bopr.android.smailer.util.ResourceUtil.eventDirectionImage;
import static com.bopr.android.smailer.util.ResourceUtil.eventStateImage;
import static com.bopr.android.smailer.util.ResourceUtil.eventStateText;
import static com.bopr.android.smailer.util.ResourceUtil.eventTypeImage;
import static com.bopr.android.smailer.util.ResourceUtil.eventTypeText;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.formatDuration;

/**
 * Log item details dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class HistoryDetailsDialogFragment extends DialogFragment {

    private PhoneEvent value;

    void showDialog(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), "log_details_dialog");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        /* avoiding disappear on rotation */
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog == null) {
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_log_details, null, false);

            view.<ImageView>findViewById(R.id.image_event_type).setImageResource(eventTypeImage(value));
            view.<ImageView>findViewById(R.id.image_event_direction).setImageResource(eventDirectionImage(value));
            view.<TextView>findViewById(R.id.text_title).setText(value.getPhone());
            view.<TextView>findViewById(R.id.text_message).setText(formatMessage(value));
            view.<TextView>findViewById(R.id.text_time).setText(formatTime(value.getStartTime()));
            view.<ImageView>findViewById(R.id.image_event_result).setImageResource(eventStateImage(value));
            view.<TextView>findViewById(R.id.text_result).setText(eventStateText(value));
            view.<TextView>findViewById(R.id.text_result_reason).setText(formatReason(value));
            view.<TextView>findViewById(R.id.text_type_title).setText(eventTypeText(value));
            view.<TextView>findViewById(R.id.text_recipient).setText(value.getRecipient());

            dialog = new AlertDialog.Builder(requireContext())
                    .setView(view)
                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.cancel();
                        }
                    })
                    .create();
        }
        return dialog;
    }

    private CharSequence formatReason(PhoneEvent event) {
        if (event.getState() == PhoneEvent.STATE_IGNORED) {
            if ((event.getStateReason() & REASON_NUMBER_BLACKLISTED) != 0) {
                return "(" + getString(R.string.number_in_blacklist) + ")";
            } else if ((event.getStateReason() & REASON_TEXT_BLACKLISTED) != 0) {
                return "(" + getString(R.string.text_in_blacklist) + ")";
            } else if ((event.getStateReason() & REASON_TRIGGER_OFF) != 0) {
                return "(" + getString(R.string.trigger_off) + ")";
            }
        }
        return null;
    }

    public void setValue(PhoneEvent value) {
        this.value = value;
    }

    private CharSequence formatMessage(PhoneEvent event) {
        if (event.isSms()) {
            return event.getText();
        } else if (event.isMissed()) {
            return getString(R.string.you_had_missed_call);
        } else {
            int pattern;
            if (event.isIncoming()) {
                pattern = R.string.you_had_incoming_call;
            } else {
                pattern = R.string.you_had_outgoing_call;
            }
            return formatter(requireContext())
                    .pattern(pattern)
                    .put("duration", formatDuration(event.getCallDuration()))
                    .format();
        }
    }

//    private String formatRecipient(PhoneEvent event) {
//        return formatter(requireContext())
//                .pattern(R.string.recipient_device)
//                .put("recipient", event.getRecipient())
//                .format();
//    }

    private CharSequence formatTime(Long time) {
        return DateFormat.format(getString(R.string._time_pattern), time);
    }
}
