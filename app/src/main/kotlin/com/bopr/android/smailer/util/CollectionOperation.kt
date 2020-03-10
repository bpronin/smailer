package com.bopr.android.smailer.util

import com.bopr.android.smailer.util.CollectionOperation.Action.ACTION_ADD
import com.bopr.android.smailer.util.CollectionOperation.Action.ACTION_DELETE


data class CollectionOperation<T>(val action: Action, val value: T) {

    enum class Action {
        ACTION_ADD,
        ACTION_DELETE;

        infix fun <T> of(value: T): CollectionOperation<T> = CollectionOperation(this, value)
    }

    companion object {

        fun <T> Collection<T>.getUpdates(new: Collection<T>): List<CollectionOperation<T>> {
            val operations = mutableListOf<CollectionOperation<T>>()

            for (e in this) {
                if (!new.contains(e)) {
                    operations.add(CollectionOperation(ACTION_DELETE, e))
                }
            }

            for (e in new) {
                if (!contains(e)) {
                    operations.add(CollectionOperation(ACTION_ADD, e))
                }
            }

            return operations
        }

        fun <T> MutableCollection<T>.applyUpdates(operations: List<CollectionOperation<T>>) {
            for (op in operations) {
                when (op.action) {
                    ACTION_DELETE ->
                        remove(op.value)
                    ACTION_ADD ->
                        add(op.value)
                }
            }
        }

        fun <T> MutableCollection<T>.applyUpdates(vararg operations: CollectionOperation<T>) {
            applyUpdates(operations.toList())
        }

    }
}