package com.bopr.android.smailer.util

/**
 * Miscellaneous utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object Util {
    //TODO: remove this

    @JvmStatic
    fun isEmpty(set: Set<*>?): Boolean = set.isNullOrEmpty()

    @JvmStatic
    @SafeVarargs
    fun <T> asSet(vararg values: T): Set<T> = values.toSet()

    @JvmStatic
    fun <T> toSet(collection: Collection<T>): Set<T> = collection.toSet()

    @JvmStatic
    fun toArray(collection: Collection<String>): Array<String> = collection.toTypedArray()

    @JvmStatic
    fun safeEquals(a: Any?, b: Any?): Boolean {
        return a === b || a != null && a == b
    }

    @JvmStatic
    fun <T> requireNonNull(obj: T?): T {
        if (obj == null) {
            throw NullPointerException()
        }
        return obj
    }

}

//fun Any?.safeEquals(other: Any?): Boolean {
//    return this?.equals(other) ?: (other === null)
//}