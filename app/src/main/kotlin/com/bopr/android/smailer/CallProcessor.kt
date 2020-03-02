package com.bopr.android.smailer

import android.Manifest.permission.READ_CONTACTS
import android.accounts.Account
import android.accounts.AccountsException
import android.content.Context
import com.bopr.android.smailer.Notifications.Companion.TARGET_MAIN
import com.bopr.android.smailer.Notifications.Companion.TARGET_RECIPIENTS
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.contactName
import com.bopr.android.smailer.util.getAccount
import com.bopr.android.smailer.util.isValidEmailAddressList
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

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
        event.processStatus = settings.callFilter.test(event)
        event.processTime = currentTimeMillis()
        if (event.processStatus != STATUS_ACCEPTED) {
            event.state = STATE_IGNORED
        } else if (startMailSession() && sendMail(event)) {
            event.state = STATE_PROCESSED
        }

        database.putEvent(event)
        database.notifyChanged()
    }

    /**
     * Sends out email for all pending events.
     */
    fun processPending() {
        val events = database.pendingEvents.list()
        if (events.isEmpty()) {
            log.debug("No pending events")
        } else {
            log.debug("Processing ${events.size} pending event(s)")

            if (startMailSession()) {
                for (event in events) {
                    event.processTime = currentTimeMillis()
                    if (sendMail(event)) {
                        event.state = STATE_PROCESSED
                        database.putEvent(event)
                    }
                }
            }
            database.notifyChanged()
        }
    }

    private fun startMailSession(): Boolean {
        log.debug("Starting session")

        return try {
            validateRecipient()
            transport.login(requireAccount(), GMAIL_SEND)
            transport.startSession()
            true
        } catch (x: Exception) {
            log.warn("Failed starting mail session: ", x)
            false
        }
    }

    private fun sendMail(event: PhoneEvent): Boolean {
        log.debug("Sending mail: $event")

        return try {
            val formatter = MailFormatter(context, event,
                    contactName = contactName(event.phone),
                    deviceName = settings.getString(PREF_DEVICE_ALIAS) ?: event.acceptor,
                    options = settings.getStringSet(PREF_EMAIL_CONTENT),
                    serviceAccount = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT),
                    locale = settings.locale
            )

            val message = MailMessage(
                    subject = formatter.formatSubject(),
                    body = formatter.formatBody(),
                    recipients = settings.getString(PREF_RECIPIENTS_ADDRESS),
                    from = settings.getString(PREF_SENDER_ACCOUNT),
                    replyTo = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
            )

            transport.send(message)

            notifications.cancelAllErrors()

            if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS, false)) {
                notifications.showMessage(R.string.email_successfully_send, TARGET_MAIN)
            }
            true
        } catch (x: UserRecoverableAuthIOException) {
            /* this occurs when app has no permission to access google account or
               account has been removed outside of the device */
            log.warn("Failed sending mail: ", x)

            notifications.showMailError(R.string.no_access_to_google_account, TARGET_MAIN)
            false
        } catch (x: Exception) {
            log.warn("Failed sending mail: ", x)

            false
        }
    }

    @Throws(AccountsException::class)
    private fun requireAccount(): Account {
        val accountName = settings.getString(PREF_SENDER_ACCOUNT)
        return getAccount(context, settings.getString(PREF_SENDER_ACCOUNT)) ?: run {
            notifications.showMailError(R.string.sender_account_not_found, TARGET_MAIN)
            throw AccountsException("Sender account [$accountName] not found")
        }
    }

    @Throws(Exception::class)
    private fun validateRecipient() {
        val recipients = settings.getString(PREF_RECIPIENTS_ADDRESS)

        if (recipients == null) {
            notifications.showMailError(R.string.no_recipients_specified, TARGET_RECIPIENTS)
            throw Exception("Recipients not specified")
        }

        if (!isValidEmailAddressList(recipients)) {
            notifications.showMailError(R.string.invalid_recipient, TARGET_RECIPIENTS)
            throw Exception("Recipients are invalid")
        }
    }

    private fun contactName(phone: String): String? {
        return if (context.checkPermission(READ_CONTACTS)) {
            contactName(context, phone)
        } else {
            log.warn("Missing required permission")
            null
        }
    }
}