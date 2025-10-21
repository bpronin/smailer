package com.bopr.android.smailer.util

import android.os.Parcel
import android.os.Parcelable
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import java.util.Locale
import kotlin.reflect.KClass

fun parcelize(value: Parcelable): ByteArray {
    Parcel.obtain().run {
        try {
            writeValue(value)
            return marshall()
        } finally {
            recycle()
        }
    }
}

fun <T : Parcelable> unparcelize(bytes: ByteArray, valueClass: KClass<T>): T {
    Parcel.obtain().run {
        try {
            unmarshall(bytes, 0, bytes.size)
            setDataPosition(0)
            @Suppress("UNCHECKED_CAST")
            return readValue(valueClass.java.getClassLoader()) as T
        } finally {
            recycle()
        }
    }
}

/**
 * Parses a locale string to a Locale object.
 *
 * The code can be a standard language tag (e.g., "en-US") or use an underscore ("en_US").
 * If the code matches VAL_PREF_DEFAULT, the system's default locale is returned.
 *
 * @param code The locale string to parse.
 * @return The corresponding Locale object.
 * @throws IllegalArgumentException if the code format is invalid.
 */
fun parseLocale(code: String): Locale {
    if (code == VAL_PREF_DEFAULT) {
        return Locale.getDefault()
    }

    // The standard format is "en-US", so we replace "_" with "-" to be compliant.
    val languageTag = code.replace('_', '-')

    // Locale.forLanguageTag is robust and handles "en", "en-US", etc.
    // It returns an empty locale for malformed tags, so we can check for that.
    val locale = Locale.forLanguageTag(languageTag)

    if (locale.language.isEmpty()) {
        throw IllegalArgumentException("Invalid locale code: $code")
    }

    return locale
}
