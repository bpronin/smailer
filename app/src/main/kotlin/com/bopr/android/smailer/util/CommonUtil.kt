package com.bopr.android.smailer.util

import java.io.Closeable

inline fun <T : Closeable?, R> T.useIt(block: T.() -> R): R {
    return use { it.block() }
}
