package com.bopr.android.smailer.util

import android.util.Patterns.EMAIL_ADDRESS
import java.io.InputStream
import java.util.*
import kotlin.math.abs

object TextUtil {

    private val COMMA_ESCAPED = Regex("(?<!/),")  /* matches commas not preceded by "/" */
    private const val REGEX_ = "REGEX:"

    fun escapeRegex(s: String): String {
        return REGEX_ + s
    }

    fun unescapeRegex(s: String?): String? {
        if (s != null) {
            val ix = s.indexOf(REGEX_)
            if (ix != -1) {
                return s.substring(ix + REGEX_.length)
            }
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

    fun commaSplit(s: String): List<String> {
        return if (s.isNotEmpty()) {
            s.split(COMMA_ESCAPED).map {
                it.trim().replace("/,", ",")
            }
        } else {
            listOf() /* important. to match commaJoin("") */
        }
    }

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

    fun capitalize(text: String?): String? {
        return if (text.isNullOrBlank()) {
            text
        } else {
            text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1)
        }
    }

    fun formatDuration(duration: Long?): String? {
        return duration?.let {
            val seconds = duration / 1000
            String.format(Locale.US, "%d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
        }
    }

    fun readStream(stream: InputStream?): String? {
        return stream?.let {
            with(Scanner(stream).useDelimiter("\\A")) {
                return if (hasNext()) return next() else ""
            }
        }
    }

    fun isValidEmailAddress(text: String?): Boolean {
        return !text.isNullOrBlank() && EMAIL_ADDRESS.matcher(text).matches()
    }

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
}