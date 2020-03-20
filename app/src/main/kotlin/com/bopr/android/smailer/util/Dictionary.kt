package com.bopr.android.smailer.util

class Dictionary<K, V>(vararg values: Pair<K, V>) {

    private val map = mapOf(*values)

    operator fun get(key: K): V {
        return map.get(key) ?: throw NoSuchElementException()
    }
}
