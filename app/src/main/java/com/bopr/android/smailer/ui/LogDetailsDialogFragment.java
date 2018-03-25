package com.bopr.android.smailer.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bopr.android.smailer.Formats;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;

import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.formatDuration;

/**
 * Log item details dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LogDetailsDialogFragment extends DialogFragment {

    private PhoneEvent value;

    public void showDialog(Activity activity) {
        show(activity.getFragmentManager(), "log_details_dialog");
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
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_log_details, null, false);

            view.<ImageView>findViewById(R.id.image_event_type).setImageResource(Formats.eventTypeImage(value));
            view.<ImageView>findViewById(R.id.image_event_direction).setImageResource(Formats.eventDirectionImage(value));
            view.<TextView>findViewById(R.id.text_title).setText(value.getPhone());
            view.<TextView>findViewById(R.id.text_message).setText(formatMessage(value));
            view.<TextView>findViewById(R.id.text_time).setText(formatTime(value.getStartTime()));
            view.<ImageView>findViewById(R.id.image_event_result).setImageResource(Formats.eventStateImage(value));
            view.<TextView>findViewById(R.id.text_result).setText(Formats.eventStateText(value));
            view.<TextView>findViewById(R.id.text_type_title).setText(Formats.eventTypeText(getContext(), value));

            dialog = AndroidUtil.dialogBuilder(getActivity())
                    .setView(view)
                    .create();
        }
        return dialog;
    }

    public void setValue(PhoneEvent value) {
        this.value = value;
    }

    private CharSequence formatMessage(PhoneEvent event) {
        if (event.isSms()) {
            return event.getText();
        } else if (event.isMissed()) {
            return getString(R.string.email_body_missed_call);
        } else {
            int pattern;
            if (event.isIncoming()) {
                pattern = R.string.email_body_incoming_call;
            } else {
                pattern = R.string.email_body_outgoing_call;
            }
            return formatter(pattern, getActivity())
                    .put("duration", formatDuration(event.getCallDuration()))
                    .format();
        }
    }

    private CharSequence formatTime(Long time) {
        return DateFormat.format(getString(R.string.event_time_pattern), time);
    }
}
