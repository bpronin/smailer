package com.bopr.android.smailer.ui


import android.content.BroadcastReceiver
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
import com.bopr.android.smailer.ListDataset
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.HistoryFragment.Holder
import com.bopr.android.smailer.util.*
import com.google.android.material.snackbar.Snackbar

/**
 * Call history log fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
//todo grouping by phone number
class HistoryFragment : RecyclerFragment<PhoneEvent, Holder>() {

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

        database = Database(context)
        databaseListener = context.registerDatabaseListener { _, _ ->
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

        val blacklisted = database.phoneBlacklist.contains(item.phone)
        val whitelisted = database.phoneWhitelist.contains(item.phone)

        if (blacklisted || whitelisted) {
            menu.removeItem(R.id.action_add_to_blacklist)
            menu.removeItem(R.id.action_add_to_whitelist)
        } else {
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
                onRemoveFromFilterList()
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
        HistoryDetailsDialogFragment(item).show(this)
    }

    override fun loadItems(): Collection<PhoneEvent> {
        return database.events
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

        if (!item.isRead) {
            item.isRead = true
            database.events.add(item) /* do not fire broadcast here */
        }
    }

    private fun onClearData() {
        ConfirmDialog(getString(R.string.ask_clear_history)) {
            database.commit { batch { events.clear() } }
        }.show(this)
    }

    private fun onMarkAllAsRead() {
        database.commit { batch { events.markAllAsRead(true) } }
        showToast(R.string.operation_complete)
    }

    private fun onAddToBlacklist() {
        addSelectionToFilterList(database.phoneBlacklist, R.string.add_to_blacklist)
    }

    private fun onAddToWhitelist() {
        addSelectionToFilterList(database.phoneWhitelist, R.string.add_to_whitelist)
    }

    private fun onMarkAsIgnored() {
        getSelectedItem()?.let {
            it.state = STATE_IGNORED
            database.commit { events.add(it) }
        }
    }

    private fun onRemoveSelected() {
        val selectedEvents = listAdapter.getItemsAt(selectedItemPosition)

        database.commit { batch { events.removeAll(selectedEvents) } }

        Snackbar.make(recycler,
                        getQuantityString(R.plurals.items_removed, selectedEvents.size),
                        Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccentText))
                .setAction(R.string.undo) {
                    database.commit { batch { events.addAll(selectedEvents) } }
                }
                .show()
    }

    private fun onRemoveFromFilterList() {
        getSelectedItem()?.let { item ->
            database.commit {
                phoneBlacklist.remove(item.phone)
                phoneWhitelist.remove(item.phone)
            }
            showToast(getString(R.string.phone_removed_from_filter, item.phone))
        }
    }

    private fun addSelectionToFilterList(list: ListDataset, @StringRes titleRes: Int) {
        getSelectedItem()?.let { item ->
            EditPhoneDialogFragment().apply {
                setTitle(titleRes)
                setValue(item.phone)
                setOnOkClicked { addToFilterList(list, it) }
            }.show(this)
        }
    }

    private fun addToFilterList(list: ListDataset, phone: String?) {
        if (!phone.isNullOrEmpty()) {
            if (!database.commit { list.add(phone) }) {
                showToast(getString(R.string.item_already_exists, phone))
            }
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