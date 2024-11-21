package com.bopr.android.smailer.util

import android.content.Context

interface ContextOwner {

    fun requireContext(): Context
}