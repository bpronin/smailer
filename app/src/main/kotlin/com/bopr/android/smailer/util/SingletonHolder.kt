package com.bopr.android.smailer.util

import android.content.Context
import com.bopr.android.smailer.Settings.Companion.settings

class SingletonHolder<T>(val onCreateInstance: (Context) -> T) {

    @Volatile
    private var instance: T? = null

    fun getInstance(context: Context): T = instance ?: synchronized(this) {
        /* NOTE: we use here context's applicationContext not context itself
        * since we want application-wide singleton */
        instance ?: onCreateInstance(context.applicationContext).also { instance = it }
    }

    fun getInstance(holder: ContextHolder) = holder.requireContext().settings
}