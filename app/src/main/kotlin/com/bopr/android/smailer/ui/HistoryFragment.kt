package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
import com.bopr.android.smailer.ui.HistoryFragment.Holder
import com.bopr.android.smailer.util.*
import com.bopr.android.smailer.util.AddressUtil.containsPhone
import com.bopr.android.smailer.util.AddressUtil.findPhone
import com.bopr.android.smailer.util.Dialogs.showConfirmationDialog

/**
 * Application activity log activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HistoryFragment : RecyclerFragment<PhoneEvent, Holder>() {

    private lateinit var database: Database
    private lateinit var phoneEventFilter: PhoneEventFilter
    private lateinit var settingsChangeListener: OnSharedPreferenceChangeListener
    private lateinit var databaseListener: BroadcastReceiver
    private lateinit var formatter: TagFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsChangeListener = SettingsListener()
        settings.registerOnSharedPreferenceChangeListener(settingsChangeListener)

        database = Database(requireContext())
        databaseListener = registerDatabaseListener(requireContext(), DatabaseListener())

        formatter = TagFormatter(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(settingsChangeListener)
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

        val blacklisted = containsPhone(phoneEventFilter.phoneBlacklist, item.phone)
        val whitelisted = containsPhone(phoneEventFilter.phoneWhitelist, item.phone)

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

    override fun onItemClick(item: PhoneEvent) {
        HistoryDetailsDialogFragment(item).showDialog(requireActivity())
    }

    override fun loadItems(): Collection<PhoneEvent> {
        phoneEventFilter = settings.getFilter()
        return database.events.toList()
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
        showConfirmationDialog(requireContext(),
                messageRes = R.string.ask_clear_history,
                buttonTextRes = R.string.clear) {
            database.clearEvents()
            database.notifyChanged()
        }
    }

    private fun onMarkAllAsRead() {
        database.markAllAsRead(true)
        database.notifyChanged()
    }

    private fun onAddToBlacklist() {
        addSelectedItemToFilter(phoneEventFilter.phoneBlacklist, R.string.add_to_blacklist)
    }

    private fun onAddToWhitelist() {
        addSelectedItemToFilter(phoneEventFilter.phoneWhitelist, R.string.add_to_whitelist)
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
            removeFromFilter(phoneEventFilter.phoneWhitelist, it.phone)
            removeFromFilter(phoneEventFilter.phoneBlacklist, it.phone)

            settings.edit().putFilter(phoneEventFilter).apply()

            showToast(requireContext(),
                    getString(R.string.phone_removed_from_filter).format(it.phone))
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
                            showToast(requireContext(),
                                    getString(R.string.item_already_exists).format(value))
                        } else {
                            list.add(value)
                            settings.edit().putFilter(phoneEventFilter).apply()
                        }
                    }
                }
            }.showDialog(requireActivity())
        }
    }

    private fun removeFromFilter(list: MutableSet<String>, number: String) {
        list.remove(findPhone(list, number))
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
                formatter.pattern(R.string.call_of_duration_short)
                        .put("duration", formatDuration(event.callDuration))
                        .format()
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

    private inner class SettingsListener : OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
            //todo granularize
            refreshItems()
        }
    }

    private inner class DatabaseListener : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            refreshItems()
        }
    }
}