package com.bopr.android.smailer.util

/**
 * Miscellaneous utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object Util {
    //TODO: remove this

    @JvmStatic
    @SafeVarargs
    fun <T> asSet(vararg values: T): MutableSet<T> = values.toMutableSet()

    @JvmStatic
    fun <T> toSet(collection: Collection<T>): Set<T> = collection.toSet()

    @JvmStatic
    fun safeEquals(a: Any?, b: Any?): Boolean {
        return a === b || a != null && a == b
    }

    @JvmStatic
    fun <T : Any> requireNonNull(obj: T?): T = requireNotNull(obj)

}

//fun Any?.safeEquals(other: Any?): Boolean {
//    return this?.equals(other) ?: (other === null)
//}