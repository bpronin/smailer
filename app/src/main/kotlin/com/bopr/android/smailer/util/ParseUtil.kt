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

fun parseLocale(code: String): Locale {
    return if (code == VAL_PREF_DEFAULT) {
        Locale.getDefault()
    } else {
        val a = code.split("_")
        if (a.size == 2) {
            Locale(a[0], a[1])
        } else {
            throw IllegalArgumentException("Invalid locale code: $code")
        }
    }
}