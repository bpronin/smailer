package com.bopr.android.smailer.remote

import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import androidx.core.app.JobIntentService
import com.bopr.android.smailer.*
import com.bopr.android.smailer.Settings.*
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.SEND_SMS_TO_CALLER
import com.bopr.android.smailer.util.AddressUtil
import com.bopr.android.smailer.util.Util.commaSplit
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

    @Override
    override fun onCreate() {
        super.onCreate()
        parser = RemoteControlTaskParser()
        notifications = Notifications(this)
        settings = Settings(this)
        query = String.format("subject:Re:[%s] label:inbox", getString(R.string.app_name))
    }

    @Override
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
                            settings.deviceName != task.acceptor ->
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
            val address = AddressUtil.extractEmail(message.from)
            val recipients = commaSplit(settings.getString(PREF_RECIPIENTS_ADDRESS, ""))
            if (!AddressUtil.containsEmail(recipients, address)) {
                log.debug("Address $address rejected")
                return false
            }
        }
        return true
    }

    private fun requireAccount(): String {
        val account = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT, null)
        if (account == null) {
            notifications.showError(R.string.service_account_not_specified, Notifications.ACTION_SHOW_REMOTE_CONTROL)
            throw IllegalArgumentException("Service account not specified")
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
        with(settings.filter) {
            text?.let {
                removeFromTextList(this, textWhitelist, it, R.string.text_remotely_removed_from_whitelist)
            }
        }
    }

    private fun addTextToWhitelist(text: String?) {
        with(settings.filter) {
            text?.let {
                addToFilterList(this, textWhitelist, it, R.string.text_remotely_added_to_whitelist)
            }
        }
    }

    private fun removeTextFromBlacklist(text: String?) {
        with(settings.filter) {
            text?.let {
                removeFromTextList(this, textBlacklist, it, R.string.text_remotely_removed_from_blacklist)
            }
        }
    }

    private fun addTextToBlacklist(text: String?) {
        with(settings.filter) {
            text?.let {
                addToFilterList(this, textBlacklist, it, R.string.text_remotely_added_to_blacklist)
            }
        }
    }

    private fun removePhoneFromWhitelist(phone: String?) {
        with(settings.filter) {
            phone?.let {
                removeFromPhoneList(this, phoneWhitelist, it, R.string.phone_remotely_removed_from_whitelist)
            }
        }
    }

    private fun addPhoneToWhitelist(phone: String?) {
        with(settings.filter) {
            phone?.let {
                addToFilterList(this, phoneWhitelist, it, R.string.phone_remotely_added_to_whitelist)
            }
        }
    }

    private fun removePhoneFromBlacklist(phone: String?) {
        with(settings.filter) {
            phone?.let {
                removeFromPhoneList(this, phoneBlacklist, it, R.string.phone_remotely_removed_from_blacklist)
            }
        }
    }

    private fun addPhoneToBlacklist(phone: String?) {
        with(settings.filter) {
            phone?.let {
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
        AddressUtil.findPhone(list, number)?.let {
            list.remove(it)
            saveFilter(filter, number, messageRes)
        } ?: log.debug("Not in list")
    }

    private fun sendSms(message: String?, phone: String?) {
        val manager = SmsManager.getDefault()
        manager.sendMultipartTextMessage(phone, null, manager.divideMessage(message), null, null)
        log.debug("Sent SMS: $message to $phone")
    }

    private fun saveFilter(filter: PhoneEventFilter, text: String?, messageRes: Int) {
        settings.putFilter(filter)
        if (settings.getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, false)) {
            notifications.showRemoteAction(messageRes, text)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger("RemoteControlService")
        private const val JOB_ID = 1002

        fun start(context: Context) {
            log.debug("Starting service")
            enqueueWork(context, RemoteControlService::class.java, JOB_ID,
                    Intent(context, RemoteControlService::class.java))
        }
    }
}