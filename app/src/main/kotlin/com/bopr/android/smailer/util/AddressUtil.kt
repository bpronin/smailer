package com.bopr.android.smailer.util

import android.telephony.PhoneNumberUtils
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import java.util.Locale
import java.util.regex.Pattern

@Suppress("RegExpRedundantEscape")
const val PHONE_REGEX: String =
    "(\\+[0-9]+[\\- \\.]*)?(\\([0-9]+\\)[\\- \\.]*)?([0-9][0-9\\- \\.]+[0-9])"
val PHONE_PATTERN: Pattern = Pattern.compile(PHONE_REGEX)
private val NON_PHONE_SYMBOLS = Regex("[^A-Za-z0-9*.]")

fun formatPhoneNumber(s: String): String {
    return PhoneNumberUtils.formatNumber(
        if (s.firstOrNull() == '+') s else "+$s",
        Locale.ROOT.country
    ) ?: s
}

fun stripPhoneNumber(number: String): String {
    return number.replace(NON_PHONE_SYMBOLS, "").uppercase(Locale.ROOT)
}

fun comparePhoneNumbers(number1: String, number2: String): Int {
    return stripPhoneNumber(number1).compareTo(stripPhoneNumber(number2))
}

fun samePhoneNumber(number1: String, number2: String): Boolean {
    return comparePhoneNumbers(number1, number2) == 0
}

//fun Collection<String>.containsPhone(phone: String): Boolean {
//    return any { samePhone(it, phone) }
//}

fun phoneNumberToRegEx(number: String): String {
    return stripPhoneNumber(number).replace("*", "(.*)")
}

fun extractPhoneNumber(text: String?): String? {
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
fun escapePhoneNumber(number: String): String {
    return if (PHONE_PATTERN.matcher(number).matches()) number else "\"$number\""
}

fun normalizeEmail(email: String): String {
    val localPart = email.split("@")[0]
    val part = if (isQuoted(localPart)) {
        localPart
    } else {
        localPart.replace(".", "")
    }
    return email.replaceFirst(localPart, part).lowercase(Locale.ROOT)
}

private fun compareEmails(e1: String, e2: String): Int {
    return normalizeEmail(e1).compareTo(normalizeEmail(e2))
}

fun sameEmail(e1: String, e2: String): Boolean {
    return compareEmails(e1, e2) == 0
}

fun Collection<String>.containsEmail(email: String): Boolean {
    return any { m -> sameEmail(m, email) }
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
