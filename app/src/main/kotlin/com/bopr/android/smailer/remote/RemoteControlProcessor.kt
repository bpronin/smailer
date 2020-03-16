package com.bopr.android.smailer.remote

import android.Manifest.permission.SEND_SMS
import android.accounts.Account
import android.content.Context
import androidx.annotation.StringRes
import com.bopr.android.smailer.*
import com.bopr.android.smailer.Database.Companion.TABLE_PHONE_BLACKLIST
import com.bopr.android.smailer.Database.Companion.TABLE_PHONE_WHITELIST
import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_BLACKLIST
import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_WHITELIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_MAIN
import com.bopr.android.smailer.Notifications.Companion.TARGET_PHONE_BLACKLIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_PHONE_WHITELIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_TEXT_BLACKLIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_TEXT_WHITELIST
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
import com.google.android.gms.common.internal.Preconditions.checkNotMainThread
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM
import org.slf4j.LoggerFactory

/**
 * Performs remote control actions.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class RemoteControlProcessor(
        private val context: Context,
        private val database: Database = Database(context),
        private val settings: Settings = Settings(context),
        private val notifications: Notifications = Notifications(context),
        private val smsTransport: SmsTransport = SmsTransport()) {

    private val parser = RemoteControlTaskParser()
    private val query = "subject:Re:[${context.getString(R.string.app_name)}] label:inbox"

    fun checkMailbox(): Int {
        checkNotMainThread() /* gmail won't work in main thread */
        if (!checkInternet()) return 0
        val account = requireAccount() ?: return 0

        val transport = GoogleMail(context)
        transport.login(account, MAIL_GOOGLE_COM)
        val messages = transport.list(query)
        if (messages.isNotEmpty()) {
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
        } else {
            log.debug("No service mail")
        }

        return messages.size
    }

    internal fun performTask(task: RemoteControlTask) {
        log.debug("Processing: $task")
        database.use {
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
    }

    private fun acceptMessage(message: MailMessage): Boolean {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS)) {
            val address = extractEmail(message.from)!!
            val recipients = settings.getStringList(PREF_RECIPIENTS_ADDRESS)
            if (!containsEmail(recipients, address)) {
                log.debug("Address $address rejected")

                return false
            }
        }
        return true
    }

    private fun checkInternet(): Boolean {
        /* check it before all to avoid awaiting timeout while sending */
        return context.hasInternetConnection().also {
            if (!it) log.warn("No internet connection")
        }
    }

    private fun requireAccount(): Account? {
        val name = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
        return context.getAccount(name).also {
            if (it == null) {
                notifications.showRemoteAccountError()
                log.warn("Service account [$name] not found")
            }
        }
    }

    private fun deviceAlias(): String {
        return settings.getString(PREF_DEVICE_ALIAS) ?: deviceName()
    }

    private fun addTextToWhitelist(text: String?) {
        addToFilterList(TABLE_TEXT_WHITELIST, text,
                R.string.text_remotely_added_to_whitelist, TARGET_TEXT_WHITELIST)
    }

    private fun removeTextFromWhitelist(text: String?) {
        removeFromFilterList(TABLE_TEXT_WHITELIST, text,
                R.string.text_remotely_removed_from_whitelist, TARGET_TEXT_WHITELIST)
    }

    private fun addTextToBlacklist(text: String?) {
        addToFilterList(TABLE_TEXT_BLACKLIST, text,
                R.string.text_remotely_added_to_blacklist, TARGET_TEXT_BLACKLIST)
    }

    private fun removeTextFromBlacklist(text: String?) {
        removeFromFilterList(TABLE_TEXT_BLACKLIST, text,
                R.string.text_remotely_removed_from_blacklist, TARGET_TEXT_BLACKLIST)
    }

    private fun addPhoneToWhitelist(phone: String?) {
        addToFilterList(TABLE_PHONE_WHITELIST, phone,
                R.string.phone_remotely_added_to_whitelist, TARGET_PHONE_WHITELIST)
    }

    private fun removePhoneFromWhitelist(phone: String?) {
        removeFromFilterList(TABLE_PHONE_WHITELIST, phone,
                R.string.phone_remotely_removed_from_whitelist, TARGET_PHONE_WHITELIST)
    }

    private fun addPhoneToBlacklist(phone: String?) {
        addToFilterList(TABLE_PHONE_BLACKLIST, phone,
                R.string.phone_remotely_added_to_blacklist, TARGET_PHONE_BLACKLIST)
    }

    private fun removePhoneFromBlacklist(phone: String?) {
        removeFromFilterList(TABLE_PHONE_BLACKLIST, phone,
                R.string.phone_remotely_removed_from_blacklist, TARGET_PHONE_BLACKLIST)
    }

    private fun sendSms(phone: String?, message: String?) {
        if (context.checkPermission(SEND_SMS)) {
            smsTransport.sendMessage(phone, message)
            showNotification(context.getString(R.string.sent_sms, phone), TARGET_MAIN)

            log.debug("Sent SMS: $message to $phone")
        } else {
            log.warn("Missing required permission")
        }
    }

    private fun addToFilterList(listName: String, value: String?, @StringRes messageRes: Int,
                                @Notifications.Target target: Int) {
        if (!value.isNullOrEmpty()) {
            if (database.notifying { putFilterListItem(listName, value) }) {
                showNotification(context.getString(messageRes, value), target)
            } else {
                log.debug("Already in list")
            }
        }
    }

    private fun removeFromFilterList(listName: String, value: String?, @StringRes messageRes: Int,
                                     @Notifications.Target target: Int) {
        if (!value.isNullOrEmpty()) {
            if (database.notifying { deleteFilterListItem(listName, value) }) {
                showNotification(context.getString(messageRes, value), target)
            } else {
                log.debug("Not in list")
            }
        }
    }

    private fun showNotification(message: String, @Notifications.Target target: Int) {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS)) {
            notifications.showRemoteAction(message, target)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("RemoteControlProcessor")
    }
}