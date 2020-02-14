package com.bopr.android.smailer.util

import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import com.bopr.android.smailer.util.TextUtil.isQuoted
import java.util.*
import java.util.regex.Pattern

/**
 * Phone number and email address utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object AddressUtil {

    val PHONE_PATTERN: Pattern = Pattern.compile( // sdd = space, dot, or dash
            "(\\+[0-9]+[\\- \\.]*)?" // +<digits><sdd>*
                    + "(\\([0-9]+\\)[\\- \\.]*)?" // (<digits>)<sdd>*
                    + "([0-9][0-9\\- \\.]+[0-9])") // <digit><digit|sdd>+<digit>

    @JvmStatic
    fun normalizePhone(phone: String): String {
        return phone.replace("[^A-Za-z0-9*.]", "").toUpperCase(Locale.ROOT)
    }

    @JvmStatic
    fun comparePhones(p1: String, p2: String): Int {
        return normalizePhone(p1).compareTo(normalizePhone(p2))
    }

    @JvmStatic
    fun phonesEqual(p1: String, p2: String): Boolean {
        return comparePhones(p1, p2) == 0
    }

    @JvmStatic
    fun findPhone(phones: Collection<String>, phone: String): String? {
        for (p in phones) {
            if (phonesEqual(p, phone)) {
                return p
            }
        }
        return null
    }

    @JvmStatic
    fun containsPhone(list: Collection<String>, phone: String): Boolean {
        return findPhone(list, phone) != null
    }

    @JvmStatic
    fun phoneToRegEx(phone: String): String {
        return normalizePhone(phone).replace("\\*", "(.*)")
    }

    @JvmStatic
    fun extractPhone(text: String?): String? {
        if (!text.isNullOrBlank()) {
            val matcher = PHONE_PATTERN.matcher(text)
            if (matcher.find()) {
                return matcher.group()
            }
        }
        return null
    }

    /**
     * Returns phone as is if it is regular or quoted otherwise
     */
    @JvmStatic
    fun escapePhone(phone: String): String {
        return if (PHONE_PATTERN.matcher(phone).matches()) phone else "\"" + phone + "\""
    }

    @JvmStatic
    fun normalizeEmail(email: String): String {
        val localPart = email.split("@")[0]
        val part = if (isQuoted(localPart)) localPart else localPart.replace("\\.", "")
        return email.replaceFirst(localPart, part).toLowerCase(Locale.ROOT)
    }

    @JvmStatic
    fun compareEmails(e1: String, e2: String): Int {
        return normalizeEmail(e1).compareTo(normalizeEmail(e2))
    }

    @JvmStatic
    fun emailsEqual(e1: String, e2: String): Boolean {
        return compareEmails(e1, e2) == 0
    }

    @JvmStatic
    fun findEmail(emails: Collection<String>, email: String): String? {
        for (m in emails) {
            if (emailsEqual(m, email)) {
                return m
            }
        }
        return null
    }

    @JvmStatic
    fun containsEmail(list: Collection<String>, email: String): Boolean {
        return findEmail(list, email) != null
    }

    @JvmStatic
    fun extractEmail(text: String?): String? {
        if (!text.isNullOrBlank()) {
            val matcher = EMAIL_ADDRESS.matcher(text)
            if (matcher.find()) {
                return matcher.group()
            }
        }
        return null
    }
}