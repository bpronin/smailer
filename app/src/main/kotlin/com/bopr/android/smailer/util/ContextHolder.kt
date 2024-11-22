package com.bopr.android.smailer.util

import android.content.Context

interface ContextHolder {

    fun requireContext(): Context
}