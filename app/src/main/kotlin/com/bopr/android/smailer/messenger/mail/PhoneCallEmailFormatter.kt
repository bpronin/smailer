package com.bopr.android.smailer.messenger.mail

import android.content.Context
import android.text.TextUtils.htmlEncode
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.util.escapePhoneNumber
import com.bopr.android.smailer.util.phoneCallTypePrefix
import com.bopr.android.smailer.util.phoneCallTypeText
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.formatPhoneNumber
import com.bopr.android.smailer.util.getContactName
import com.bopr.android.smailer.util.htmlReplaceUrlsWithLinks
import com.bopr.android.smailer.util.httpEncoded
import com.bopr.android.smailer.util.stripPhoneNumber

/**
 * Formats email subject and body from phone event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneCallEmailFormatter(
    private val context: Context,
    private val info: PhoneCallInfo
) : BaseMailFormatter(
    context,
    info.startTime,
    info.processTime,
    info.location
) {

    private val serviceAccount = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
    private val phoneSearchUrl = settings.getPhoneSearchUrl()

    override fun getSubject(): String {
        return "${string(phoneCallTypePrefix(info))} ${escapePhoneNumber(info.phone)}"
    }

    override fun getTitle(): String {
        return string(phoneCallTypeText(info))
    }

    override fun getMessage(): String {
        return when {
            info.isMissed -> {
                string(R.string.you_had_missed_call)
            }

            info.isSms -> {
                info.text!!.htmlReplaceUrlsWithLinks()
            }

            else -> {
                val duration = formatDuration(info.callDuration)
                if (info.isIncoming) {
                    string(R.string.you_had_incoming_call, duration)
                } else {
                    string(R.string.you_had_outgoing_call, duration)
                }
            }
        }
    }

    override fun getSenderName(): String {
        val strippedPhone = stripPhoneNumber(info.phone)
        val formattedPhone = formatPhoneNumber(info.phone)

        val telLink = "<a href=\"tel:${formattedPhone.httpEncoded()}\"" +
                " style=\"text-decoration: none;\">&#9742;</a>$strippedPhone"


        val text = context.getContactName(info.phone)?.let {
            val url = phoneSearchUrl.replace(PHONE_SEARCH_TAG, strippedPhone)
            "<a href=\"$url\">${string(R.string.unknown_contact)}</a>"
        }.orEmpty()

        val patternRes = when {
            info.isSms -> R.string.sender_phone
            info.isIncoming -> R.string.caller_phone
            else -> R.string.called_phone
        }

        return string(patternRes, telLink, text)
    }

    override fun getReplyLinks(): List<String>? {
        if (serviceAccount.isNullOrEmpty()) return null

        val phone = escapePhoneNumber(info.phone)
        val smsText = info.text ?: ""
        val subject = formatSubject()

        return listOf(
            formatReplyLink(
                R.string.add_phone_to_blacklist,
                "add phone $phone to blacklist",
                subject
            ),
            formatReplyLink(
                R.string.add_text_to_blacklist,
                "add text \"$smsText\" to blacklist",
                subject
            ),
            formatReplyLink(
                R.string.send_sms_to_sender,
                "send SMS message \"Sample text\" to $phone",
                subject
            )
        )
    }

    private fun formatReplyLink(@StringRes titleRes: Int, body: String, subject: String): String {
        /* Important! There should not be line breaks in href values. Note: Kotlin's """ """ adds line breaks */
//        return "<a href=\"mailto:$serviceAccount?subject=${htmlEncode("Re: $subject")}&amp;" +
//                "body=${htmlEncode("To device \"$deviceName\": %0d%0a $body")}\">" +
//                "<small>${string(titleRes)}</small>" +
//                "</a>"
        return href(
            "mailto:$serviceAccount?" +
                    "subject=${htmlEncode("Re: $subject")}&amp;" +
                    "body=${htmlEncode("To device \"${settings.getDeviceName()}\": %0d%0a $body")}",
            "<small>${string(titleRes)}</small>"
        )
    }

    companion object {

        const val PHONE_SEARCH_TAG = "{phone}"
    }

}