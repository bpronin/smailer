package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.os.Bundle
import com.bopr.android.smailer.R
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_EVENTS
import com.bopr.android.smailer.data.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.data.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.updateSummary

/**
 * Main settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainFragment : BasePreferenceFragment(R.xml.pref_main) {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requirePreference(PREF_REMOTE_CONTROL_ENABLED).setOnChangeListener {
//            it.updateSummary(
//                getString(
//                    if (settings.getBoolean(it.key)) R.string.enabled else R.string.disabled
//                ),
//                SUMMARY_STYLE_DEFAULT
//            )
//        }

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
    }

    private fun updateHistoryPreferenceView() {
        requirePreference(PREF_HISTORY).updateSummary(
            getQuantityString(
                R.plurals.new_history_items,
                R.string.new_history_items_zero,
                database.phoneEvents.unreadCount
            )
        )
    }

    companion object {

        private const val PREF_HISTORY = "history"
    }
}
