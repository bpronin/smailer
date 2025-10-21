package com.bopr.android.smailer.ui


import android.content.BroadcastReceiver
import android.os.Bundle
import android.text.format.DateFormat
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.LayoutInflater.from
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.R
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.data.StringDataset
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.ui.HistoryFragment.Holder
import com.bopr.android.smailer.util.addOnItemSwipedListener
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.getColorFromAttr
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.messageStateImage
import com.bopr.android.smailer.util.phoneCallDirectionImage
import com.bopr.android.smailer.util.phoneCallTypeImage
import com.bopr.android.smailer.util.showToast
import com.google.android.material.snackbar.Snackbar

/**
 * Call history log fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class HistoryFragment : RecyclerFragment<Event, Holder>() {

    private lateinit var databaseListener: BroadcastReceiver
    private var defaultItemTextColor: Int = 0
    private var unreadItemTextColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        emptyTextRes = R.string.history_is_empty

        val context = requireContext()

        defaultItemTextColor = context.getColorFromAttr(android.R.attr.textColorSecondary)
        unreadItemTextColor = context.getColorFromAttr(android.R.attr.textColorPrimary)

        databaseListener = database.registerListener {
            refreshItems()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        database.unregisterListener(databaseListener)
        database.close()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        recycler.addOnItemSwipedListener {
            updateSelectedItemPosition(it)
            onRemoveSelected()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(FragmentMenuProvider())
    }

    override fun onCreateItemContextMenu(menu: ContextMenu, item: Event) {
        requireActivity().menuInflater.inflate(R.menu.menu_context_history, menu)

        if (item.processState != STATE_PENDING) {
            menu.removeItem(R.id.action_ignore)
        }

        (item.payload as? PhoneCallData)?.let {
            if (!it.isSms) {
                menu.removeItem(R.id.action_add_text_to_blacklist)
                menu.removeItem(R.id.action_add_text_to_whitelist)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_remove_item -> {
                onRemoveSelected()
                true
            }

            R.id.action_add_phone_to_blacklist -> {
                addSelectionPhoneToFilterList(database.phoneBlacklist, R.string.add_to_blacklist)
                true
            }

            R.id.action_add_phone_to_whitelist -> {
                addSelectionPhoneToFilterList(database.phoneWhitelist, R.string.add_to_whitelist)
                true
            }

            R.id.action_add_text_to_blacklist -> {
                addSelectionTextToFilterList(database.textBlacklist, R.string.add_to_blacklist)
                true
            }

            R.id.action_add_text_to_whitelist -> {
                addSelectionTextToFilterList(database.textWhitelist, R.string.add_to_whitelist)
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

    override fun onItemClick(item: Event) {
        HistoryDetailsDialogFragment(item).show(this)
    }

    override fun loadItems(): Collection<Event> {
        return database.events
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        val view = from(requireContext()).inflate(R.layout.list_item_log, parent, false)
        return Holder(view)
    }

    override fun bindViewHolder(item: Event, holder: Holder) {
        holder.apply {
            stateView.setImageResource(messageStateImage(item.processState))
            textView.text = formatSummary(item)

            if (!item.isRead) {
                phoneView.setTextColor(unreadItemTextColor)
                textView.setTextColor(unreadItemTextColor)
                timeView.setTextColor(unreadItemTextColor)
            } else {
                phoneView.setTextColor(defaultItemTextColor)
                textView.setTextColor(defaultItemTextColor)
                timeView.setTextColor(defaultItemTextColor)
            }

            if (!item.isRead) {
                item.isRead = true
                database.events.add(item) /* do not commit to not fire broadcast here */
            }

            (item.payload as? PhoneCallData)?.let {
                timeView.text = DateFormat.format(
                    getString(R.string._time_pattern),
                    it.startTime
                )
                phoneView.text = it.phone
                typeView.setImageResource(phoneCallTypeImage(it))
                directionView.setImageResource(phoneCallDirectionImage(it))
            }
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

    private fun onMarkAsIgnored() {
        getSelectedItem()?.let {
            it.processState = STATE_IGNORED
            database.commit { events.add(it) }
        }
    }

    private fun onRemoveSelected() {
        val selectedItems = listAdapter.getItemsAt(selectedItemPosition)

        database.commit { batch { events.removeAll(selectedItems) } }

        Snackbar.make(
            recycler,
            getQuantityString(R.plurals.items_removed, selectedItems.size),
            Snackbar.LENGTH_LONG
        )
            .setActionTextColor(getColor(requireContext(), R.color.colorAccentText))
            .setAction(R.string.undo) {
                database.commit { batch { events.addAll(selectedItems) } }
            }
            .show()
    }

    private fun addSelectionPhoneToFilterList(list: StringDataset, @StringRes titleRes: Int) {
        getSelectedItem()?.let { event ->
            (event.payload as? PhoneCallData)?.let {
                EditPhoneDialogFragment(R.string.enter_phone_number_or_wildcard).apply {
                    setTitle(titleRes)
                    setValue(it.phone)
                    setOnOkClicked { action -> addToFilterList(list, action) }
                }.show(this)
            }
        }
    }

    private fun addToFilterList(list: StringDataset, value: String?) {
        if (!value.isNullOrEmpty()) {
            if (!database.commit { list.add(value) }) {
                showToast(getString(R.string.item_already_exists, value))
            }
        }
    }

    private fun addSelectionTextToFilterList(list: StringDataset, @StringRes titleRes: Int) {
        getSelectedItem()?.let { event ->
            (event.payload as? PhoneCallData)?.let {
                EditTextDialogFragment().apply {
                    setTitle(titleRes)
                    setValue(it.text)
                    setOnOkClicked { action -> addToFilterList(list, action) }
                }.show(this)
            }
        }
    }

    private fun formatSummary(event: Event): CharSequence? {
        return (event.payload as? PhoneCallData)?.let {
            formatPhoneCallSummary(it)
        }
    }

    private fun formatPhoneCallSummary(info: PhoneCallData): CharSequence? {
        return when {
            info.isSms -> info.text

            info.isMissed -> getString(R.string.missed_call)

            info.isIncoming -> getString(
                R.string.incoming_call_of, formatDuration(info.callDuration)
            )

            else -> getString(R.string.outgoing_call_of, formatDuration(info.callDuration))
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

    inner class FragmentMenuProvider : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_history, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_clear -> {
                    onClearData()
                    true
                }

                R.id.action_mark_all_as_read -> {
                    onMarkAllAsRead()
                    true
                }

                else -> false
            }

        }
    }

}