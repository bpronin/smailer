package com.bopr.android.smailer.util

import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import java.util.*
import java.util.regex.Pattern

val PHONE_PATTERN: Pattern = Pattern.compile(       // sdd = space, dot, or dash
        "(\\+[0-9]+[\\- \\.]*)?"                    // +<digits><sdd>*
                + "(\\([0-9]+\\)[\\- \\.]*)?"       // (<digits>)<sdd>*
                + "([0-9][0-9\\- \\.]+[0-9])")      // <digit><digit|sdd>+<digit>
private val NON_PHONE_SYMBOLS = Regex("[^A-Za-z0-9*.]")

fun normalizePhone(phone: String): String {
    return phone.replace(NON_PHONE_SYMBOLS, "").toUpperCase(Locale.ROOT)
}

fun comparePhones(p1: String, p2: String): Int {
    return normalizePhone(p1).compareTo(normalizePhone(p2))
}

fun samePhone(p1: String, p2: String): Boolean {
    return comparePhones(p1, p2) == 0
}

fun findPhone(phones: Collection<String>, phone: String): String? {
    return phones.find { samePhone(it, phone) }
}

fun containsPhone(phones: Collection<String>, phone: String): Boolean {
    return phones.any { samePhone(it, phone) }
}

fun phoneToRegEx(phone: String): String {
    return normalizePhone(phone).replace("*", "(.*)")
}

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
 * Returns phone as is if it is a regular or quoted otherwise
 */
fun escapePhone(phone: String): String {
    return if (PHONE_PATTERN.matcher(phone).matches()) phone else "\"" + phone + "\""
}

fun normalizeEmail(email: String): String {
    val localPart = email.split("@")[0]
    val part = if (isQuoted(localPart)) {
        localPart
    } else {
        localPart.replace(".", "")
    }
    return email.replaceFirst(localPart, part).toLowerCase(Locale.ROOT)
}

private fun compareEmails(e1: String, e2: String): Int {
    return normalizeEmail(e1).compareTo(normalizeEmail(e2))
}

fun sameEmail(e1: String, e2: String): Boolean {
    return compareEmails(e1, e2) == 0
}

fun findEmail(emails: Collection<String>, email: String): String? {
    return emails.find { m -> sameEmail(m, email) }
}

fun containsEmail(emails: Collection<String>, email: String): Boolean {
    return emails.any { m -> sameEmail(m, email) }
}

fun extractEmail(text: String?): String? {
    if (!text.isNullOrBlank()) {
        val matcher = EMAIL_ADDRESS.matcher(text)
        if (matcher.find()) {
            return matcher.group()
        }
    }
    return null
}
