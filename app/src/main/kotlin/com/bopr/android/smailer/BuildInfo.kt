package com.bopr.android.smailer

import android.content.Context
import java.io.IOException
import java.util.*

/**
 * Application release build info.
 */
class BuildInfo(context: Context) {

    val number: String
    val time: String
    val name: String

    init {
        try {
            Properties().run {
                context.assets.open("release.properties").use {
                    load(it)
                }
                number = getProperty("build_number")
                time = getProperty("build_time")
            }
            name = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (x: IOException) {
            throw RuntimeException("Cannot read release properties file", x)
        }
    }

}