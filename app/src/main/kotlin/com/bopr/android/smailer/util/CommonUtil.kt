package com.bopr.android.smailer.util

import java.io.Closeable

inline fun <T : Closeable?, R> T.useIt(block: T.() -> R): R {
    return use { block() }
}

fun Any?.isNull() = this == null

fun Any?.nonNull() = this != null