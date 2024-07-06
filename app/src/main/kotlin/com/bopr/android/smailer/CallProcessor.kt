package com.bopr.android.smailer

import android.Manifest.permission.READ_CONTACTS
import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.contactName
import com.bopr.android.smailer.util.getAccount
import com.bopr.android.smailer.util.hasInternetConnection
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.util.*

/**
 * Sends out email for phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallProcessor(
        private val context: Context,
        private val database: Database = Database(context),
        private val transport: GoogleMail = GoogleMail(context),
        private val notifications: Notifications = Notifications(context),
        private val locator: GeoLocator = GeoLocator(context, database)) {

    private val log = LoggerFactory.getLogger("CallProcessor")
    private val settings: Settings = Settings(context)
    private val recipientsValidator = RecipientsValidator(notifications)

    /**
     * Sends out a mail for event.
     *
     * @param event email event
     */
    fun process(event: PhoneEvent) {
        log.debug("Processing: {}", event)

        database.use {
            event.apply {
                location = locator.getLocation()
                processStatus = eventFilter().test(this)
                processTime = currentTimeMillis()

                if (processStatus != STATUS_ACCEPTED) {
                    state = STATE_IGNORED
                    log.debug("Ignored")
                } else if (startMailSession() && sendMail(this)) {
                    state = STATE_PROCESSED
                    log.debug("Processed")
                } else {
                    state = STATE_PENDING
                    log.debug("Postponed")
                }
            }

            database.commit { events.add(event) }
        }
    }

    /**
     * Sends out email for all pending events.
     */
    fun processPending() {
        database.use {
            val pendingEvents = database.events.filterPending
            if (pendingEvents.isEmpty()) {
                log.debug("No pending events")
            } else {
                log.debug("Processing ${pendingEvents.size} pending event(s)")

                if (startMailSession()) {
                    database.commit {
                        batch {
                            for (event in pendingEvents) {
                                event.processTime = currentTimeMillis()
                                if (sendMail(event)) {
                                    event.state = STATE_PROCESSED
                                    events.add(event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMailSession(): Boolean {
        if (checkInternet() && checkRecipient()) {
            requireAccount()?.let {
                transport.login(it, GMAIL_SEND)
                log.debug("Mail session started")
                return true
            }
        }
        return false
    }

    private fun sendMail(event: PhoneEvent): Boolean {
        val formatter = MailFormatter(
                context = context,
                event = event,
                contactName = contactName(event.phone),
                deviceName = settings.deviceAlias,
                options = settings.emailContent,
                serviceAccount = settings.remoteControlAccount,
                phoneSearchUrl = settings.phoneSearchUrl,
                locale = parseLocale(settings.emailLocale)
        )

        val message = MailMessage(
                subject = formatter.formatSubject(),
                body = formatter.formatBody(),
                recipients = settings.emailRecipientsPlain,
                from = settings.senderAccount,
                replyTo = settings.remoteControlAccount
        )

        return try {
            transport.send(message)

            log.debug("Mail sent")

            if (settings.isNotifySendSuccess) {
                notifications.showMailSendSuccess()
            }
            true
        } catch (x: UserRecoverableAuthIOException) {
            /* this occurs when app has no permission to access google account or
               sender account has been removed from outside of the device */
            log.warn("Failed sending mail: ", x)

            notifications.showGoogleAccessError()
            false
        } catch (x: Exception) {
            log.warn("Failed sending mail: ", x)

            false
        }
    }

    private fun checkInternet(): Boolean {
        /* check it before all to avoid awaiting timeout while sending */
        return context.hasInternetConnection().also {
            if (!it) log.warn("No internet connection")
        }
    }

    private fun checkRecipient(): Boolean {
//        val recipients = settings.emailRecipients
//
//        if (recipients.isEmpty()) {
//            notifications.showRecipientsError(R.string.no_recipients_specified)
//
//            log.warn("Recipients not specified")
//            return false
//        }
//
//        if (!isValidEmailAddressList(recipients)) {
//            notifications.showRecipientsError(R.string.invalid_recipient)
//
//            log.warn("Recipients are invalid")
//            return false
//        }
//
//        return true
        return recipientsValidator.checkRecipients(settings.emailRecipients)
    }

    private fun requireAccount(): Account? {
        val name = settings.senderAccount
        return context.getAccount(name).also {
            if (it == null) {
                notifications.showSenderAccountError()
                log.warn("Sender account [$name] not found")
            }
        }
    }

    private fun eventFilter() = PhoneEventFilter(
            settings.emailTriggers,
            database.phoneBlacklist,
            database.phoneWhitelist,
            database.textBlacklist,
            database.textWhitelist
    )

    private fun contactName(phone: String): String? {
        return if (context.checkPermission(READ_CONTACTS)) {
            contactName(context, phone)
        } else {
            log.warn("Missing required permission")
            null
        }
    }

    private fun parseLocale(code: String): Locale {
        return if (code == VAL_PREF_DEFAULT) {
            Locale.getDefault()
        } else {
            val a = code.split("_")
            if (a.size == 2) {
                Locale(a[0], a[1])
            } else {
                throw IllegalArgumentException("Invalid locale code: $code")
            }
        }
    }
}