package com.bopr.android.smailer

import android.content.Context
import java.io.IOException
import java.util.Properties

/**
 * Application release build info.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
data class BuildInfo(
    val number: String,
    val time: String,
    val name: String?
) {

    companion object {

        fun get(context: Context): BuildInfo {
            try {
                Properties().run {
                    context.assets.open("release.properties").use {
                        load(it)
                    }
                    return BuildInfo(
                        getProperty("build_number"),
                        getProperty("build_time"),
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    )
                }
            } catch (x: IOException) {
                throw RuntimeException("Cannot read release properties file", x)
            }
        }
    }

}