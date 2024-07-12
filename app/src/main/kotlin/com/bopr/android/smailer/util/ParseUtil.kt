package com.bopr.android.smailer.util

import android.content.Intent
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.bopr.android.smailer.PhoneEvent
import kotlin.reflect.KClass

fun Intent.getPhoneEventExtra(name: String): PhoneEvent? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, PhoneEvent::class.java)
    } else {
        @Suppress("DEPRECATION")
        return getParcelableExtra(name)
    }
}

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