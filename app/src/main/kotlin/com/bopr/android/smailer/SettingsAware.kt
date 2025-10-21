package com.bopr.android.smailer

import android.content.Context
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.Disposable

/**
 * Common [Settings] holder.
 * 
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
abstract class SettingsAware(context: Context) : Disposable {

    protected val settings = context.settings
    private val settingsListener = settings.registerListener(::onSettingsChanged)

    protected abstract fun onSettingsChanged(settings: Settings, key: String)

    override fun dispose() {
        settings.unregisterListener(settingsListener)
    }
}