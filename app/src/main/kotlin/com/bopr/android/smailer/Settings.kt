package com.bopr.android.smailer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import com.bopr.android.smailer.util.SharedPreferencesWrapper
import com.bopr.android.smailer.util.TextUtil.commaJoin
import com.bopr.android.smailer.util.TextUtil.commaSplit
import com.bopr.android.smailer.util.TextUtil.isNullOrEmpty
import com.bopr.android.smailer.util.Util.asSet
import java.io.IOException
import java.util.*

/**
 * Settings.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Settings(private val context: Context) :
        SharedPreferencesWrapper(context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE)) {

    fun getLocale(): Locale {
        val value = getString(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)
        return if (value == VAL_PREF_DEFAULT) {
            Locale.getDefault()
        } else {
            val ss = value!!.split("_")
            if (ss.size == 2) {
                Locale(ss[0], ss[1])
            } else {
                throw IllegalArgumentException("Invalid locale code: $value")
            }
        }
    }

    fun getDeviceName(): String {
        val value = getString(PREF_DEVICE_ALIAS, "")
        return if (isNullOrEmpty(value)) {
            deviceName()
        } else {
            value!!
        }
    }

    fun getReleaseVersion(): String {
        try {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (x: PackageManager.NameNotFoundException) {
            throw RuntimeException(x)
        }
    }

    fun getReleaseInfo(): BuildInfo {
        try {
            val properties = Properties()
            context.assets.open("release.properties").use {
                properties.load(it)
            }

            return BuildInfo(
                    properties.getProperty("build_number"),
                    properties.getProperty("build_time")
            )
        } catch (x: IOException) {
            throw RuntimeException("Cannot read release properties", x)
        }
    }

    fun getFilter(): PhoneEventFilter {
        return PhoneEventFilter().apply {
            triggers = getStringSet(PREF_EMAIL_TRIGGERS, emptySet())!!
            phoneBlacklist = commaSplit(getString(PREF_FILTER_PHONE_BLACKLIST, "")!!).toMutableSet()
            phoneWhitelist = commaSplit(getString(PREF_FILTER_PHONE_WHITELIST, "")!!).toMutableSet()
            textBlacklist = commaSplit(getString(PREF_FILTER_TEXT_BLACKLIST, "")!!).toMutableSet()
            textWhitelist = commaSplit(getString(PREF_FILTER_TEXT_WHITELIST, "")!!).toMutableSet()
        }
    }

    fun loadDefaults() {
        with(edit()) {
            putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
            putStringOptional(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)
            putBooleanOptional(PREF_RESEND_UNSENT, true)
            putBooleanOptional(PREF_MARK_SMS_AS_READ, false)
            putBooleanOptional(PREF_REMOTE_CONTROL_ENABLED, false)
            putBooleanOptional(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
            putBooleanOptional(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
            putStringSetOptional(PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)

            getStringSet(PREF_EMAIL_CONTENT, null)?.let {
                if (getInt(PREF_SETTINGS_VERSION, 1) == 1) {
                    it.add(VAL_PREF_EMAIL_CONTENT_HEADER)
                    it.add(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT)
                    putStringSet(PREF_EMAIL_CONTENT, it)
                }
            } ?: putStringSet(PREF_EMAIL_CONTENT, DEFAULT_CONTENT)

            apply()
        }
    }

    override fun edit(): EditorWrapper {
        return EditorWrapper(super.edit())
    }

    inner class EditorWrapper(edit: SharedPreferencesWrapper.EditorWrapper) : SharedPreferencesWrapper.EditorWrapper(edit) {

        fun putFilter(filter: PhoneEventFilter): EditorWrapper {
            putString(PREF_FILTER_PHONE_BLACKLIST, commaJoin(filter.phoneBlacklist))
            putString(PREF_FILTER_PHONE_WHITELIST, commaJoin(filter.phoneWhitelist))
            putString(PREF_FILTER_TEXT_BLACKLIST, commaJoin(filter.textBlacklist))
            putString(PREF_FILTER_TEXT_WHITELIST, commaJoin(filter.textWhitelist))

            return this
        }
    }

    class BuildInfo(val number: String, val time: String)

    companion object {

        private const val SETTINGS_VERSION = 2
        const val PREFERENCES_STORAGE_NAME = "com.bopr.android.smailer_preferences"

        const val PREF_SYNC_TIME = "sync_time" /* hidden */
        const val PREF_SETTINGS_VERSION = "settings_version"
        const val PREF_SENDER_ACCOUNT = "sender_account"
        const val PREF_RECIPIENTS_ADDRESS = "recipients_address"
        const val PREF_EMAIL_CONTENT = "email_content"
        const val PREF_EMAIL_TRIGGERS = "email_triggers"
        const val PREF_EMAIL_LOCALE = "email_locale"
        const val PREF_NOTIFY_SEND_SUCCESS = "notify_send_success"
        const val PREF_RULES = "rules"
        const val PREF_HISTORY = "history"
        const val PREF_MARK_SMS_AS_READ = "mark_processed_sms_as_read"
        const val PREF_RESEND_UNSENT = "resend_unsent" /* hidden */
        const val PREF_FILTER_PHONE_BLACKLIST = "message_filter_blacklist"
        const val PREF_FILTER_PHONE_WHITELIST = "message_filter_whitelist"
        const val PREF_FILTER_TEXT_BLACKLIST = "message_filter_text_blacklist"
        const val PREF_FILTER_TEXT_WHITELIST = "message_filter_text_whitelist"
        const val PREF_DEVICE_ALIAS = "device_alias"
        const val PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled"
        const val PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account"
        const val PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications"
        const val PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients"

        const val VAL_PREF_DEFAULT = "default"
        const val VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME = "time"
        const val VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT = "time_sent"
        const val VAL_PREF_EMAIL_CONTENT_DEVICE_NAME = "device_name"
        const val VAL_PREF_EMAIL_CONTENT_LOCATION = "location"
        const val VAL_PREF_EMAIL_CONTENT_CONTACT = "contact_name"
        const val VAL_PREF_EMAIL_CONTENT_HEADER = "header"
        const val VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS = "remote_control_links"
        const val VAL_PREF_TRIGGER_IN_SMS = "in_sms"
        const val VAL_PREF_TRIGGER_OUT_SMS = "out_sms"
        const val VAL_PREF_TRIGGER_IN_CALLS = "in_calls"
        const val VAL_PREF_TRIGGER_OUT_CALLS = "out_calls"
        const val VAL_PREF_TRIGGER_MISSED_CALLS = "missed_calls"

        @JvmField
        val DEFAULT_CONTENT: Set<String> = asSet(
                VAL_PREF_EMAIL_CONTENT_HEADER,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_CONTACT,
                VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS)

        @JvmField
        val DEFAULT_TRIGGERS: Set<String> = asSet(
                VAL_PREF_TRIGGER_IN_SMS,
                VAL_PREF_TRIGGER_MISSED_CALLS)
    }

}