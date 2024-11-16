package com.bopr.android.smailer.control

import android.Manifest.permission.SEND_SMS
import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_PHONE_TO_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_PHONE_TO_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_TEXT_TO_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_TEXT_TO_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_PHONE_FROM_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_PHONE_FROM_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_TEXT_FROM_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_TEXT_FROM_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.SEND_SMS_TO_CALLER
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.StringDataset
import com.bopr.android.smailer.ui.EventFilterPhoneBlacklistActivity
import com.bopr.android.smailer.ui.EventFilterPhoneWhitelistActivity
import com.bopr.android.smailer.ui.EventFilterTextBlacklistActivity
import com.bopr.android.smailer.ui.EventFilterTextWhitelistActivity
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.sendSmsMessage
import com.bopr.android.smailer.util.useIt
import com.bopr.android.smailer.util.Logger
import kotlin.reflect.KClass

/**
 * Executes application control commands.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class ControlCommandExecutor(
    private val context: Context,
    private val database: Database = Database(context),
    private val settings: Settings = Settings(context),
    private val notifications: NotificationsHelper = NotificationsHelper(context),
) {

    fun execute(command: ControlCommand) {
        log.debug("Executing: $command")

        when (command.action) {
            ADD_PHONE_TO_BLACKLIST ->
                addPhoneToBlacklist(command.argument)

            REMOVE_PHONE_FROM_BLACKLIST ->
                removePhoneFromBlacklist(command.argument)

            ADD_PHONE_TO_WHITELIST ->
                addPhoneToWhitelist(command.argument)

            REMOVE_PHONE_FROM_WHITELIST ->
                removePhoneFromWhitelist(command.argument)

            ADD_TEXT_TO_BLACKLIST ->
                addTextToBlacklist(command.argument)

            REMOVE_TEXT_FROM_BLACKLIST ->
                removeTextFromBlacklist(command.argument)

            ADD_TEXT_TO_WHITELIST ->
                addTextToWhitelist(command.argument)

            REMOVE_TEXT_FROM_WHITELIST ->
                removeTextFromWhitelist(command.argument)

            SEND_SMS_TO_CALLER ->
                sendSms(command.arguments["phone"], command.arguments["text"])
        }
    }

    private fun addTextToWhitelist(text: String?) {
        addToFilterList(
            database.smsTextWhitelist, text,
            R.string.text_remotely_added_to_whitelist,
            EventFilterTextWhitelistActivity::class
        )
    }

    private fun removeTextFromWhitelist(text: String?) {
        removeFromFilterList(
            database.smsTextWhitelist,
            text,
            R.string.text_remotely_removed_from_whitelist,
            EventFilterTextWhitelistActivity::class
        )
    }

    private fun addTextToBlacklist(text: String?) {
        addToFilterList(
            database.smsTextBlacklist,
            text,
            R.string.text_remotely_added_to_blacklist,
            EventFilterTextBlacklistActivity::class
        )
    }

    private fun removeTextFromBlacklist(text: String?) {
        removeFromFilterList(
            database.smsTextBlacklist,
            text,
            R.string.text_remotely_removed_from_blacklist,
            EventFilterTextBlacklistActivity::class
        )
    }

    private fun addPhoneToWhitelist(phone: String?) {
        addToFilterList(
            database.phoneWhitelist,
            phone,
            R.string.phone_remotely_added_to_whitelist,
            EventFilterPhoneWhitelistActivity::class
        )
    }

    private fun removePhoneFromWhitelist(phone: String?) {
        removeFromFilterList(
            database.phoneWhitelist,
            phone,
            R.string.phone_remotely_removed_from_whitelist,
            EventFilterPhoneWhitelistActivity::class
        )
    }

    private fun addPhoneToBlacklist(phone: String?) {
        addToFilterList(
            database.phoneBlacklist,
            phone,
            R.string.phone_remotely_added_to_blacklist,
            EventFilterPhoneBlacklistActivity::class
        )
    }

    private fun removePhoneFromBlacklist(phone: String?) {
        removeFromFilterList(
            database.phoneBlacklist,
            phone,
            R.string.phone_remotely_removed_from_blacklist,
            EventFilterPhoneBlacklistActivity::class
        )
    }

    private fun sendSms(phone: String?, message: String?) {
        if (context.checkPermission(SEND_SMS)) {
            context.sendSmsMessage(phone, message)
            notifySuccess(context.getString(R.string.sent_sms, phone), MainActivity::class)

            log.debug("Sent SMS: $message to $phone")
        } else {
            log.warn("Missing required permission")
        }
    }

    private fun addToFilterList(
        list: StringDataset,
        value: String?,
        @StringRes messageRes: Int,
        target: KClass<out Activity>
    ) {
        if (!value.isNullOrEmpty()) {
            database.useIt {
                if (commit { list.add(value) }) {
                    notifySuccess(context.getString(messageRes, value), target)
                } else {
                    log.debug("Already in list")
                }
            }
        }
    }

    private fun removeFromFilterList(
        list: StringDataset,
        value: String?,
        @StringRes messageRes: Int,
        target: KClass<out Activity>
    ) {
        if (!value.isNullOrEmpty()) {
            database.useIt {
                if (commit { list.remove(value) }) {
                    notifySuccess(context.getString(messageRes, value), target)
                } else {
                    log.debug("Not in list")
                }
            }
        }
    }

    private fun notifySuccess(message: String, target: KClass<out Activity>) {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS)) {
            notifications.notifyInfo(
                context.getString(R.string.remote_action),
                message,
                target
            )
        }
    }

    companion object {

        private val log = Logger("ControlCommandProcessor")
    }
}