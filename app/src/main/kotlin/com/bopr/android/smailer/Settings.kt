package com.bopr.android.smailer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.android.smailer.util.SharedPreferencesWrapper
import java.util.*

/**
 * Settings.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Settings(context: Context) : SharedPreferencesWrapper(
        context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE)) {

    val locale: Locale
        get() {
            val value = getString(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)!!
            return if (value == VAL_PREF_DEFAULT) {
                Locale.getDefault()
            } else {
                val ss = value.split("_")
                if (ss.size == 2) {
                    Locale(ss[0], ss[1])
                } else {
                    throw IllegalArgumentException("Invalid locale code: $value")
                }
            }
        }

    val callFilter: PhoneEventFilter
        get() {
            return PhoneEventFilter(
                    triggers = getStringSet(PREF_EMAIL_TRIGGERS),
                    phoneBlacklist = getCommaSet(PREF_FILTER_PHONE_BLACKLIST),
                    phoneWhitelist = getCommaSet(PREF_FILTER_PHONE_WHITELIST),
                    textBlacklist = getCommaSet(PREF_FILTER_TEXT_BLACKLIST),
                    textWhitelist = getCommaSet(PREF_FILTER_TEXT_WHITELIST)
            )
        }

    fun loadDefaults() {
        with(edit()) {
            putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
            putStringOptional(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)
            putBooleanOptional(PREF_REMOTE_CONTROL_ENABLED, false)
            putBooleanOptional(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
            putBooleanOptional(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
            putStringSetOptional(PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)

            val emailContent = getStringSet(PREF_EMAIL_CONTENT)
            if (emailContent.isEmpty()) {
                putStringSet(PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
            } else if (getInt(PREF_SETTINGS_VERSION, 1) == 1) {
                emailContent.add(VAL_PREF_EMAIL_CONTENT_HEADER)
                emailContent.add(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT)
                putStringSet(PREF_EMAIL_CONTENT, emailContent)
            }

            apply()
        }
    }

    override fun edit(): EditorWrapper {
        return EditorWrapper(super.edit())
    }

    inner class EditorWrapper(edit: SharedPreferencesWrapper.EditorWrapper) : SharedPreferencesWrapper.EditorWrapper(edit) {

        fun putFilter(filter: PhoneEventFilter): EditorWrapper {
            putCommaSet(PREF_FILTER_PHONE_BLACKLIST, filter.phoneBlacklist)
            putCommaSet(PREF_FILTER_PHONE_WHITELIST, filter.phoneWhitelist)
            putCommaSet(PREF_FILTER_TEXT_BLACKLIST, filter.textBlacklist)
            putCommaSet(PREF_FILTER_TEXT_WHITELIST, filter.textWhitelist)

            return this
        }
    }

    companion object {

        private const val SETTINGS_VERSION = 2
        const val PREFERENCES_STORAGE_NAME = "com.bopr.android.smailer_preferences"

        const val PREF_DEVICE_ALIAS = "device_alias"
        const val PREF_EMAIL_CONTENT = "email_content"
        const val PREF_EMAIL_LOCALE = "email_locale"
        const val PREF_EMAIL_TRIGGERS = "email_triggers"
        const val PREF_FILTER_PHONE_BLACKLIST = "message_filter_blacklist"
        const val PREF_FILTER_PHONE_WHITELIST = "message_filter_whitelist"
        const val PREF_FILTER_TEXT_BLACKLIST = "message_filter_text_blacklist"
        const val PREF_FILTER_TEXT_WHITELIST = "message_filter_text_whitelist"
        const val PREF_HISTORY = "history"
        const val PREF_NOTIFY_SEND_SUCCESS = "notify_send_success"
        const val PREF_RECIPIENTS_ADDRESS = "recipients_address"
        const val PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account"
        const val PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled"
        const val PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients"
        const val PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications"
        const val PREF_SENDER_ACCOUNT = "sender_account"
        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */
        const val PREF_SYNC_TIME = "sync_time" /* hidden */

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

        val DEFAULT_CONTENT: Set<String> = mutableSetOf(
                VAL_PREF_EMAIL_CONTENT_HEADER,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_CONTACT,
                VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS)

        val DEFAULT_TRIGGERS: Set<String> = mutableSetOf(
                VAL_PREF_TRIGGER_IN_SMS,
                VAL_PREF_TRIGGER_MISSED_CALLS)
    }

}