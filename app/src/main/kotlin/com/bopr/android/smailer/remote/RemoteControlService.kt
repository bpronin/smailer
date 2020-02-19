package com.bopr.android.smailer.remote

import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import androidx.core.app.JobIntentService
import com.bopr.android.smailer.*
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
import com.bopr.android.smailer.util.AddressUtil.containsEmail
import com.bopr.android.smailer.util.AddressUtil.extractEmail
import com.bopr.android.smailer.util.AddressUtil.findPhone
import com.google.api.services.gmail.GmailScopes
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
            transport.startSession(requireAccount(), GmailScopes.MAIL_GOOGLE_COM)

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
                            settings.getDeviceName() != task.acceptor ->
                                log.debug("Not my service mail")
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
        if (settings.getBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, false)) {
            val address = extractEmail(message.from)!!
            val recipients = settings.getCommaSet(PREF_RECIPIENTS_ADDRESS)
            if (!containsEmail(recipients, address)) {
                log.debug("Address $address rejected")

                return false
            }
        }
        return true
    }

    @Throws(Exception::class)
    private fun requireAccount(): String {
        val account = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
        if (account.isNullOrEmpty()) {
            notifications.showError(R.string.service_account_not_specified, Notifications.ACTION_SHOW_REMOTE_CONTROL)
            throw Exception("Service account not specified")
        }
        return account
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
            with(settings.getFilter()) {
                removeFromTextList(this, textWhitelist, it, R.string.text_remotely_removed_from_whitelist)
            }
        }
    }

    private fun addTextToWhitelist(text: String?) {
        text?.let {
            with(settings.getFilter()) {
                addToFilterList(this, textWhitelist, it, R.string.text_remotely_added_to_whitelist)
            }
        }
    }

    private fun removeTextFromBlacklist(text: String?) {
        text?.let {
            with(settings.getFilter()) {
                removeFromTextList(this, textBlacklist, it, R.string.text_remotely_removed_from_blacklist)
            }
        }
    }

    private fun addTextToBlacklist(text: String?) {
        text?.let {
            with(settings.getFilter()) {
                addToFilterList(this, textBlacklist, it, R.string.text_remotely_added_to_blacklist)
            }
        }
    }

    private fun removePhoneFromWhitelist(phone: String?) {
        phone?.let {
            with(settings.getFilter()) {
                removeFromPhoneList(this, phoneWhitelist, it, R.string.phone_remotely_removed_from_whitelist)
            }
        }
    }

    private fun addPhoneToWhitelist(phone: String?) {
        phone?.let {
            with(settings.getFilter()) {
                addToFilterList(this, phoneWhitelist, it, R.string.phone_remotely_added_to_whitelist)
            }
        }
    }

    private fun removePhoneFromBlacklist(phone: String?) {
        phone?.let {
            with(settings.getFilter()) {
                removeFromPhoneList(this, phoneBlacklist, it, R.string.phone_remotely_removed_from_blacklist)
            }
        }
    }

    private fun addPhoneToBlacklist(phone: String?) {
        phone?.let {
            with(settings.getFilter()) {
                addToFilterList(this, phoneBlacklist, it, R.string.phone_remotely_added_to_blacklist)
            }
        }
    }

    private fun addToFilterList(filter: PhoneEventFilter, list: MutableSet<String>, text: String, messageRes: Int) {
        if (!list.contains(text)) {
            list.add(text)
            saveFilter(filter, text, messageRes)
        } else {
            log.debug("Already in list")
        }
    }

    private fun removeFromTextList(filter: PhoneEventFilter, list: MutableSet<String>, text: String, messageRes: Int) {
        if (list.contains(text)) {
            list.remove(text)
            saveFilter(filter, text, messageRes)
        } else {
            log.debug("Not in list")
        }
    }

    private fun removeFromPhoneList(filter: PhoneEventFilter, list: MutableSet<String>, number: String, messageRes: Int) {
        findPhone(list, number)?.let {
            list.remove(it)
            saveFilter(filter, number, messageRes)
        } ?: log.debug("Not in list")
    }

    private fun sendSms(message: String?, phone: String?) {
        with(SmsManager.getDefault()) {
            sendMultipartTextMessage(phone, null, divideMessage(message), null, null)
        }

        log.debug("Sent SMS: $message to $phone")
    }

    private fun saveFilter(filter: PhoneEventFilter, text: String, messageRes: Int) {
        settings.edit().putFilter(filter).apply()
        if (settings.getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, false)) {
            notifications.showRemoteAction(messageRes, text)
        }
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