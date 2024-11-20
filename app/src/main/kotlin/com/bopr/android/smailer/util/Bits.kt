package com.bopr.android.smailer.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Bits(private var value: Int = 0) : Parcelable {

    private fun bits(index: Int) = 1 shl index

    private fun add(otherValue: Int) = value or otherValue

    private fun sub(otherValue: Int) = value and otherValue.inv()

    operator fun get(index: Int) = (value and bits(index)) != 0

    operator fun set(index: Int, on: Boolean): Bits {
        value = if (on) add(bits(index)) else sub(bits(index))
        return this
    }

    operator fun plus(other: Bits): Bits {
        value = add(other.value)
        return this
    }

    operator fun minus(other: Bits): Bits {
        value = sub(other.value)
        return this
    }

    operator fun contains(other: Bits) = (value and other.value) != 0

    override fun equals(other: Any?) = (other is Bits) && (value == other.value)

    override fun hashCode() = value.hashCode()

    override fun toString() = value.toString(2)

    fun toInt() = value

    fun isEmpty() = value == 0

    fun isNotEmpty() = value != 0

    companion object {

        fun bit(index: Int) = Bits().set(index, true)
    }
}
