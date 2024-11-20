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
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.data.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.data.StringDataset
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.ui.HistoryFragment.Holder
import com.bopr.android.smailer.util.addOnItemSwipedListener
import com.bopr.android.smailer.util.phoneCallDirectionImage
import com.bopr.android.smailer.util.messageStateImage
import com.bopr.android.smailer.util.phoneCallTypeImage
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.getColorFromAttr
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.showToast
import com.google.android.material.snackbar.Snackbar

/**
 * Call history log fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HistoryFragment : RecyclerFragment<PhoneCallInfo, Holder>() {

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
        databaseListener = context.registerDatabaseListener {
            refreshItems()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterDatabaseListener(databaseListener)
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

    override fun onCreateItemContextMenu(menu: ContextMenu, item: PhoneCallInfo) {
        requireActivity().menuInflater.inflate(R.menu.menu_context_history, menu)

        if (item.processState != STATE_PENDING) {
            menu.removeItem(R.id.action_ignore)
        }

        if (!item.isSms) {
            menu.removeItem(R.id.action_add_text_to_blacklist)
            menu.removeItem(R.id.action_add_text_to_whitelist)
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

    override fun onItemClick(item: PhoneCallInfo) {
        HistoryDetailsDialogFragment(item).show(this)
    }

    override fun loadItems(): Collection<PhoneCallInfo> {
        return database.phoneCalls
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        val view = from(requireContext()).inflate(R.layout.list_item_log, parent, false)
        return Holder(view)
    }

    override fun bindViewHolder(item: PhoneCallInfo, holder: Holder) {
        holder.timeView.text = DateFormat.format(getString(R.string._time_pattern), item.startTime)
        holder.textView.text = formatSummary(item)
        holder.phoneView.text = item.phone
        holder.typeView.setImageResource(phoneCallTypeImage(item))
        holder.directionView.setImageResource(phoneCallDirectionImage(item))
        holder.stateView.setImageResource(messageStateImage(item.processState))

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
            database.phoneCalls.add(item) /* do not fire broadcast here */
        }
    }

    private fun onClearData() {
        ConfirmDialog(getString(R.string.ask_clear_history)) {
            database.commit { batch { phoneCalls.clear() } }
        }.show(this)
    }

    private fun onMarkAllAsRead() {
        database.commit { batch { phoneCalls.markAllAsRead(true) } }
        showToast(R.string.operation_complete)
    }

    private fun onMarkAsIgnored() {
        getSelectedItem()?.let {
            it.processState = STATE_IGNORED
            database.commit { phoneCalls.add(it) }
        }
    }

    private fun onRemoveSelected() {
        val selectedItems = listAdapter.getItemsAt(selectedItemPosition)

        database.commit { batch { phoneCalls.removeAll(selectedItems) } }

        Snackbar.make(
            recycler,
            getQuantityString(R.plurals.items_removed, selectedItems.size),
            Snackbar.LENGTH_LONG
        )
            .setActionTextColor(getColor(requireContext(), R.color.colorAccentText))
            .setAction(R.string.undo) {
                database.commit { batch { phoneCalls.addAll(selectedItems) } }
            }
            .show()
    }

    private fun addSelectionPhoneToFilterList(list: StringDataset, @StringRes titleRes: Int) {
        getSelectedItem()?.let { item ->
            EditPhoneDialogFragment(R.string.enter_phone_number_or_wildcard).apply {
                setTitle(titleRes)
                setValue(item.phone)
                setOnOkClicked { addToFilterList(list, it) }
            }.show(this)
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
        getSelectedItem()?.let { item ->
            EditTextDialogFragment().apply {
                setTitle(titleRes)
                setValue(item.text)
                setOnOkClicked { addToFilterList(list, it) }
            }.show(this)
        }
    }

    private fun formatSummary(info: PhoneCallInfo): CharSequence? {
        return when {
            info.isSms ->
                info.text

            info.isMissed ->
                getString(R.string.missed_call)

            info.isIncoming ->
                getString(R.string.incoming_call_of, formatDuration(info.callDuration))

            else ->
                getString(R.string.outgoing_call_of, formatDuration(info.callDuration))
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