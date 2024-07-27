package com.bopr.android.smailer.processor.mail

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.text.TextUtils.htmlEncode
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.escapePhone
import com.bopr.android.smailer.util.eventTypePrefix
import com.bopr.android.smailer.util.eventTypeText
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.htmlReplaceUrlsWithLinks
import com.bopr.android.smailer.util.httpEncoded
import com.bopr.android.smailer.util.normalizePhone
import com.bopr.android.smailer.util.tryGetContactName

/**
 * Formats email subject and body from phone event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MailPhoneEventFormatter(
    private val context: Context,
    private val event: PhoneEventData
) : BaseMailFormatter(
    context,
    event.startTime,
    event.processTime,
    event.location
) {

    private val serviceAccount = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
    private val phoneSearchUrl = settings.getPhoneSearchUrl()
    private val contactName = tryGetContactName(context, event.phone)

    override fun getSubject(): String {
        return "${string(eventTypePrefix(event))} ${escapePhone(event.phone)}"
    }

    override fun getTitle(): String {
        return string(eventTypeText(event))
    }

    override fun getMessage(): String {
        return when {
            event.isMissed -> {
                string(R.string.you_had_missed_call)
            }

            event.isSms -> {
                event.text!!.htmlReplaceUrlsWithLinks()
            }

            else -> {
                val duration = formatDuration(event.callDuration)
                if (event.isIncoming) {
                    string(R.string.you_had_incoming_call, duration)
                } else {
                    string(R.string.you_had_outgoing_call, duration)
                }
            }
        }
    }

    override fun getSenderName(): String {
        val telLink = "<a href=\"tel:${event.phone.httpEncoded()}\"" +
                " style=\"text-decoration: none;\">&#9742;</a>${event.phone}"

        val contact = if (contactName.isNullOrEmpty()) {
            if (context.checkPermission(READ_CONTACTS)) {
                val url = phoneSearchUrl.replace(PHONE_SEARCH_TAG, normalizePhone(event.phone))
                "<a href=\"$url\">${string(R.string.unknown_contact)}</a>"
            } else ""
        } else {
            contactName
        }

        val patternRes = when {
            event.isSms ->
                R.string.sender_phone

            event.isIncoming ->
                R.string.caller_phone

            else ->
                R.string.called_phone
        }
        return string(patternRes, telLink, contact)
    }

    override fun getReplyLinks(): List<String>? {
        if (serviceAccount.isNullOrEmpty()) return null

        val phone = escapePhone(event.phone)
        val smsText = event.text ?: ""
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