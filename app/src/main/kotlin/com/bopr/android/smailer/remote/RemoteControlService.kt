package com.bopr.android.smailer.remote

import android.Manifest.permission.SEND_SMS
import android.accounts.Account
import android.accounts.AccountsException
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import androidx.core.app.JobIntentService
import com.bopr.android.smailer.*
import com.bopr.android.smailer.Notifications.Companion.TARGET_REMOTE_CONTROL
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.SEND_SMS_TO_CALLER
import com.bopr.android.smailer.util.*
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

    override fun onCreate() {
        super.onCreate()
        parser = RemoteControlTaskParser()
        notifications = Notifications(this)
        settings = Settings(this)
        query = String.format("subject:Re:[%s] label:inbox", getString(R.string.app_name))
    }

    override fun onHandleWork(intent: Intent) {
        log.debug("Handling intent: $intent")

        try {
            val transport = GoogleMail(this)
            transport.login(requireAccount(), MAIL_GOOGLE_COM)
            transport.startSession()

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
                                performTask(task)
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

    private fun performTask(task: RemoteControlTask) {
        log.debug("Processing: $task")

        when (task.action) {
            ADD_PHONE_TO_BLACKLIST ->
                addPhoneToBlacklist(task.argument)
            REMOVE_PHONE_FROM_BLACKLIST ->
                removePhoneFromBlacklist(task.argument)
            ADD_PHONE_TO_WHITELIST ->
                addPhoneToWhitelist(task.argument)
            REMOVE_PHONE_FROM_WHITELIST ->
                removePhoneFromWhitelist(task.argument)
            ADD_TEXT_TO_BLACKLIST ->
                addTextToBlacklist(task.argument)
            REMOVE_TEXT_FROM_BLACKLIST ->
                removeTextFromBlacklist(task.argument)
            ADD_TEXT_TO_WHITELIST ->
                addTextToWhitelist(task.argument)
            REMOVE_TEXT_FROM_WHITELIST ->
                removeTextFromWhitelist(task.argument)
            SEND_SMS_TO_CALLER ->
                sendSms(task.arguments["text"], task.arguments["phone"])
        }
    }

    private fun removeTextFromWhitelist(text: String?) {
        text?.let {
            with(settings.callFilter) {
                removeFromTextList(this, textWhitelist, it, R.string.text_remotely_removed_from_whitelist)
            }
        }
    }

    private fun addTextToWhitelist(text: String?) {
        text?.let {
            with(settings.callFilter) {
                addToTextList(this, textWhitelist, it, R.string.text_remotely_added_to_whitelist)
            }
        }
    }

    private fun removeTextFromBlacklist(text: String?) {
        text?.let {
            with(settings.callFilter) {
                removeFromTextList(this, textBlacklist, it, R.string.text_remotely_removed_from_blacklist)
            }
        }
    }

    private fun addTextToBlacklist(text: String?) {
        text?.let {
            with(settings.callFilter) {
                addToTextList(this, textBlacklist, it, R.string.text_remotely_added_to_blacklist)
            }
        }
    }

    private fun removePhoneFromWhitelist(phone: String?) {
        phone?.let {
            with(settings.callFilter) {
                removeFromPhoneList(this, phoneWhitelist, it, R.string.phone_remotely_removed_from_whitelist)
            }
        }
    }

    private fun addPhoneToWhitelist(phone: String?) {
        phone?.let {
            with(settings.callFilter) {
                addToPhoneList(this, phoneWhitelist, it, R.string.phone_remotely_added_to_whitelist)
            }
        }
    }

    private fun removePhoneFromBlacklist(phone: String?) {
        phone?.let {
            with(settings.callFilter) {
                removeFromPhoneList(this, phoneBlacklist, it, R.string.phone_remotely_removed_from_blacklist)
            }
        }
    }

    private fun addPhoneToBlacklist(phone: String?) {
        phone?.let {
            with(settings.callFilter) {
                addToPhoneList(this, phoneBlacklist, it, R.string.phone_remotely_added_to_blacklist)
            }
        }
    }

    private fun addToTextList(filter: PhoneEventFilter, list: MutableSet<String>, value: String, messageRes: Int) {
        if (!list.contains(value)) {
            list.add(value)
            settings.callFilter = filter
            showNotification(getString(messageRes, value))
        } else {
            log.debug("Already in list")
        }
    }

    private fun addToPhoneList(filter: PhoneEventFilter, list: MutableSet<String>, value: String, messageRes: Int) {
        if (!containsPhone(list, value)) {
            list.add(value)
            settings.callFilter = filter
            showNotification(getString(messageRes, value))
        } else {
            log.debug("Already in list")
        }
    }

    private fun removeFromTextList(filter: PhoneEventFilter, list: MutableSet<String>,
                                   text: String, messageRes: Int) {
        list.remove(text)
        settings.callFilter = filter
        showNotification(getString(messageRes, text))
    }

    private fun removeFromPhoneList(filter: PhoneEventFilter, list: MutableSet<String>,
                                    number: String, messageRes: Int) {
        /* searching by normalized form */
        findPhone(list, number)?.let {
            list.remove(it)
            settings.callFilter = filter
            showNotification(getString(messageRes, number))
        }
    }

    private fun sendSms(message: String?, phone: String?) {
        if (checkPermission(SEND_SMS)) {
            SmsManager.getDefault().run {
                sendMultipartTextMessage(phone, null, divideMessage(message), null, null)
            }
            showNotification(getString(R.string.sent_sms, phone))

            log.debug("Sent SMS: $message to $phone")
        } else {
            log.warn("Missing required permission")
        }
    }

    private fun showNotification(message: String) {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS)) {
            notifications.showRemoteAction(message)
        }
    }

    private fun deviceAlias(): String {
        return settings.getString(PREF_DEVICE_ALIAS) ?: deviceName()
    }

    companion object {

        private val log = LoggerFactory.getLogger("RemoteControlService")
        private const val JOB_ID = 1002

        fun startRemoteControlService(context: Context) {
            log.debug("Starting service")

            enqueueWork(context, RemoteControlService::class.java, JOB_ID,
                    Intent(context, RemoteControlService::class.java))
        }
    }
}