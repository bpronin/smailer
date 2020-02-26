package com.bopr.android.smailer.ui


import android.content.BroadcastReceiver
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_NUMBER_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TEXT_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TRIGGER_OFF
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
import com.bopr.android.smailer.ui.HistoryFragment.Holder
import com.bopr.android.smailer.util.AddressUtil.containsPhone
import com.bopr.android.smailer.util.TextUtil.formatDuration
import com.bopr.android.smailer.util.UiUtil.eventDirectionImage
import com.bopr.android.smailer.util.UiUtil.eventStateImage
import com.bopr.android.smailer.util.UiUtil.eventTypeImage
import com.bopr.android.smailer.util.UiUtil.showToast

/**
 * Application activity log activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HistoryFragment : RecyclerFragment<PhoneEvent, Holder>(), OnSharedPreferenceChangeListener {

    private lateinit var database: Database
    private lateinit var callFilter: PhoneEventFilter
    private lateinit var databaseListener: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings.registerOnSharedPreferenceChangeListener(this)

        database = Database(requireContext())
        databaseListener = registerDatabaseListener(requireContext()) {
            refreshItems()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
        unregisterDatabaseListener(requireContext(), databaseListener)
        database.close()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_log_clear -> {
                onClearData()
                true
            }
            R.id.action_log_mar_all_read -> {
                onMarkAllAsRead()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateItemContextMenu(menu: ContextMenu, item: PhoneEvent) {
        requireActivity().menuInflater.inflate(R.menu.menu_context_history, menu)

        if (item.state != STATE_PENDING) {
            menu.removeItem(R.id.action_ignore)
        }

        val blacklisted = containsPhone(callFilter.phoneBlacklist, item.phone)
        val whitelisted = containsPhone(callFilter.phoneWhitelist, item.phone)

        if (blacklisted) {
            menu.removeItem(R.id.action_add_to_blacklist)
        }

        if (whitelisted) {
            menu.removeItem(R.id.action_add_to_whitelist)
        }

        if (!blacklisted && !whitelisted) {
            menu.removeItem(R.id.action_remove_from_lists)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_to_blacklist -> {
                onAddToBlacklist()
                true
            }
            R.id.action_add_to_whitelist -> {
                onAddToWhitelist()
                true
            }
            R.id.action_remove_from_lists -> {
                onRemoveFromLists()
                true
            }
            R.id.action_ignore -> {
                onMarkAsIgnored()
                true
            }
            else ->
                super.onContextItemSelected(item)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_EMAIL_TRIGGERS,
            PREF_FILTER_PHONE_BLACKLIST,
            PREF_FILTER_PHONE_WHITELIST,
            PREF_FILTER_TEXT_BLACKLIST,
            PREF_FILTER_TEXT_WHITELIST -> {
                refreshItems()
            }
        }
    }

    override fun onItemClick(item: PhoneEvent) {
        HistoryDetailsDialogFragment(item).show(requireActivity())
    }

    override fun loadItems(): Collection<PhoneEvent> {
        callFilter = settings.callFilter
        return database.events.list()
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_log, parent, false)
        return Holder(view)
    }

    override fun bindViewHolder(item: PhoneEvent, holder: Holder) {
        holder.timeView.text = DateFormat.format(getString(R.string._time_pattern), item.startTime)
        holder.textView.text = formatSummary(item)
        holder.phoneView.text = item.phone
        holder.typeView.setImageResource(eventTypeImage(item))
        holder.directionView.setImageResource(eventDirectionImage(item))
        holder.stateView.setImageResource(eventStateImage(item))
        holder.typeView.isEnabled = item.stateReason and REASON_TRIGGER_OFF == 0
        holder.directionView.isEnabled = item.stateReason and REASON_TRIGGER_OFF == 0
        holder.textView.isEnabled = item.stateReason and REASON_TEXT_BLACKLISTED == 0
        holder.phoneView.isEnabled = item.stateReason and REASON_NUMBER_BLACKLISTED == 0

        markItemAsRead(item)
    }

    private fun onClearData() {
        ConfirmDialog(getString(R.string.ask_clear_history)) {
            database.clearEvents()
            database.notifyChanged()
        }.show(requireActivity())
    }

    private fun onMarkAllAsRead() {
        database.markAllAsRead(true)
        database.notifyChanged()
    }

    private fun onAddToBlacklist() {
        addSelectedItemToFilter(callFilter.phoneBlacklist, R.string.add_to_blacklist)
    }

    private fun onAddToWhitelist() {
        addSelectedItemToFilter(callFilter.phoneWhitelist, R.string.add_to_whitelist)
    }

    private fun onMarkAsIgnored() {
        getSelectedItem()?.let {
            it.state = STATE_IGNORED
            database.putEvent(it)
            database.notifyChanged()
        }
    }

    private fun onRemoveFromLists() {
        getSelectedItem()?.let {
            callFilter.phoneWhitelist.remove(it.phone)
            callFilter.phoneBlacklist.remove(it.phone)
            saveCallFilter()

            showToast(requireContext(), getString(R.string.phone_removed_from_filter, it.phone))
        }
    }

    private fun addSelectedItemToFilter(list: MutableSet<String>, @StringRes titleRes: Int) {
        getSelectedItem()?.let {
            EditPhoneDialogFragment().apply {
                setTitle(titleRes)
                setValue(it.phone)
                setOnOkClicked { value ->
                    if (!value.isNullOrEmpty()) {
                        if (list.contains(value)) {
                            showToast(requireContext(), getString(R.string.item_already_exists, value))
                        } else {
                            list.add(value)
                            saveCallFilter()
                        }
                    }
                }
            }.show(requireActivity())
        }
    }

    private fun saveCallFilter() {
        settings.edit().putFilter(callFilter).apply()
    }

    private fun markItemAsRead(event: PhoneEvent) {
        if (!event.isRead) {
            event.isRead = true
            database.putEvent(event)
        }
    }

    private fun formatSummary(event: PhoneEvent): CharSequence? {
        return when {
            event.isSms ->
                event.text
            event.isMissed ->
                getString(R.string.missed_call)
            else ->
                getString(R.string.call_of_duration_short, formatDuration(event.callDuration))
        }
    }

    inner class Holder(view: View) : ViewHolder(view) {

        val typeView: ImageView = view.findViewById(R.id.list_item_type)
        val directionView: ImageView = view.findViewById(R.id.list_item_direction)
        val phoneView: TextView = view.findViewById(R.id.list_item_phone)
        val timeView: TextView = view.findViewById(R.id.list_item_time)
        val textView: TextView = view.findViewById(R.id.list_item_text)
        val stateView: ImageView = view.findViewById(R.id.list_item_state)
    }
}