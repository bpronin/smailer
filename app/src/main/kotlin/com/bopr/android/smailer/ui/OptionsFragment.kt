package com.bopr.android.smailer.ui

import android.os.Bundle
import com.bopr.android.smailer.R

/**
 * Options fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class OptionsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_options)
        //todo add reset dialogs action here
    }
}
