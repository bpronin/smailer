package com.bopr.android.smailer.util

import android.util.Log

class Logger(name: String) {

    private val tag = "app.$name"

//    fun trace(message: String, error:Throwable? = null) {
//        Log.v(tag, message, error)
//    }

    fun debug(message: String, error:Throwable? = null) {
        Log.d(tag, message, error)
    }

    fun info(message: String, error:Throwable? = null) {
        Log.i(tag, message, error)
    }

    fun warn(message: String, error:Throwable? = null) {
        Log.w(tag, message, error)
    }

    fun error(message: String, error:Throwable? = null) {
        Log.e(tag, message, error)
    }

}