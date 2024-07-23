package com.bopr.android.smailer.util

import android.util.Patterns.EMAIL_ADDRESS
import java.io.InputStream
import java.net.URLEncoder
import java.util.Locale
import java.util.Scanner
import java.util.regex.Pattern
import kotlin.math.abs

const val QUOTATION_REGEX = "\"([^\"]*)\""
val QUOTATION_PATTERN: Pattern = Pattern.compile(QUOTATION_REGEX)
val WEB_URL_PATTERN: Pattern = Pattern.compile("(?:\\S+)://\\S+")

private val COMMA_ESCAPED = Regex("(?<!/),")  /* matches commas not preceded by slash symbol */
private const val REGEX_ = "REGEX:"

fun strings(vararg values: Any): Array<String> {
    return Array(values.size) { values[it].toString() }
}

fun escapeRegex(s: String): String {
    return REGEX_ + s
}

fun unescapeRegex(s: String?): String? {
    if (!s.isNullOrEmpty() && s.startsWith(REGEX_)) {
        return s.substring(REGEX_.length)
    }
    return null
}

fun isQuoted(s: String?): Boolean {
    return !s.isNullOrEmpty() && s.first() == '\"' && s.last() == '\"'
}

fun commaJoin(values: Collection<*>): String {
    return values.joinToString(",") {
        it.toString().replace(",", "/,")
    }
}

fun commaSplit(s: String?): List<String> {
    return if (!s.isNullOrEmpty()) {
        s.split(COMMA_ESCAPED).map {
            it.trim().replace("/,", ",")
        }
    } else {
        emptyList() /* to match commaJoin("") */
    }
}

fun decimalToDMS(
    coordinate: Double, degreeSymbol: String, minuteSymbol: String,
    secondSymbol: String
): String {
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

fun String?.capitalize(): String? {
    return this?.let { substring(0, 1).uppercase() + substring(1) }
}

fun formatDuration(duration: Long?): String? {
    return duration?.let {
        val seconds = duration / 1000
        String.format(Locale.US, "%d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
    }
}

fun readStream(stream: InputStream): String {
    /* \A =	The beginning of the input */
    return Scanner(stream).useDelimiter("\\A").use {
        if (it.hasNext()) return it.next() else ""
    }
}

fun isValidUrl(url: String?): Boolean {
    return !url.isNullOrBlank() && WEB_URL_PATTERN.matcher(url).matches()
}

fun isValidEmailAddress(address: String?): Boolean {
    return !address.isNullOrBlank() && EMAIL_ADDRESS.matcher(address).matches()
}

fun isValidEmailAddressList(addresses: String?): Boolean {
    return !addresses.isNullOrBlank() && commaSplit(addresses).all { isValidEmailAddress(it) }

}

fun String.htmlReplaceUrlsWithLinks(): String {
    val sb = StringBuffer()

    val matcher = WEB_URL_PATTERN.matcher(this)
    while (matcher.find()) {
        val url = matcher.group()
        matcher.appendReplacement(sb, "<a href=\"$url\">$url</a>")
    }
    matcher.appendTail(sb)

    return sb.toString()
}

fun String.httpEncoded(): String {
//    return try {
    return URLEncoder.encode(this, "UTF-8")
//    } catch (x: UnsupportedEncodingException) {
//        throw RuntimeException(x)
//    }
}
