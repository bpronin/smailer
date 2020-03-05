package com.bopr.android.smailer.remote

import android.accounts.Account
import android.accounts.AccountsException
import android.content.Intent
import androidx.core.app.JobIntentService
import com.bopr.android.smailer.*
import com.bopr.android.smailer.Notifications.Companion.TARGET_REMOTE_CONTROL
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.util.containsEmail
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.extractEmail
import com.bopr.android.smailer.util.getAccount
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM
import org.slf4j.LoggerFactory

/**
 * Remote control service.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RemoteControlService : JobIntentService() {

    private lateinit var settings: Settings
    private lateinit var query: String
    private lateinit var parser: RemoteControlTaskParser
    private lateinit var notifications: Notifications
    private lateinit var processor: RemoteControlProcessor

    override fun onCreate() {
        super.onCreate()
        parser = RemoteControlTaskParser()
        processor = RemoteControlProcessor(this)
        settings = Settings(this)
        query = "subject:Re:[${getString(R.string.app_name)}] label:inbox"
    }

    override fun onHandleWork(intent: Intent) {
        log.trace("Handling intent: $intent")

        try {
            val transport = GoogleMail(this)
            transport.login(requireAccount(), MAIL_GOOGLE_COM)

            val messages = transport.list(query)
            if (messages.isEmpty()) {
                log.debug("No service mail")
                return
            }

            for (message in messages) {
                if (acceptMessage(message)) {
                    message.body?.let {
                        val task = parser.parse(it)
                        when {
                            task == null ->
                                log.debug("Not a service mail")
                            deviceAlias() != task.acceptor ->
                                log.debug("Not my mail")
                            else -> {
                                transport.markAsRead(message)
                                processor.perform(task)
                                transport.trash(message)
                            }
                        }
                    }
                }
            }
        } catch (x: Exception) {
            log.error("Remote control error", x)
        }
    }

    private fun acceptMessage(message: MailMessage): Boolean {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS)) {
            val address = extractEmail(message.from)!!
            val recipients = settings.getCommaList(PREF_RECIPIENTS_ADDRESS)
            if (!containsEmail(recipients, address)) {
                log.debug("Address $address rejected")

                return false
            }
        }
        return true
    }

    @Throws(AccountsException::class)
    private fun requireAccount(): Account {
        val accountName = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
        return getAccount(this, accountName) ?: run {
            notifications.showError(R.string.service_account_not_found, TARGET_REMOTE_CONTROL)
            throw AccountsException("Service account [$accountName] not found")
        }
    }

    private fun deviceAlias(): String {
        return settings.getString(PREF_DEVICE_ALIAS) ?: deviceName()
    }

    companion object {

        private val log = LoggerFactory.getLogger("RemoteControlService")
    }
}