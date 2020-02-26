package com.bopr.android.smailer

import android.content.Context
import java.io.IOException
import java.util.*

class BuildInfo(context: Context) {

    val number: String
    val time: String
    val name: String

    init {
        try {
            val properties = Properties()
            context.assets.open("release.properties").use {
                properties.load(it)
            }
            number = properties.getProperty("build_number")
            time = properties.getProperty("build_time")
            name = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (x: IOException) {
            throw RuntimeException("Cannot read build info", x)
        }
    }

}