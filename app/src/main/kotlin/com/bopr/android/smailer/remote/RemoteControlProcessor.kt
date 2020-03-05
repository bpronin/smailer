package com.bopr.android.smailer.remote

import android.Manifest.permission.SEND_SMS
import android.accounts.Account
import android.accounts.AccountsException
import android.content.Context
import androidx.annotation.StringRes
import com.bopr.android.smailer.*
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
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
 * Performs remote control actions.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class RemoteControlProcessor(
        private val context: Context,
        private val settings: Settings = Settings(context),
        private val notifications: Notifications = Notifications(context),
        private val smsTransport: SmsTransport = SmsTransport()) {

    private val parser = RemoteControlTaskParser()
    private val query = "subject:Re:[${context.getString(R.string.app_name)}] label:inbox"

    @Throws(AccountsException::class)
    fun handleMail() {
        val transport = GoogleMail(context)
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
                            performTask(task)
                            transport.trash(message)
                        }
                    }
                }
            }
        }
    }

    fun performTask(task: RemoteControlTask) {
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
                sendSms(task.arguments["phone"], task.arguments["text"])
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
        return getAccount(context, accountName) ?: run {
            notifications.showError(R.string.service_account_not_found, Notifications.TARGET_REMOTE_CONTROL)
            throw AccountsException("Service account [$accountName] not found")
        }
    }

    private fun deviceAlias(): String {
        return settings.getString(PREF_DEVICE_ALIAS) ?: deviceName()
    }

    private fun addTextToWhitelist(text: String?) {
        addToList(PREF_FILTER_TEXT_WHITELIST, text,
                R.string.text_remotely_added_to_whitelist, String::equals)
    }

    private fun removeTextFromWhitelist(text: String?) {
        removeFromList(PREF_FILTER_TEXT_WHITELIST, text,
                R.string.text_remotely_removed_from_whitelist, String::equals)
    }

    private fun addTextToBlacklist(text: String?) {
        addToList(PREF_FILTER_TEXT_BLACKLIST, text,
                R.string.text_remotely_added_to_blacklist, String::equals)
    }

    private fun removeTextFromBlacklist(text: String?) {
        removeFromList(PREF_FILTER_TEXT_BLACKLIST, text,
                R.string.text_remotely_removed_from_blacklist, String::equals)
    }

    private fun addPhoneToWhitelist(phone: String?) {
        addToList(PREF_FILTER_PHONE_WHITELIST, phone,
                R.string.phone_remotely_added_to_whitelist, ::samePhone)
    }

    private fun removePhoneFromWhitelist(phone: String?) {
        removeFromList(PREF_FILTER_PHONE_WHITELIST, phone,
                R.string.phone_remotely_removed_from_whitelist, ::samePhone)
    }

    private fun addPhoneToBlacklist(phone: String?) {
        addToList(PREF_FILTER_PHONE_BLACKLIST, phone,
                R.string.phone_remotely_added_to_blacklist, ::samePhone)
    }

    private fun removePhoneFromBlacklist(phone: String?) {
        removeFromList(PREF_FILTER_PHONE_BLACKLIST, phone,
                R.string.phone_remotely_removed_from_blacklist, ::samePhone)
    }

    private fun sendSms(phone: String?, message: String?) {
        if (context.checkPermission(SEND_SMS)) {
            smsTransport.sendMessage(phone, message)
            showNotification(context.getString(R.string.sent_sms, phone))

            log.debug("Sent SMS: $message to $phone")
        } else {
            log.warn("Missing required permission")
        }
    }

    private fun addToList(key: String, value: String?, @StringRes messageRes: Int,
                          compare: (String, String) -> Boolean) {
        value?.run {
            val set = settings.getCommaSet(key)
            if (set.none { compare(it, this) }) {
                settings.update {
                    putCommaSet(key, set.apply { add(value) })
                }
                showNotification(context.getString(messageRes, value))
            } else {
                log.debug("Already in list")
            }
        }
    }

    private fun removeFromList(key: String, value: String?, @StringRes messageRes: Int,
                               compare: (String, String) -> Boolean) {
        value?.run {
            val set = settings.getCommaSet(key)
            set.find {
                compare(it, this)
            }?.let {
                settings.update {
                    putCommaSet(key, set.apply { remove(it) })
                }
                showNotification(context.getString(messageRes, value))
            } ?: log.debug("Not in list")
        }
    }

    private fun showNotification(message: String) {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS)) {
            notifications.showRemoteAction(message)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("RemoteControlProcessor")
    }
}