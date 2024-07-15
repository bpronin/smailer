package com.bopr.android.smailer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.android.smailer.processor.mail.MailPhoneEventFormatter.Companion.PHONE_SEARCH_TAG
import com.bopr.android.smailer.util.SharedPreferencesWrapper
import com.bopr.android.smailer.util.DEVICE_NAME

/**
 * Settings.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Settings(context: Context) :
    SharedPreferencesWrapper(context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)) {

    fun getEmailContent() = getStringSet(PREF_EMAIL_CONTENT)
    fun getEmailTriggers() = getStringSet(PREF_EMAIL_TRIGGERS)
    fun getEmailRecipients() = requireString(PREF_RECIPIENTS_ADDRESS, "")
    fun getMessageLocale() = requireString(PREF_MESSAGE_LOCALE, VAL_PREF_DEFAULT)
    fun getDeviceName() = requireString(PREF_DEVICE_ALIAS, DEVICE_NAME)
    fun getPhoneSearchUrl() = requireString(PREF_PHONE_SEARCH_URL, DEFAULT_PHONE_SEARCH_URL)

    fun loadDefaults() = update {
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putBooleanIfNotExists(PREF_NOTIFY_SEND_SUCCESS, false)
        putBooleanIfNotExists(PREF_REMOTE_CONTROL_ENABLED, false)
        putBooleanIfNotExists(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
        putBooleanIfNotExists(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
        putBooleanIfNotExists(PREF_SYNC_ENABLED, true)
        putStringIfNotExists(PREF_MESSAGE_LOCALE, VAL_PREF_DEFAULT)
        putBooleanIfNotExists(PREF_EMAIL_MESSENGER_ENABLED, false)
        putStringSetIfNotExists(PREF_EMAIL_CONTENT, DEFAULT_EMAIL_CONTENT)
        putStringSetIfNotExists(PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
        putBooleanIfNotExists(PREF_TELEGRAM_MESSENGER_ENABLED, true)
        putBooleanIfNotExists(PREF_TELEGRAM_MESSAGE_SHOW_HEADER, true)
        putBooleanIfNotExists(PREF_TELEGRAM_MESSAGE_SHOW_CALLER, false)
        putBooleanIfNotExists(PREF_TELEGRAM_MESSAGE_SHOW_DEVICE_NAME, false)
        putBooleanIfNotExists(PREF_TELEGRAM_MESSAGE_SHOW_EVENT_TIME, false)
        putBooleanIfNotExists(PREF_TELEGRAM_MESSAGE_SHOW_PROCESS_TIME, false)
        putBooleanIfNotExists(PREF_TELEGRAM_MESSAGE_SHOW_LOCATION, false)
        putBooleanIfNotExists(PREF_PROCESS_BATTERY_LEVEL, false)
    }

    companion object {
        var sharedPreferencesName = "com.bopr.android.smailer_preferences"

        private const val SETTINGS_VERSION = 2

        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */

        const val PREF_DEVICE_ALIAS = "device_alias"
        const val PREF_PROCESS_BATTERY_LEVEL = "process_battery_level"
        const val PREF_EMAIL_CONTENT = "email_content"
        const val PREF_EMAIL_MESSENGER_ENABLED = "email_messenger_enabled"
        const val PREF_EMAIL_TRIGGERS = "email_triggers"
        const val PREF_MESSAGE_LOCALE = "email_locale"
        const val PREF_NOTIFY_SEND_SUCCESS = "notify_send_success"
        const val PREF_PHONE_SEARCH_URL = "phone_search_url"
        const val PREF_RECIPIENTS_ADDRESS = "recipients_address"
        const val PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account"
        const val PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled"
        const val PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients"
        const val PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications"
        const val PREF_EMAIL_SENDER_ACCOUNT = "sender_account"
        const val PREF_SYNC_ENABLED = "sync_enabled"
        const val PREF_TELEGRAM_BOT_TOKEN = "telegram_bot_token"
        const val PREF_TELEGRAM_MESSAGE_SHOW_DEVICE_NAME = "telegram_message_show_device_name"
        const val PREF_TELEGRAM_MESSAGE_SHOW_EVENT_TIME = "telegram_message_show_event_time"
        const val PREF_TELEGRAM_MESSAGE_SHOW_PROCESS_TIME = "telegram_message_show_process_time"
        const val PREF_TELEGRAM_MESSAGE_SHOW_LOCATION = "telegram_message_show_location"
        const val PREF_TELEGRAM_MESSAGE_SHOW_HEADER = "telegram_message_show_header"
        const val PREF_TELEGRAM_MESSAGE_SHOW_CALLER = "telegram_message_show_caller"
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

        const val DEFAULT_PHONE_SEARCH_URL = "https://www.google.com/search?q=$PHONE_SEARCH_TAG"

        //todo: replace with individual boolean options
        val DEFAULT_EMAIL_CONTENT: Set<String> = mutableSetOf(
            VAL_PREF_EMAIL_CONTENT_CONTACT,
            VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
            VAL_PREF_EMAIL_CONTENT_HEADER,
            VAL_PREF_EMAIL_CONTENT_LOCATION,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
            VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
        )

        //todo: replace with individual boolean options
        val DEFAULT_TRIGGERS: Set<String> = mutableSetOf(
            VAL_PREF_TRIGGER_IN_SMS,
            VAL_PREF_TRIGGER_MISSED_CALLS
        )
    }

}