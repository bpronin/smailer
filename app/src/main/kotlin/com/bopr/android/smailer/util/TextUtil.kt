package com.bopr.android.smailer.util

import android.util.Patterns.EMAIL_ADDRESS
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

object TextUtil {
    //TODO: remove @JvmStatic

    val QUOTATION_PATTERN: Pattern = Pattern.compile("\"([^\"]*)\"")

    private const val REGEX_ = "REGEX:"

    @JvmStatic
    fun isNotEmpty(s: CharSequence): Boolean = s.isNotEmpty()

    @JvmStatic
    fun isNullOrEmpty(s: CharSequence?): Boolean = s.isNullOrEmpty()

    @JvmStatic
    fun isNullOrBlank(s: CharSequence?): Boolean = s.isNullOrBlank()

    @JvmStatic
    fun escapeRegex(s: String): String {
        return REGEX_ + s
    }

    @JvmStatic
    fun unescapeRegex(s: String): String? {
        if (s.isNotEmpty()) {
            val ss = s.split(REGEX_)
            if (ss.size > 1) {
                return ss[1]
            }
        }
        return null
    }

    @JvmStatic
    fun isQuoted(s: String?): Boolean {
        return !s.isNullOrBlank() && QUOTATION_PATTERN.matcher(s).matches()
    }

    @JvmStatic
    fun join(values: Collection<*>, divider: String) = values.joinToString(divider)

    @JvmStatic
    fun split(value: String, divider: String, trim: Boolean): List<String> {
        if (value.isNotEmpty()) {
            var s = value
            if (trim) {
                s = value.replace(" ", "")
            }
            if (s.isNotEmpty()) {
                return s.split(divider)
            }
        }
        return emptyList()
    }

    @JvmStatic
    fun commaJoin(values: Collection<*>): String {
//        return values.joinToString()
        return join(values, ",")
    }

    @JvmStatic
    fun commaSplit(s: String): List<String> {
        return split(s, ",", true)
    }

    @JvmStatic
    fun decimalToDMS(coordinate: Double, degreeSymbol: String, minuteSymbol: String,
                     secondSymbol: String): String {
        var c = coordinate
        var mod = c % 1
        var intPart = c.toInt()

        val degrees = abs(intPart)

        c = mod * 60
        mod = c % 1
        intPart = c.toInt()
        val minutes = abs(intPart)

        c = mod * 60
        intPart = c.toInt()
        val seconds = abs(intPart)

        return degrees.toString() + degreeSymbol + minutes + minuteSymbol + seconds + secondSymbol
    }

    @JvmStatic
    fun capitalize(text: String?): String? {
        return if (text.isNullOrBlank()) {
            text
        } else {
            text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1)
        }
    }

    @JvmStatic
    fun formatDuration(duration: Long): String? {
        val seconds = duration / 1000
        return String.format(Locale.US, "%d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
    }

    @JvmStatic
    fun readStream(stream: InputStream?): String? {
        return stream?.let {
            with(Scanner(stream).useDelimiter("\\A")) {
                return if (hasNext()) return next() else ""
            }
        }
    }

    @JvmStatic
    fun isValidEmailAddress(text: String?): Boolean {
        return !text.isNullOrBlank() && EMAIL_ADDRESS.matcher(text).matches()
    }

    @JvmStatic
    fun isValidEmailAddressList(text: String?): Boolean {
        if (!text.isNullOrBlank()) {
            for (s in commaSplit(text)) {
                if (!isValidEmailAddress(s)) {
                    return false
                }
            }
            return true
        }
        return false
    }

//
//fun normalizePhone(phone: String): String {
//    return phone.replace("[^A-Za-z0-9*.]", "").toUpperCase(Locale.ROOT)
//}
//
//fun comparePhones(p1: String, p2: String): Int {
//    return normalizePhone(p1).compareTo(normalizePhone(p2))
//}

//fun phonesEqual(p1: String?, p2: String?): Boolean {
//    if (p1 != null) {
//        if (p2 != null) {
//            return comparePhones(p1, p2) == 0
//        }
//    } else if (p2 == null) {
//        return true
//    }
//    return false
////    return p1.equals(p2) || comparePhones(p1, p2) == 0
//}
//
//fun phoneToRegEx(phone: String): String {
//    return normalizePhone(phone).replace("\\*".toRegex(), "(.*)")
//}
//
//fun extractPhone(text: String?): String? {
//    if (!text.isNullOrBlank()) {
//        val matcher = PHONE_PATTERN.matcher(text)
//        if (matcher.find()) {
//            return matcher.group()
//        }
//    }
//    return null
//}
//
///**
// * Returns phone as it is if it is regular or quoted otherwise
// */
//fun escapePhone(phone: String): String? {
//    return if (PHONE_PATTERN.matcher(phone).matches()) phone else "\"" + phone + "\""
//}

//fun Collection<String>.findPhone(phone: String): String? {
//    return this.find { s -> phonesEqual(s, phone) }
//}
//
//fun Collection<String>.containsPhone(phone: String): Boolean {
//    return this.findPhone(phone) != null
//}
}