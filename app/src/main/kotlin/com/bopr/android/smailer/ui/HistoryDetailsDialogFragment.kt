package com.bopr.android.smailer.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_NUMBER_BLACKLISTED
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_TEXT_BLACKLISTED
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.*

/**
 * Call history log item details dialog fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class HistoryDetailsDialogFragment(private val event: Event) :
    BaseDialogFragment("log_details_dialog") {

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        return inflater.inflate(R.layout.dialog_history_details, root, false).apply {
            findViewById<ImageView>(R.id.image_processing_result).setImageResource(
                messageStateImage(event.processState)
            )
            findViewById<TextView>(R.id.text_result).setText(messageStateText(event.processState))
            findViewById<TextView>(R.id.text_recipient).text = event.target
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

            (event.payload as? PhoneCallData)?.let {
                findViewById<ImageView>(R.id.image_phone_call_type).setImageResource(
                    phoneCallTypeImage(it)
                )
                findViewById<ImageView>(R.id.image_phone_call_direction).setImageResource(
                    phoneCallDirectionImage(it)
                )
                findViewById<TextView>(R.id.text_title).text = it.phone
                findViewById<TextView>(R.id.text_message).text = formatMessage(it)
                findViewById<TextView>(R.id.text_time).text = formatTime(it.startTime)
                findViewById<TextView>(R.id.text_type_title).setText(phoneCallTypeText(it))
            }
        }
    }

    private fun onExplainResult() {
        val msg = buildString {
            if (FLAG_BYPASS_NUMBER_BLACKLISTED in event.bypassFlags) {
                append(getString(R.string.number_in_blacklist))
                append("\n\n")
            }
            if (FLAG_BYPASS_TEXT_BLACKLISTED in event.bypassFlags) {
                append(getString(R.string.text_in_blacklist))
                append("\n\n")
            }
            if (FLAG_BYPASS_TRIGGER_OFF in event.bypassFlags) {
                append(getString(R.string.trigger_off))
                append("\n\n")
            }
        }

        MessageDialog(getString(R.string.ignored), msg).show(this)
    }

    private fun formatProcessTime(event: Event): CharSequence? {
        return event.processTime?.run {
            getString(R.string.processed_at, formatTime(this))
        }
    }

    private fun formatTime(time: Long): CharSequence {
        return DateFormat.format(getString(R.string._time_pattern), time)
    }

    private fun formatMessage(info: PhoneCallData): CharSequence? {
        return when {
            info.isSms ->
                info.text

            info.isMissed ->
                getString(R.string.you_had_missed_call)

            else -> {
                val pattern = if (info.isIncoming) {
                    R.string.you_had_incoming_call
                } else {
                    R.string.you_had_outgoing_call
                }
                getString(pattern, formatDuration(info.callDuration))
            }
        }
    }
}