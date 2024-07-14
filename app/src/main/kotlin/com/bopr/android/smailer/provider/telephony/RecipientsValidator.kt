package com.bopr.android.smailer.provider.telephony

import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.isValidEmailAddressList
import org.slf4j.LoggerFactory

class RecipientsValidator(private val notifications: NotificationsHelper) {

    private val log = LoggerFactory.getLogger("CallProcessor")

    fun checkRecipients(recipients: MutableList<String>): Boolean {
        if (recipients.isEmpty()) {
            notifications.showRecipientsError(R.string.no_recipients_specified)

            log.warn("Recipients not specified")
            return false
        }

        if (!isValidEmailAddressList(recipients)) {
            notifications.showRecipientsError(R.string.invalid_recipient)

            log.warn("Recipients are invalid")
            return false
        }

        return true
    }
}
