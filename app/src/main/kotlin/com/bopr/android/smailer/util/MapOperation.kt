package com.bopr.android.smailer.util

import com.bopr.android.smailer.util.MapOperation.Action.*

data class MapOperation<K, V>(val action: Action, val key: K, val value: V? = null) {

    enum class Action {
        ACTION_ADD,
        ACTION_UPDATE,
        ACTION_DELETE;

        infix fun <K, V> of(data: Pair<K, V>): MapOperation<K, V> =
                MapOperation(this, data.first, data.second)

        infix fun <K, V> of(key: K): MapOperation<K, V> = MapOperation(this, key)
    }

    companion object {

        fun <K, V> Map<K, V>.getUpdates(new: Map<K, V>): List<MapOperation<K, V>> {
            val operations = mutableListOf<MapOperation<K, V>>()

            for (e in entries) {
                if (!new.containsKey(e.key)) {
                    operations.add(MapOperation(ACTION_DELETE, e.key, e.value))
                }
            }

            for (e in new.entries) {
                if (containsKey(e.key)) {
                    operations.add(MapOperation(ACTION_UPDATE, e.key, e.value))
                } else {
                    operations.add(MapOperation(ACTION_ADD, e.key, e.value))
                }
            }

            return operations
        }

        fun <K, V> MutableMap<K, V>.applyUpdates(operations: List<MapOperation<K, V>>) {
            for (op in operations) {
                when (op.action) {
                    ACTION_DELETE ->
                        remove(op.key)
                    ACTION_ADD, ACTION_UPDATE ->
                        put(op.key, op.value!!)
                }
            }
        }

        fun <K, V> MutableMap<K, V>.applyUpdates(vararg operations: MapOperation<K, V>) {
            return applyUpdates(operations.asList())
        }

    }
}