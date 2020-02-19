package com.bopr.android.smailer.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.util.TagFormatter
import com.bopr.android.smailer.util.TextUtil.commaJoin
import com.bopr.android.smailer.util.TextUtil.isValidEmailAddress
import com.bopr.android.smailer.util.UiUtil.showToast
import com.bopr.android.smailer.util.UiUtil.underwivedText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Recipients list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
//todo: generalize with FilterListFragment
class RecipientsFragment : BaseFragment() {

    private lateinit var listAdapter: ListAdapter
    private lateinit var listView: RecyclerView
    private lateinit var settingsListener: SettingsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsListener = SettingsListener(requireContext())
        settings.registerOnSharedPreferenceChangeListener(settingsListener)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recipients, container, false)

        listView = view.findViewById<RecyclerView>(android.R.id.list).apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        ItemTouchHelper(object : SimpleCallback(0, LEFT or RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, swipeDir: Int) {
                removeItems(intArrayOf(holder.adapterPosition))
            }
        }).also {
            it.attachToRecyclerView(listView)
        }

        view.findViewById<FloatingActionButton>(R.id.button_add).apply {
            setOnClickListener { addItem() }
        }

        loadItems()

        return view
    }

    private fun updateEmptyText() {
        val view = view
        if (view != null && listView.adapter != null) {
            val text = view.findViewById<TextView>(R.id.text_empty)
            if (listView.adapter!!.itemCount == 0) {
                text.visibility = View.VISIBLE
            } else {
                text.visibility = View.GONE
            }
        }
    }

    private fun getItemsList(): List<Item> {
        val addresses = settings.getCommaSet(PREF_RECIPIENTS_ADDRESS)
        return addresses.toList().sorted().map { Item(it) }
    }

    private fun loadItems() {
        listAdapter = ListAdapter().also {
            it.registerAdapterDataObserver(object : AdapterDataObserver() {

                override fun onChanged() {
                    updateEmptyText()
                }
            })

            listView.adapter = it
        }

        listAdapter.setItems(getItemsList())
    }

    private fun persistItems() {
        val addresses: MutableList<String?> = ArrayList()
        for (item in listAdapter.getItems()) {
            addresses.add(item.address)
        }
        settings.edit().putString(PREF_RECIPIENTS_ADDRESS, commaJoin(addresses)).apply()
    }

    private fun addItem() {
        showItemEditor(null)
    }

    private fun editItem(item: Item?) {
        item?.let { showItemEditor(it) }
    }

    private fun removeItems(positions: IntArray) {
        val savedItems: List<Item> = ArrayList(listAdapter.getItems())
        val removedItems = listAdapter.removeItems(positions)
        persistItems()
        showUndoAction(removedItems, savedItems)
    }

    private fun undoRemove(lastItems: List<Item>) {
        listAdapter.setItems(lastItems)
        persistItems()
    }

    private fun isItemExists(address: String): Boolean {
        for (item in listAdapter.getItems()) {
            if (item.address == address) {
                return true
            }
        }
        return false
    }

    private fun showItemEditor(item: Item?) {
        EditEmailDialogFragment().apply {
            setTitle(if (item == null) R.string.add else R.string.edit)
            setInitialValue(item?.address)
            setOnOkClicked { value ->
                if (value != null) {
                    if (isItemExists(value) && (item == null || item.address != value)) {
                        showToast(requireContext(), TagFormatter(requireContext())
                                .pattern(R.string.recipient_already_exists)
                                .put("name", value)
                                .format())
                    } else if (!value.isNullOrBlank()) {
                        listAdapter.replaceItem(item, Item(value))
                        persistItems()
                    }
                }
            }
        }.showDialog(requireActivity())
    }

    private fun showUndoAction(removedItems: List<Item?>, lastItems: List<Item>) {
        val title = if (removedItems.size == 1) {
            getString(R.string.item_removed)
        } else {
            TagFormatter(requireContext())
                    .pattern(R.string.items_removed)
                    .put("count", removedItems.size.toString())
                    .format()
        }

        Snackbar.make(listView, title, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .setAction(R.string.undo) { undoRemove(lastItems) }
                .show()
    }

    private inner class Item(val address: String)

    private inner class ListAdapter : RecyclerView.Adapter<ItemViewHolder>() {

        private val items: MutableList<Item> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val inflater = LayoutInflater.from(context)
            return ItemViewHolder(inflater.inflate(R.layout.list_item_recipient, parent, false))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = getItem(position)
            val address = item?.address
            holder.textView.text = if (isValidEmailAddress(address)) address else underwivedText(requireContext(), address)
            holder.itemView.setOnClickListener { editItem(item) }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return items.size
        }

        private fun getItem(position: Int): Item? {
            return if (position != -1) items[position] else null
        }

        fun getItems(): List<Item> {
            return Collections.unmodifiableList(items)
        }

        fun setItems(items: List<Item>) {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
        }

        fun removeItems(positions: IntArray): List<Item?> {
            val removedItems: MutableList<Item?> = ArrayList()
            for (position in positions) {
                val item = getItem(position)
                removedItems.add(item)
                items.remove(item)
            }
            notifyDataSetChanged()
            return removedItems
        }

        fun replaceItem(oldItem: Item?, newItem: Item) {
            val position = items.indexOf(oldItem)
            if (position < 0) {
                items.add(newItem)
            } else {
                items[position] = newItem
            }
            notifyDataSetChanged()
        }
    }

    private inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text)
    }

    private inner class SettingsListener(context: Context) : BaseSettingsListener(context) {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == PREF_RECIPIENTS_ADDRESS) {
                loadItems()
            }
            super.onSharedPreferenceChanged(sharedPreferences, key)
        }
    }
}