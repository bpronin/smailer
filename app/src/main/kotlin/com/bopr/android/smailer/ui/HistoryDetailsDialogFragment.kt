package com.bopr.android.smailer.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.provider.telephony.PhoneEventData.Companion.ACCEPT_STATE_BYPASS_NUMBER_BLACKLISTED
import com.bopr.android.smailer.provider.telephony.PhoneEventData.Companion.ACCEPT_STATE_BYPASS_TEXT_BLACKLISTED
import com.bopr.android.smailer.provider.telephony.PhoneEventData.Companion.ACCEPT_STATE_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.R
import com.bopr.android.smailer.provider.EventState.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.util.*

/**
 * Call history log item details dialog fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HistoryDetailsDialogFragment(private val event: PhoneEventData) : BaseDialogFragment("log_details_dialog") {

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        return inflater.inflate(R.layout.dialog_history_details, root, false)
                .apply {
                    findViewById<ImageView>(R.id.image_event_type).setImageResource(eventTypeImage(event))
                    findViewById<ImageView>(R.id.image_event_direction).setImageResource(eventDirectionImage(event))
                    findViewById<TextView>(R.id.text_title).text = event.phone
                    findViewById<TextView>(R.id.text_message).text = formatMessage(event)
                    findViewById<TextView>(R.id.text_time).text = formatTime(event.startTime)
                    findViewById<ImageView>(R.id.image_event_result).setImageResource(eventStateImage(event.processState))
                    findViewById<TextView>(R.id.text_result).setText(eventStateText(event.processState))
                    findViewById<TextView>(R.id.text_type_title).setText(eventTypeText(event))
                    findViewById<TextView>(R.id.text_recipient).text = event.acceptor
                    findViewById<TextView>(R.id.text_result_time).run {
                        visibility = if (event.processState == STATE_PROCESSED) VISIBLE else GONE
                        text = formatProcessTime(event)
                    }
                    findViewById<ImageView>(R.id.image_explain_result).run {
                        visibility = if (event.processState == STATE_IGNORED) VISIBLE else GONE
                        setOnClickListener {
                            onExplainResult()
                        }
                    }
                }
    }

    private fun onExplainResult() {
        val sb = StringBuilder()
        if (event.acceptState and ACCEPT_STATE_BYPASS_NUMBER_BLACKLISTED != 0) {
            sb.append(getString(R.string.number_in_blacklist)).append("\n\n")
        }
        if (event.acceptState and ACCEPT_STATE_BYPASS_TEXT_BLACKLISTED != 0) {
            sb.append(getString(R.string.text_in_blacklist)).append("\n\n")
        }
        if (event.acceptState and ACCEPT_STATE_BYPASS_TRIGGER_OFF != 0) {
            sb.append(getString(R.string.trigger_off)).append("\n\n")
        }

        MessageDialog(getString(R.string.ignored), sb.toString()).show(this)
    }

    private fun formatMessage(event: PhoneEventData): CharSequence? {
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

    private fun formatProcessTime(event: PhoneEventData): CharSequence? {
        return event.processTime?.run {
            getString(R.string.processed_at, formatTime(this))
        }
    }

    private fun formatTime(time: Long): CharSequence {
        return DateFormat.format(getString(R.string._time_pattern), time)
    }
}