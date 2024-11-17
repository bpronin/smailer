package com.bopr.android.smailer.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.ACCEPT_STATE_BYPASS_NUMBER_BLACKLISTED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.ACCEPT_STATE_BYPASS_TEXT_BLACKLISTED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.ACCEPT_STATE_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.R
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.util.*

/**
 * Call history log item details dialog fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HistoryDetailsDialogFragment(private val info: PhoneCallInfo) : BaseDialogFragment("log_details_dialog") {

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        return inflater.inflate(R.layout.dialog_history_details, root, false)
                .apply {
                    findViewById<ImageView>(R.id.image_phone_call_type).setImageResource(phoneCallTypeImage(info))
                    findViewById<ImageView>(R.id.image_phone_call_direction).setImageResource(phoneCallDirectionImage(info))
                    findViewById<TextView>(R.id.text_title).text = info.phone
                    findViewById<TextView>(R.id.text_message).text = formatMessage(info)
                    findViewById<TextView>(R.id.text_time).text = formatTime(info.startTime)
                    findViewById<ImageView>(R.id.image_processing_result).setImageResource(messageStateImage(info.processState))
                    findViewById<TextView>(R.id.text_result).setText(messageStateText(info.processState))
                    findViewById<TextView>(R.id.text_type_title).setText(phoneCallTypeText(info))
                    findViewById<TextView>(R.id.text_recipient).text = info.acceptor
                    findViewById<TextView>(R.id.text_result_time).run {
                        visibility = if (info.processState == STATE_PROCESSED) VISIBLE else GONE
                        text = formatProcessTime(info)
                    }
                    findViewById<ImageView>(R.id.image_explain_result).run {
                        visibility = if (info.processState == STATE_IGNORED) VISIBLE else GONE
                        setOnClickListener {
                            onExplainResult()
                        }
                    }
                }
    }

    private fun onExplainResult() {
        val sb = StringBuilder()
        if (info.acceptState and ACCEPT_STATE_BYPASS_NUMBER_BLACKLISTED != 0) {
            sb.append(getString(R.string.number_in_blacklist)).append("\n\n")
        }
        if (info.acceptState and ACCEPT_STATE_BYPASS_TEXT_BLACKLISTED != 0) {
            sb.append(getString(R.string.text_in_blacklist)).append("\n\n")
        }
        if (info.acceptState and ACCEPT_STATE_BYPASS_TRIGGER_OFF != 0) {
            sb.append(getString(R.string.trigger_off)).append("\n\n")
        }

        MessageDialog(getString(R.string.ignored), sb.toString()).show(this)
    }

    private fun formatMessage(info: PhoneCallInfo): CharSequence? {
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

    private fun formatProcessTime(info: PhoneCallInfo): CharSequence? {
        return info.processTime?.run {
            getString(R.string.processed_at, formatTime(this))
        }
    }

    private fun formatTime(time: Long): CharSequence {
        return DateFormat.format(getString(R.string._time_pattern), time)
    }
}