package com.bopr.android.smailer

import android.accounts.AccountsException
import android.content.Context
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_MARK_SMS_AS_READ
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.util.ContentUtils.contactName
import com.bopr.android.smailer.util.ContentUtils.markSmsAsRead
import com.bopr.android.smailer.util.isValidEmailAddressList
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

/**
 * Sends out email for phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallProcessor(
        private val context: Context,
        private val database: Database,
        private val transport: GoogleMail = GoogleMail(context),
        private val notifications: Notifications = Notifications(context),
        private val locator: GeoLocator = GeoLocator(context, database)) {

    private val log = LoggerFactory.getLogger("CallProcessor")
    private val settings: Settings = Settings(context)

    /**
     * Sends out a mail for event.
     *
     * @param event email event
     */
    fun process(event: PhoneEvent) {
        log.debug("Processing event: $event")

        event.location = locator.getLocation()
        event.stateReason = settings.getFilter().test(event)
        if (event.stateReason != REASON_ACCEPTED) {
            event.state = STATE_IGNORED
        } else if (startMailSession(false) && sendMail(event, false)) {
            event.state = STATE_PROCESSED
        }

        database.putEvent(event)
        database.notifyChanged()
    }

    /**
     * Sends out email for all pending events.
     */
    fun processPending() {
        log.debug("Processing pending events")

        val events = database.pendingEvents.toList()
        if (events.isEmpty()) {
            log.debug("No pending events")
        } else {
            if (startMailSession(true)) {
                for (event in events) {
                    if (sendMail(event, true)) {
                        event.state = STATE_PROCESSED
                        database.putEvent(event)
                    }
                }
            }
            database.notifyChanged()
        }
    }

    private fun startMailSession(silent: Boolean): Boolean {
        log.debug("Starting session")

        return try {
            requireRecipient(silent)
            transport.startSession(requireAccount(silent), GMAIL_SEND)
            true
        } catch (x: AccountsException) {
            log.warn("Failed starting mail session: ", x)

            showErrorNotification(R.string.account_not_registered, silent)
            false
        } catch (x: Exception) {
            log.warn("Failed starting mail session: ", x)

            false
        }
    }

    private fun sendMail(event: PhoneEvent, silent: Boolean): Boolean {
        log.debug("Sending mail: $event")

        return try {
            sendMessage(event, requireRecipient(silent))
            notifications.hideAllErrors()

            if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS, false)) {
                notifications.showMessage(R.string.email_successfully_send, Notifications.ACTION_SHOW_MAIN)
            }
            if (settings.getBoolean(PREF_MARK_SMS_AS_READ, false)) {
                markSmsAsRead(context, event)
            }
            true
        } catch (x: UserRecoverableAuthIOException) {
            log.warn("Failed sending mail: ", x)

            showErrorNotification(R.string.need_google_permission, silent)
            false
        } catch (x: Exception) {
            log.warn("Failed sending mail: ", x)

            false
        }
    }

    @Throws(Exception::class)
    private fun requireAccount(silent: Boolean): String {
        val sender = settings.getString(PREF_SENDER_ACCOUNT)
        if (sender.isNullOrEmpty()) {
            showErrorNotification(R.string.no_account_specified, silent)
            throw Exception("Account not specified")
        }
        return sender
    }

    @Throws(Exception::class)
    private fun requireRecipient(silent: Boolean): String {
        val recipients = settings.getString(PREF_RECIPIENTS_ADDRESS)

        if (recipients == null) {
            showErrorNotification(R.string.no_recipients_specified, silent)
            throw Exception("Recipients not specified")
        }

        if (!isValidEmailAddressList(recipients)) {
            showErrorNotification(R.string.invalid_recipient, silent)
            throw Exception("Recipients are invalid")
        }

        return recipients
    }

    @Throws(IOException::class)
    private fun sendMessage(event: PhoneEvent, recipient: String?) {
        val formatter = MailFormatter(context, event).apply {
            setSendTime(Date())
            setContactName(contactName(context, event.phone))
            setDeviceName(settings.getDeviceName())
            setOptions(settings.getStringSet(PREF_EMAIL_CONTENT))
            setServiceAccount(settings.getString(PREF_REMOTE_CONTROL_ACCOUNT))
            setLocale(settings.getLocale())
        }

        val message = MailMessage().apply {
            subject = formatter.formatSubject()
            body = formatter.formatBody()
            recipients = recipient
            from = settings.getString(PREF_SENDER_ACCOUNT)
            replyTo = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
        }

        transport.send(message)
    }

    private fun showErrorNotification(reason: Int, silent: Boolean) {
        if (!silent) {
            notifications.showMailError(reason, Notifications.ACTION_SHOW_MAIN)
        }
    }
}