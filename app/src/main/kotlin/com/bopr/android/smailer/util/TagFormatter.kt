package com.bopr.android.smailer.util

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import java.util.*
import java.util.regex.Pattern

/**
 * Tagged string formatter.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TagFormatter(private val resources: Resources) {

    private val values: MutableMap<String, String?> = LinkedHashMap()

    constructor(context: Context) : this(context.resources)

    fun pattern(pattern: String): TagPattern {
        return TagPattern(pattern)
    }

    fun pattern(@StringRes resourceId: Int): TagPattern {
        return pattern(resources.getString(resourceId))
    }

    override fun toString(): String {
        return "TagFormatter{" +
                "values=" + values +
                ", resources=" + resources +
                '}'
    }

    companion object {

        /* NOTE: "\\}" works differently on android device and pure java */
        @Suppress("RegExpRedundantEscape")
        private val VALUE_PATTERN = Pattern.compile("\\{(.*?)\\}")
    }

    inner class TagPattern internal constructor(private val pattern: String) {

        fun put(key: String, value: String?): TagPattern {
            values[key] = value
            return this
        }

        fun put(key: String, @StringRes resourceId: Int): TagPattern {
            return put(key, resources.getString(resourceId))
        }

        fun put(key: String, pattern: TagPattern): TagPattern {
            return put(key, pattern.format())
        }

        fun format(): String {
            val sb = StringBuffer()

            val matcher = VALUE_PATTERN.matcher(pattern)
            while (matcher.find()) {
                values[matcher.group(1)!!]?.let {
                    matcher.appendReplacement(sb, it)
                }
            }
            matcher.appendTail(sb)

            return sb.toString()
        }

        override fun toString(): String {
            return format()
        }

    }

}