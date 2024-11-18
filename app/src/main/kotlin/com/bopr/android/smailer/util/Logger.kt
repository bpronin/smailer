package com.bopr.android.smailer.util

import android.util.Log

class Logger(name: String) {

    private val tag = "app.$name"

    fun verb(message: Any, error:Throwable? = null): Logger {
        Log.v(tag, message.toString(), error)
        return this
    }

    fun debug(message: Any, error:Throwable? = null): Logger {
        Log.d(tag, message.toString(), error)
        return this
    }

    fun info(message: Any, error:Throwable? = null): Logger {
        Log.i(tag, message.toString(), error)
        return this
    }

    fun warn(message: Any, error:Throwable? = null): Logger {
        Log.w(tag, message.toString(), error)
        return this
    }

    fun error(message: Any, error:Throwable? = null): Logger {
        Log.e(tag, message.toString(), error)
        return this
    }

}