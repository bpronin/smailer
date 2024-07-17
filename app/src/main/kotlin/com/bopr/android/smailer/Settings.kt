package com.bopr.android.smailer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.annotation.StringRes
import com.bopr.android.smailer.processor.mail.MailPhoneEventFormatter.Companion.PHONE_SEARCH_TAG
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.SharedPreferencesWrapper

/**
 * Settings.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Settings(context: Context) :
    SharedPreferencesWrapper(context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)) {

    fun getEmailContent() = getStringSet(PREF_EMAIL_MESSAGE_CONTENT)
    fun getEmailTriggers() = getStringSet(PREF_EMAIL_TRIGGERS)
    fun getEmailRecipients() = requireString(PREF_RECIPIENTS_ADDRESS, "")
    fun getMessageLocale() = requireString(PREF_MESSAGE_LOCALE, VAL_PREF_DEFAULT)
    fun getDeviceName() = requireString(PREF_DEVICE_ALIAS, DEVICE_NAME)
    fun getPhoneSearchUrl() = requireString(PREF_PHONE_SEARCH_URL, DEFAULT_PHONE_SEARCH_URL)
    fun hasTelegramMessageContent(key:String) = getStringSet(PREF_TELEGRAM_MESSAGE_CONTENT).contains(key)

    fun loadDefaults() = update {
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putOptBoolean(PREF_NOTIFY_SEND_SUCCESS, false)
        putOptBoolean(PREF_REMOTE_CONTROL_ENABLED, false)
        putOptBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
        putOptBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
        putOptBoolean(PREF_SYNC_ENABLED, true)
        putOptBoolean(PREF_EMAIL_MESSENGER_ENABLED, false)
        putOptBoolean(PREF_TELEGRAM_MESSENGER_ENABLED, true)
        putOptBoolean(PREF_DISPATCH_BATTERY_LEVEL, false)
        putOptString(PREF_MESSAGE_LOCALE, VAL_PREF_DEFAULT)
        putOptStringSet(PREF_EMAIL_MESSAGE_CONTENT, DEFAULT_EMAIL_MESSAGE_CONTENT)
        putOptStringSet(PREF_TELEGRAM_MESSAGE_CONTENT, DEFAULT_TELEGRAM_MESSAGE_CONTENT)
        putOptStringSet(PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
    }

    companion object {
        var sharedPreferencesName = "com.bopr.android.smailer_preferences"

        private const val SETTINGS_VERSION = 2

        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */

        const val PREF_DEVICE_ALIAS = "device_alias"

        const val PREF_DISPATCH_BATTERY_LEVEL = "dispatch_battery_level"

        const val PREF_EMAIL_MESSAGE_CONTENT = "email_message_content"
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

        const val PREF_TELEGRAM_MESSAGE_CONTENT = "pref_telegram_message_content"
        const val PREF_TELEGRAM_MESSENGER_ENABLED = "pref_telegram_messenger_enabled"

        const val VAL_PREF_DEFAULT = "default"
        const val VAL_PREF_MESSAGE_CONTENT_BODY = "val_pref_message_content_body"
        const val VAL_PREF_MESSAGE_CONTENT_CALLER = "val_pref_message_content_caller"
        const val VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME = "val_pref_message_content_device_name"
        const val VAL_PREF_MESSAGE_CONTENT_HEADER = "val_pref_message_content_header"
        const val VAL_PREF_MESSAGE_CONTENT_LOCATION = "val_pref_message_content_location"
        const val VAL_PREF_MESSAGE_CONTENT_EVENT_TIME = "val_pref_message_content_event_time"
        const val VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME = "val_pref_message_content_dispatch_time"
        const val VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS = "val_pref_message_content_control_links"
        const val VAL_PREF_TRIGGER_IN_CALLS = "in_calls"
        const val VAL_PREF_TRIGGER_IN_SMS = "in_sms"
        const val VAL_PREF_TRIGGER_MISSED_CALLS = "missed_calls"
        const val VAL_PREF_TRIGGER_OUT_CALLS = "out_calls"
        const val VAL_PREF_TRIGGER_OUT_SMS = "out_sms"

        const val DEFAULT_PHONE_SEARCH_URL = "https://www.google.com/search?q=$PHONE_SEARCH_TAG"

        val DEFAULT_EMAIL_MESSAGE_CONTENT = mutableSetOf(
            VAL_PREF_MESSAGE_CONTENT_BODY,
            VAL_PREF_MESSAGE_CONTENT_CALLER,
            VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
            VAL_PREF_MESSAGE_CONTENT_HEADER,
            VAL_PREF_MESSAGE_CONTENT_LOCATION,
            VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
            VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
            VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
        )

        val DEFAULT_TELEGRAM_MESSAGE_CONTENT = mutableSetOf(
            VAL_PREF_MESSAGE_CONTENT_BODY,
            VAL_PREF_MESSAGE_CONTENT_CALLER,
            VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
            VAL_PREF_MESSAGE_CONTENT_HEADER,
            VAL_PREF_MESSAGE_CONTENT_LOCATION,
            VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
            VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
        )

        val DEFAULT_TRIGGERS: Set<String> = mutableSetOf(
            VAL_PREF_TRIGGER_IN_SMS,
            VAL_PREF_TRIGGER_MISSED_CALLS
        )
    }

}