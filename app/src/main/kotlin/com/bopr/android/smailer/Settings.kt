package com.bopr.android.smailer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.bopr.android.smailer.messenger.mail.PhoneCallMailFormatter.Companion.PHONE_SEARCH_TAG
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.commaJoin
import com.bopr.android.smailer.util.commaSplit

/**
 * Application settings. Wrapper for [SharedPreferences].
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Settings(context: Context, changeListener: ChangeListener? = null) {

    private val preferences = context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)
    private var preferencesListener = changeListener?.let {
        OnSharedPreferenceChangeListener { _, key ->
            key?.run { it.onSettingsChanged(this@Settings, key) }
        }.also {
            preferences.registerOnSharedPreferenceChangeListener(it)
        }
    }

    init {
        if (SETTINGS_VERSION > getInt(PREF_SETTINGS_VERSION)) {
            loadDefaults()
        }
    }

    internal fun loadDefaults() {
        update {
            putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
            ifNotExists(PREF_DISPATCH_BATTERY_LEVEL) { putBoolean(it, false) }
            ifNotExists(PREF_MAIL_MESSENGER_ENABLED) { putBoolean(it, false) }
            ifNotExists(PREF_MESSAGE_LOCALE) { putString(it, VAL_PREF_DEFAULT) }
            ifNotExists(PREF_NOTIFY_SEND_SUCCESS) { putBoolean(it, false) }
            ifNotExists(PREF_REMOTE_CONTROL_ENABLED) { putBoolean(it, false) }
            ifNotExists(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS) { putBoolean(it, true) }
            ifNotExists(PREF_REMOTE_CONTROL_NOTIFICATIONS) { putBoolean(it, true) }
            ifNotExists(PREF_SMS_MESSENGER_ENABLED) { putBoolean(it, false) }
            ifNotExists(PREF_SYNC_ENABLED) { putBoolean(it, true) }
            ifNotExists(PREF_TELEGRAM_MESSENGER_ENABLED) { putBoolean(it, true) }
            ifNotExists(PREF_MAIL_MESSAGE_CONTENT) {
                putStringSet(
                    it, setOf(
                        VAL_PREF_MESSAGE_CONTENT_BODY,
                        VAL_PREF_MESSAGE_CONTENT_CALLER,
                        VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                        VAL_PREF_MESSAGE_CONTENT_HEADER,
                        VAL_PREF_MESSAGE_CONTENT_LOCATION,
                        VAL_PREF_MESSAGE_CONTENT_CREATION_TIME,
                        VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
                        VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
                    )
                )
            }
            ifNotExists(PREF_MAIL_TRIGGERS) {
                putStringSet(
                    it, setOf(
                        VAL_PREF_TRIGGER_IN_SMS,
                        VAL_PREF_TRIGGER_MISSED_CALLS
                    )
                )
            }
            ifNotExists(PREF_TELEGRAM_MESSAGE_CONTENT) {
                putStringSet(
                    it, setOf(
                        VAL_PREF_MESSAGE_CONTENT_BODY,
                        VAL_PREF_MESSAGE_CONTENT_CALLER,
                        VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                        VAL_PREF_MESSAGE_CONTENT_HEADER,
                        VAL_PREF_MESSAGE_CONTENT_LOCATION,
                        VAL_PREF_MESSAGE_CONTENT_CREATION_TIME,
                        VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
                    )
                )
            }
        }
    }

    fun getMailTriggers() = getStringSet(PREF_MAIL_TRIGGERS)
    fun getMailRecipients() = getString(PREF_MAIL_MESSENGER_RECIPIENTS, "")
    fun getMessageLocale() = getString(PREF_MESSAGE_LOCALE, VAL_PREF_DEFAULT)
    fun getDeviceName() = getString(PREF_DEVICE_ALIAS, DEVICE_NAME)
    fun getPhoneSearchUrl() = getString(PREF_PHONE_SEARCH_URL, DEFAULT_PHONE_SEARCH_URL)
    fun hasMailContent(value: String) = getStringSet(PREF_MAIL_MESSAGE_CONTENT).contains(value)
    fun hasTelegramMessageContent(value: String) =
        getStringSet(PREF_TELEGRAM_MESSAGE_CONTENT).contains(value)

    fun dispose() {
        preferencesListener?.run {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    fun getString(key: String): String? {
        return preferences.getString(key, null)
    }

    fun getString(key: String, defaultValue: String): String {
        return preferences.getString(key, defaultValue)!!
    }

    fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    fun getInt(key: String): Int {
        return preferences.getInt(key, 0)
    }

    fun getStringSet(key: String): MutableSet<String> {
        return preferences.getStringSet(key, mutableSetOf())!!
    }

    fun getStringList(key: String): MutableList<String> {
        return getString(key)?.commaSplit()?.toMutableList() ?: mutableListOf()
    }

    fun update(action: Editor.() -> Unit) {
        val edit = preferences.edit()
        Editor(edit).action()
        edit.apply()
    }

    inner class Editor(private val wrapped: SharedPreferences.Editor) {

        fun clear() {
            wrapped.clear()
        }

        fun ifNotExists(key: String, put: Editor.(key: String) -> Unit) {
            if (!preferences.contains(key)) put(key)
        }

        fun putInt(key: String, value: Int) {
            wrapped.putInt(key, value)
        }

        fun putBoolean(key: String, value: Boolean) {
            wrapped.putBoolean(key, value)
        }

        fun putString(key: String, value: String?) {
            wrapped.putString(key, value)
        }

        fun putStringList(key: String, value: Collection<String>?) {
            wrapped.putString(key, value?.commaJoin())
        }

        fun putStringSet(key: String, value: Set<String>) {
            wrapped.putStringSet(key, value)
        }

    }

    interface ChangeListener {

        fun onSettingsChanged(settings: Settings, key: String)
    }

    companion object {
        var sharedPreferencesName = "com.bopr.android.smailer_preferences"

        private const val SETTINGS_VERSION = 2

        const val PREF_DEVICE_ALIAS = "device_alias"
        const val PREF_DISPATCH_BATTERY_LEVEL = "dispatch_battery_level"
        const val PREF_MAIL_MESSAGE_CONTENT = "email_message_content"
        const val PREF_MAIL_MESSENGER_ENABLED = "email_messenger_enabled"
        const val PREF_MAIL_MESSENGER_RECIPIENTS = "recipients_address"
        const val PREF_MAIL_SENDER_ACCOUNT = "sender_account"
        const val PREF_MAIL_TRIGGERS = "email_triggers"
        const val PREF_MESSAGE_LOCALE = "email_locale"
        const val PREF_NOTIFY_SEND_SUCCESS = "notify_send_success"
        const val PREF_PHONE_SEARCH_URL = "phone_search_url"
        const val PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account"
        const val PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled"
        const val PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients"
        const val PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications"
        const val PREF_SETTINGS_VERSION = "settings_version"
        const val PREF_SMS_MESSENGER_ENABLED = "pref_sms_messenger_enabled"
        const val PREF_SMS_MESSENGER_RECIPIENTS = "pref_sms_messenger_recipients"
        const val PREF_SYNC_ENABLED = "sync_enabled"
        const val PREF_TELEGRAM_BOT_TOKEN = "telegram_bot_token"
        const val PREF_TELEGRAM_CHAT_ID = "telegram_chat_id"
        const val PREF_TELEGRAM_MESSAGE_CONTENT = "pref_telegram_message_content"
        const val PREF_TELEGRAM_MESSENGER_ENABLED = "pref_telegram_messenger_enabled"

        const val VAL_PREF_DEFAULT = "default"
        const val VAL_PREF_MESSAGE_CONTENT_BODY = "val_pref_message_content_body"
        const val VAL_PREF_MESSAGE_CONTENT_CALLER = "val_pref_message_content_caller"
        const val VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS = "val_pref_message_content_control_links"
        const val VAL_PREF_MESSAGE_CONTENT_CREATION_TIME = "val_pref_message_content_event_time"
        const val VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME = "val_pref_message_content_device_name"
        const val VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME = "val_pref_message_content_dispatch_time"
        const val VAL_PREF_MESSAGE_CONTENT_HEADER = "val_pref_message_content_header"
        const val VAL_PREF_MESSAGE_CONTENT_LOCATION = "val_pref_message_content_location"
        const val VAL_PREF_TRIGGER_IN_CALLS = "in_calls"
        const val VAL_PREF_TRIGGER_IN_SMS = "in_sms"
        const val VAL_PREF_TRIGGER_MISSED_CALLS = "missed_calls"
        const val VAL_PREF_TRIGGER_OUT_CALLS = "out_calls"
        const val VAL_PREF_TRIGGER_OUT_SMS = "out_sms"

        const val DEFAULT_PHONE_SEARCH_URL = "https://www.google.com/search?q=$PHONE_SEARCH_TAG"
    }

}