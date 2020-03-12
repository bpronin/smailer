package com.bopr.android.smailer.ui


import android.content.BroadcastReceiver
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.view.LayoutInflater.from
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
import com.bopr.android.smailer.ui.HistoryFragment.Holder
import com.bopr.android.smailer.util.*
import com.google.android.material.snackbar.Snackbar

/**
 * Call history log fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
//todo grouping by phone number
class HistoryFragment : RecyclerFragment<PhoneEvent, Holder>(), OnSharedPreferenceChangeListener {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver
    private var defaultItemTextColor: Int = 0
    private var unreadItemTextColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        emptyTextRes = R.string.history_is_empty

        val context = requireContext()

        defaultItemTextColor = context.getColorFromAttr(android.R.attr.textColorSecondary)
        unreadItemTextColor = context.getColorFromAttr(android.R.attr.textColorPrimary)

        settings.registerOnSharedPreferenceChangeListener(this)

        database = Database(context)
        databaseListener = context.registerDatabaseListener {
            refreshItems()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        recycler.addOnItemSwipedListener {
            updateSelectedItemPosition(it)
            onRemoveSelected()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
        requireContext().unregisterDatabaseListener(databaseListener)
        database.close()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_history, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
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

        val blacklisted = settings.getStringList(PREF_FILTER_PHONE_BLACKLIST).containsPhone(item.phone)
        val whitelisted = settings.getStringList(PREF_FILTER_PHONE_WHITELIST).containsPhone(item.phone)

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
            R.id.action_remove_item -> {
                onRemoveSelected()
                true
            }
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
        HistoryDetailsDialogFragment(item).show(this)
    }

    override fun loadItems(): Collection<PhoneEvent> {
        return database.events.list()
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        val view = from(requireContext()).inflate(R.layout.list_item_log, parent, false)
        return Holder(view)
    }

    override fun bindViewHolder(item: PhoneEvent, holder: Holder) {
        holder.timeView.text = DateFormat.format(getString(R.string._time_pattern), item.startTime)
        holder.textView.text = formatSummary(item)
        holder.phoneView.text = item.phone
        holder.typeView.setImageResource(eventTypeImage(item))
        holder.directionView.setImageResource(eventDirectionImage(item))
        holder.stateView.setImageResource(eventStateImage(item))

        if (!item.isRead) {
            holder.phoneView.setTextColor(unreadItemTextColor)
            holder.textView.setTextColor(unreadItemTextColor)
            holder.timeView.setTextColor(unreadItemTextColor)
        } else {
            holder.phoneView.setTextColor(defaultItemTextColor)
            holder.textView.setTextColor(defaultItemTextColor)
            holder.timeView.setTextColor(defaultItemTextColor)
        }

        markItemAsRead(item)
    }

    private fun onClearData() {
        ConfirmDialog(getString(R.string.ask_clear_history)) {
            database.clearEvents()
            database.notifyChanged()
        }.show(this)
    }

    private fun onMarkAllAsRead() {
        database.markAllEventsAsRead(true)
        database.notifyChanged()
        showToast(R.string.operation_complete)
    }

    private fun onAddToBlacklist() {
        addSelectedItemToFilter(PREF_FILTER_PHONE_BLACKLIST, R.string.add_to_blacklist)
    }

    private fun onAddToWhitelist() {
        addSelectedItemToFilter(PREF_FILTER_PHONE_WHITELIST, R.string.add_to_whitelist)
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
            settings.run {
                val blacklist = getStringList(PREF_FILTER_PHONE_BLACKLIST)
                val whitelist = getStringList(PREF_FILTER_PHONE_WHITELIST)
                blacklist.remove(it.phone)
                whitelist.remove(it.phone)
                update {
                    putStringList(PREF_FILTER_PHONE_BLACKLIST, blacklist)
                    putStringList(PREF_FILTER_PHONE_WHITELIST, whitelist)
                }
            }

            showToast(getString(R.string.phone_removed_from_filter, it.phone))
        }
    }

    private fun onRemoveSelected() {
        val events = listAdapter.getItemsAt(selectedItemPosition)

        database.deleteEvents(events)
        database.notifyChanged()

        Snackbar.make(recycler,
                        getQuantityString(R.plurals.items_removed, events.size),
                        Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccentText))
                .setAction(R.string.undo) {
                    database.putEvents(events)
                    database.notifyChanged()
                }
                .show()
    }

    private fun addSelectedItemToFilter(settingName: String, @StringRes titleRes: Int) {
        getSelectedItem()?.let {
            EditPhoneDialogFragment().apply {
                setTitle(titleRes)
                setValue(it.phone)
                setOnOkClicked { value ->
                    if (!value.isNullOrEmpty()) {
                        settings.run {
                            val list = getStringList(settingName)
                            if (list.contains(value)) {
                                showToast(getString(R.string.item_already_exists, value))
                            } else {
                                list.add(value)
                                update { putStringList(settingName, list) }
                            }
                        }
                    }
                }
            }.show(this)
        }
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
            event.isIncoming ->
                getString(R.string.incoming_call_of, formatDuration(event.callDuration))
            else ->
                getString(R.string.outgoing_call_of, formatDuration(event.callDuration))
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