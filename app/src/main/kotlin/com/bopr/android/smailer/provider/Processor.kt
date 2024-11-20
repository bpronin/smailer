package com.bopr.android.smailer.provider

interface Processor<T> {

    fun add(data: T)

    fun process(): Int

}