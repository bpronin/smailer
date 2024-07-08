package com.bopr.android.smailer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.android.smailer.MailFormatter.Companion.PHONE_SEARCH_TAG
import com.bopr.android.smailer.util.SharedPreferencesWrapper
import com.bopr.android.smailer.util.deviceName

/**
 * Settings.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Settings(context: Context) :
    SharedPreferencesWrapper(context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)) {

    val emailContent get() = getStringSet(PREF_EMAIL_CONTENT)
    val emailLocale get() = getString(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)!!
    val emailRecipients get() = getStringList(PREF_RECIPIENTS_ADDRESS)
    val emailRecipientsPlain get() = getString(PREF_RECIPIENTS_ADDRESS)
    val emailTriggers get() = getStringSet(PREF_EMAIL_TRIGGERS)
    val isNotifyRemoteControlActions get() = getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS)
    val isNotifySendSuccess get() = getBoolean(PREF_NOTIFY_SEND_SUCCESS)
    val isRemoteControlEnabled get() = getBoolean(PREF_REMOTE_CONTROL_ENABLED)
    val isRemoteControlFilterRecipients get() = getBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS)
    val isSyncEnabled get() = getBoolean(PREF_SYNC_ENABLED)
    val remoteControlAccount get() = getString(PREF_REMOTE_CONTROL_ACCOUNT)
    val senderAccount get() = getString(PREF_SENDER_ACCOUNT)
    val emailMessengerEnabled get() = getBoolean(PREF_EMAIL_MESSENGER_ENABLED)
    val telegramMessengerEnabled get() = getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)
    val telegramBotToken get() = getString(PREF_TELEGRAM_BOT_TOKEN)

    val deviceAlias: String
        get() {
            val setting = getString(PREF_DEVICE_ALIAS)
            return if (!setting.isNullOrEmpty()) setting else deviceName()
        }
    val phoneSearchUrl: String
        get() {
            val setting = getString(PREF_PHONE_SEARCH_URL)
            return if (!setting.isNullOrEmpty()) setting else DEFAULT_PHONE_SEARCH_URL
        }

    fun loadDefaults() = update {
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putBooleanOptional(PREF_NOTIFY_SEND_SUCCESS, false)
        putBooleanOptional(PREF_REMOTE_CONTROL_ENABLED, false)
        putBooleanOptional(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
        putBooleanOptional(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
        putBooleanOptional(PREF_SYNC_ENABLED, true)
        putStringOptional(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)
        putStringSetOptional(PREF_EMAIL_CONTENT, DEFAULT_EMAIL_CONTENT)
        putStringSetOptional(PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
        putBooleanOptional(PREF_TELEGRAM_MESSENGER_ENABLED, true)
        putBooleanOptional(PREF_EMAIL_MESSENGER_ENABLED, false)
    }

    companion object {

        var sharedPreferencesName = "com.bopr.android.smailer_preferences"

        private const val SETTINGS_VERSION = 2

        const val PREF_DEVICE_ALIAS = "device_alias"
        const val PREF_EMAIL_CONTENT = "email_content"
        const val PREF_EMAIL_LOCALE = "email_locale"
        const val PREF_EMAIL_TRIGGERS = "email_triggers"
        const val PREF_NOTIFY_SEND_SUCCESS = "notify_send_success"
        const val PREF_PHONE_SEARCH_URL = "phone_search_url"
        const val PREF_RECIPIENTS_ADDRESS = "recipients_address"
        const val PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account"
        const val PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled"
        const val PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients"
        const val PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications"
        const val PREF_SENDER_ACCOUNT = "sender_account"
        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */
        const val PREF_SYNC_ENABLED = "sync_enabled"
        const val PREF_TELEGRAM_BOT_TOKEN = "telegram_bot_token"
        const val PREF_EMAIL_MESSENGER_ENABLED = "email_messenger_enabled"
        const val PREF_TELEGRAM_MESSENGER_ENABLED = "telegram_messenger_enabled"

        const val VAL_PREF_DEFAULT = "default"
        const val VAL_PREF_EMAIL_CONTENT_CONTACT = "contact_name"
        const val VAL_PREF_EMAIL_CONTENT_DEVICE_NAME = "device_name"
        const val VAL_PREF_EMAIL_CONTENT_HEADER = "header"
        const val VAL_PREF_EMAIL_CONTENT_LOCATION = "location"
        const val VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME = "time"
        const val VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT = "time_sent"
        const val VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS = "remote_control_links"
        const val VAL_PREF_TRIGGER_IN_CALLS = "in_calls"
        const val VAL_PREF_TRIGGER_IN_SMS = "in_sms"
        const val VAL_PREF_TRIGGER_MISSED_CALLS = "missed_calls"
        const val VAL_PREF_TRIGGER_OUT_CALLS = "out_calls"
        const val VAL_PREF_TRIGGER_OUT_SMS = "out_sms"
        const val VAL_PREF_LOW_BATTERY_LEVEL = "low_battery_level"

        const val DEFAULT_PHONE_SEARCH_URL = "https://www.google.com/search?q=$PHONE_SEARCH_TAG"

        val DEFAULT_EMAIL_CONTENT: Set<String> = mutableSetOf(
            VAL_PREF_EMAIL_CONTENT_CONTACT,
            VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
            VAL_PREF_EMAIL_CONTENT_HEADER,
            VAL_PREF_EMAIL_CONTENT_LOCATION,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
            VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
        )

        val DEFAULT_TRIGGERS: Set<String> = mutableSetOf(
            VAL_PREF_TRIGGER_IN_SMS,
            VAL_PREF_TRIGGER_MISSED_CALLS,
            VAL_PREF_LOW_BATTERY_LEVEL
        )

    }

}