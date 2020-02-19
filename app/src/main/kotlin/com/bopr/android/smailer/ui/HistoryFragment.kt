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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
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
import com.bopr.android.smailer.util.AddressUtil.containsPhone
import com.bopr.android.smailer.util.AddressUtil.findPhone
import com.bopr.android.smailer.util.Dialogs.showConfirmationDialog
import com.bopr.android.smailer.util.TagFormatter
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
class HistoryFragment : BaseFragment() {

    private lateinit var database: Database
    private lateinit var listView: RecyclerView
    private lateinit var listAdapter: ListAdapter
    private lateinit var phoneEventFilter: PhoneEventFilter
    private lateinit var settingsChangeListener: OnSharedPreferenceChangeListener
    private lateinit var databaseListener: BroadcastReceiver
    private lateinit var formatter: TagFormatter
    private var selectedListItemPosition = NO_POSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsChangeListener = SettingsListener()
        settings.registerOnSharedPreferenceChangeListener(settingsChangeListener)

        database = Database(requireContext())
        databaseListener = registerDatabaseListener(requireContext(), DatabaseListener())

        formatter = TagFormatter(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_log, container, false)

        listView = view.findViewById(android.R.id.list)
        listView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        return view
    }

    override fun onStart() {
        super.onStart()
        loadData()
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

    private fun onClearData() {
        showConfirmationDialog(requireContext(), messageRes = R.string.ask_clear_history,
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
        addToPhoneList(phoneEventFilter.phoneBlacklist) {
            settings.edit().putFilter(phoneEventFilter).apply()
        }
    }

    private fun onAddToWhitelist() {
        addToPhoneList(phoneEventFilter.phoneWhitelist) {
            settings.edit().putFilter(phoneEventFilter).apply()
        }
    }

    private fun onRemoveFromLists() {
        if (selectedListItemPosition != NO_POSITION) {
            val number = listAdapter.getItem(selectedListItemPosition).phone
            removeFromPhoneLists(phoneEventFilter.phoneWhitelist, number)
            removeFromPhoneLists(phoneEventFilter.phoneBlacklist, number)
            settings.edit().putFilter(phoneEventFilter).apply()
            showToast(requireContext(), formatter.pattern(R.string.phone_removed_from_filter)
                    .put("number", number)
                    .format())
        }
    }

    private fun onMarkAsIgnored() {
        if (selectedListItemPosition != NO_POSITION) {
            val event = listAdapter.getItem(selectedListItemPosition)
            event.state = STATE_IGNORED
            database.putEvent(event)
            database.notifyChanged()
        }
    }

    private fun addToPhoneList(list: MutableSet<String>, commit: () -> Unit) {
        if (selectedListItemPosition != NO_POSITION) {
            EditPhoneDialogFragment().apply {
                setTitle(R.string.add)
                setInitialValue(listAdapter.getItem(selectedListItemPosition).phone)
                setOnOkClicked { value ->
                    if (!value.isNullOrEmpty()) {
                        if (list.contains(value)) {
                            showToast(requireContext(), formatter
                                    .pattern(R.string.item_already_exists)
                                    .put("item", value)
                                    .format())
                        } else {
                            list.add(value)
                            commit()
                        }
                    }
                }
            }.showDialog(requireActivity())
        }
    }

    private fun removeFromPhoneLists(list: MutableSet<String>, number: String) {
        list.remove(findPhone(list, number))
    }

    private fun loadData() {
        listAdapter = ListAdapter(database.events.toList())
        listView.adapter = listAdapter
        phoneEventFilter = settings.getFilter()
        updateEmptyText()
    }

    private fun updateEmptyText() {
        val view = view
        if (view != null) {
            val text = view.findViewById<TextView>(R.id.text_empty)
            if (listAdapter.itemCount == 0) {
                text.visibility = View.VISIBLE
            } else {
                text.visibility = View.GONE
            }
        }
    }

    private fun showDetails() {
        if (selectedListItemPosition != NO_POSITION) {
            val event = listAdapter.getItem(selectedListItemPosition)
            HistoryDetailsDialogFragment(event).showDialog(requireActivity())
        }
    }

    private inner class ListAdapter(private val items: List<PhoneEvent>) : Adapter<ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val inflater = LayoutInflater.from(requireContext())
            return ItemViewHolder(inflater.inflate(R.layout.list_item_log, parent, false))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val event = getItem(position)

            holder.timeView.text = DateFormat.format(getString(R.string._time_pattern), event.startTime)
            holder.textView.text = formatSummary(event)
            holder.phoneView.text = event.phone
            holder.typeView.setImageResource(eventTypeImage(event))
            holder.directionView.setImageResource(eventDirectionImage(event))
            holder.stateView.setImageResource(eventStateImage(event))
            holder.typeView.isEnabled = event.stateReason and REASON_TRIGGER_OFF == 0
            holder.directionView.isEnabled = event.stateReason and REASON_TRIGGER_OFF == 0
            holder.textView.isEnabled = event.stateReason and REASON_TEXT_BLACKLISTED == 0
            holder.phoneView.isEnabled = event.stateReason and REASON_NUMBER_BLACKLISTED == 0
            holder.itemView.setOnClickListener {
                selectedListItemPosition = holder.adapterPosition
                showDetails()
            }
            holder.itemView.setOnLongClickListener {
                selectedListItemPosition = holder.adapterPosition
                false
            }
            holder.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                requireActivity().menuInflater.inflate(R.menu.menu_context_history, menu)
                if (event.state != STATE_PENDING) {
                    menu.removeItem(R.id.action_ignore)
                }
                val blacklisted = containsPhone(phoneEventFilter.phoneBlacklist, event.phone)
                val whitelisted = containsPhone(phoneEventFilter.phoneWhitelist, event.phone)
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

            markAsRead(event)
        }

        private fun markAsRead(event: PhoneEvent) {
            if (!event.isRead) {
                event.isRead = true
                database.putEvent(event)
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun getItem(position: Int): PhoneEvent {
            return items[position]
        }

        private fun formatSummary(event: PhoneEvent): CharSequence? {
            return when {
                event.isSms ->
                    event.text
                event.isMissed ->
                    getString(R.string.missed_call)
                else ->
                    formatter
                            .pattern(R.string.call_of_duration_short)
                            .put("duration", formatDuration(event.callDuration))
                            .format()
            }
        }

    }

    private inner class ItemViewHolder(view: View) : ViewHolder(view) {

        val typeView: ImageView = view.findViewById(R.id.list_item_type)
        val directionView: ImageView = view.findViewById(R.id.list_item_direction)
        val phoneView: TextView = view.findViewById(R.id.list_item_phone)
        val timeView: TextView = view.findViewById(R.id.list_item_time)
        val textView: TextView = view.findViewById(R.id.list_item_text)
        val stateView: ImageView = view.findViewById(R.id.list_item_state)
    }

    private inner class SettingsListener : OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
            loadData()
        }
    }

    private inner class DatabaseListener : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            loadData()
        }
    }
}