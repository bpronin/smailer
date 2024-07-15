package com.bopr.android.smailer.provider.telephony

import android.content.Context
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.consumer.EventMessenger
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.util.GeoLocator
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

/**
 * Sends out messages for phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneEventProcessor(
    private val context: Context,
    private val database: Database = Database(context),
    private val messenger: EventMessenger = EventMessenger(context),
    private val notifications: NotificationsHelper = NotificationsHelper(context),
    private val locator: GeoLocator = GeoLocator(context, database)
) {

    private val settings: Settings = Settings(context)

    fun process(event: PhoneEventInfo) {
        log.debug("Processing: {}", event)

        database.use {
            event.apply {
                location = locator.getLocation()
                processStatus = eventFilter().test(this)
                processTime = currentTimeMillis()

                if (processStatus != STATUS_ACCEPTED) {
                    state = STATE_IGNORED
                    log.debug("Ignored")
                } else if (startMailSession() && sendMessage(this)) {
                    state = STATE_PROCESSED
                    log.debug("Processed")
                } else {
                    state = STATE_PENDING
                    log.debug("Postponed")
                }
            }

            database.commit {
                phoneEvents.add(event)
            }
        }
    }

    fun processPending() {
        database.use {
            val pendingEvents = database.phoneEvents.filterPending
            if (pendingEvents.isEmpty()) {
                log.debug("No pending events")
            } else {
                log.debug("Processing ${pendingEvents.size} pending event(s)")

                if (startMailSession()) {
                    database.commit {
                        batch {
                            for (event in pendingEvents) {
                                event.processTime = currentTimeMillis()
                                if (sendMessage(event)) {
                                    event.state = STATE_PROCESSED
                                    phoneEvents.add(event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMailSession(): Boolean {
//        if (checkInternet() && checkRecipient()) {
//            requireAccount()?.let {
//                messenger.startSession()
//                log.debug("Mail session started")
        return true
//            }
//        }
//        return false
    }

    private fun sendMessage(event: PhoneEventInfo): Boolean {
        return try {
            messenger.sendMessageFor(event)

            log.debug("Event message sent")

            if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS)) {
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

    private fun eventFilter() = PhoneEventFilter(
        settings.getEmailTriggers(),
        database.phoneBlacklist,
        database.phoneWhitelist,
        database.smsTextBlacklist,
        database.smsTextWhitelist
    )

    companion object {

        private val log = LoggerFactory.getLogger("PhoneEventProcessor")
    }

}