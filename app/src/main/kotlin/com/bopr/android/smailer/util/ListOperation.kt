package com.bopr.android.smailer.util

import android.os.Parcelable
import com.bopr.android.smailer.util.ListOperation.Action.ACTION_DELETE
import com.bopr.android.smailer.util.ListOperation.Action.ACTION_UPDATE
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class ListOperation<T>(val action: Action, val value: @RawValue T):Parcelable {

    enum class Action {
        ACTION_UPDATE,
        ACTION_DELETE;

        infix fun <T> of(value: T): ListOperation<T> = ListOperation(this, value)
    }

    companion object {

        fun <T> Collection<T>.getUpdates(new: Collection<T>): List<ListOperation<T>> {
            val operations = mutableListOf<ListOperation<T>>()

            for (e in this) {
                if (!new.contains(e)) {
                    operations.add(ListOperation(ACTION_DELETE, e))
                }
            }

            for (e in new) {
                if (!contains(e)) {
                    operations.add(ListOperation(ACTION_UPDATE, e))
                }
            }

            return operations
        }

        fun <T> MutableCollection<T>.applyUpdates(operations: List<ListOperation<T>>) {
            for (op in operations) {
                when (op.action) {
                    ACTION_DELETE ->
                        remove(op.value)
                    ACTION_UPDATE ->
                        add(op.value)
                }
            }
        }

        fun <T> MutableCollection<T>.applyUpdates(vararg operations: ListOperation<T>) {
            applyUpdates(operations.toList())
        }

    }
}