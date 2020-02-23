package com.bopr.android.smailer.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_NUMBER_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TEXT_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TRIGGER_OFF
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.TextUtil.formatDuration
import com.bopr.android.smailer.util.UiUtil.eventDirectionImage
import com.bopr.android.smailer.util.UiUtil.eventStateImage
import com.bopr.android.smailer.util.UiUtil.eventStateText
import com.bopr.android.smailer.util.UiUtil.eventTypeImage
import com.bopr.android.smailer.util.UiUtil.eventTypeText

/**
 * Log item details dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HistoryDetailsDialogFragment(private val event: PhoneEvent) : BaseDialogFragment("log_details_dialog") {

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        return inflater.inflate(R.layout.dialog_history_details, root, false)
                .apply {
                    findViewById<ImageView>(R.id.image_event_type).setImageResource(eventTypeImage(event))
                    findViewById<ImageView>(R.id.image_event_direction).setImageResource(eventDirectionImage(event))
                    findViewById<TextView>(R.id.text_title).text = event.phone
                    findViewById<TextView>(R.id.text_message).text = formatMessage(event)
                    findViewById<TextView>(R.id.text_time).text = formatTime(event.startTime)
                    findViewById<ImageView>(R.id.image_event_result).setImageResource(eventStateImage(event))
                    findViewById<TextView>(R.id.text_result).setText(eventStateText(event))
                    findViewById<TextView>(R.id.text_result_reason).text = formatReason(event)
                    findViewById<TextView>(R.id.text_type_title).setText(eventTypeText(event))
                    findViewById<TextView>(R.id.text_recipient).text = event.acceptor
                }
    }

    private fun formatReason(event: PhoneEvent): CharSequence? {
        if (event.state == STATE_IGNORED) {
            when {
                event.stateReason and REASON_NUMBER_BLACKLISTED != 0 ->
                    return "(" + getString(R.string.number_in_blacklist) + ")"
                event.stateReason and REASON_TEXT_BLACKLISTED != 0 ->
                    return "(" + getString(R.string.text_in_blacklist) + ")"
                event.stateReason and REASON_TRIGGER_OFF != 0 ->
                    return "(" + getString(R.string.trigger_off) + ")"
            }
        }
        return null
    }

    private fun formatMessage(event: PhoneEvent): CharSequence? {
        return when {
            event.isSms ->
                event.text
            event.isMissed ->
                getString(R.string.you_had_missed_call)
            else -> {
                val pattern = if (event.isIncoming) {
                    R.string.you_had_incoming_call
                } else {
                    R.string.you_had_outgoing_call
                }
                getString(pattern, formatDuration(event.callDuration))
            }
        }
    }

    private fun formatTime(time: Long): CharSequence {
        return DateFormat.format(getString(R.string._time_pattern), time)
    }
}