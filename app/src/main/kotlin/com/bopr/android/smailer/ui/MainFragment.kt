package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_EVENTS
import com.bopr.android.smailer.data.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.data.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.util.getQuantityString

/**
 * Main settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainFragment : BasePreferenceFragment() {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver


    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Database(requireContext())
        databaseListener = requireContext().registerDatabaseListener { tables ->
            if (tables.contains(TABLE_PHONE_EVENTS)) updateHistoryPreferenceView()
        }
    }

    override fun onDestroy() {
        requireContext().unregisterDatabaseListener(databaseListener)
        database.close()

        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()

        updateHistoryPreferenceView()
        updateRemoteControlPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            PREF_REMOTE_CONTROL_ENABLED -> updateRemoteControlPreferenceView()
        }
    }

    private fun updateHistoryPreferenceView() {
        requirePreference("history").updateSummary(
            getQuantityString(
                R.plurals.new_history_items,
                R.string.new_history_items_zero,
                database.phoneEvents.unreadCount
            )
        )
    }

    private fun updateRemoteControlPreferenceView() {
//        val preference = requirePreference(PREF_REMOTE_CONTROL_ENABLED)
//        val enabled = settings.getBoolean(preference.key)
//        updateSummary(
//            preference,
//            getString(if (enabled) R.string.enabled else R.string.disabled),
//            SUMMARY_STYLE_DEFAULT
//        )
    }
}
