package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.TagFormatter
import com.bopr.android.smailer.util.TextUtil.isNullOrEmpty
import com.bopr.android.smailer.util.UiUtil.showToast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Base for black/whitelist fragments.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class FilterListFragment : BaseFragment() {

    private lateinit var listAdapter: ListAdapter
    private lateinit var listView: RecyclerView
    private var selectedListPosition = RecyclerView.NO_POSITION

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_filter_list, container, false)

        listView = view.findViewById<RecyclerView>(android.R.id.list).apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        ItemTouchHelper(object : SimpleCallback(0, LEFT or RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, swipeDir: Int) {
                selectedListPosition = holder.adapterPosition
                removeSelectedItem()
            }

        }).also { it.attachToRecyclerView(listView) }

        view.findViewById<FloatingActionButton>(R.id.button_add).apply {
            setOnClickListener { addItem() }
        }

        loadItems()

        return view
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_item -> {
                editSelectedItem()
                true
            }
            R.id.action_remove_item -> {
                removeSelectedItem()
                true
            }
            else ->
                super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_log_clear) {
            clearData()
        }
        return super.onOptionsItemSelected(item)
    }

    protected abstract fun getItemsList(filter: PhoneEventFilter): Set<String>

    protected abstract fun setItemsList(filter: PhoneEventFilter, list: List<String>)

    protected abstract fun createEditItemDialog(text: String?): EditFilterListItemDialogFragment

    protected abstract fun getItemText(value: String?): String?

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

    fun loadItems() {
        listAdapter = ListAdapter().also {
            it.registerAdapterDataObserver(object : AdapterDataObserver() {

                override fun onChanged() {
                    updateEmptyText()
                }
            })
        }

        listView.adapter = listAdapter

        val items = getItemsList(settings.getFilter())
                .toList()
                .sorted()
                .map { Item(it) }

        listAdapter.setItems(items)
    }

    private fun clearData() {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.ask_clear_list)
                .setPositiveButton(R.string.clear) { _, _ ->
                    listAdapter.setItems(emptyList())
                    persistItems()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
    }

    private fun persistItems() {
        val items: MutableList<String> = ArrayList()
        for (item in listAdapter.getItems()) {
            items.add(item.value)
        }
        val filter = settings.getFilter()
        setItemsList(filter, items)
        settings.edit().putFilter(filter).apply()
    }

    private fun isItemExists(text: String): Boolean {
        for (item in listAdapter.getItems()) {
            if (text == item.value) {
                return true
            }
        }
        return false
    }

    private fun addItem() {
        editItem(null)
    }

    private fun editSelectedItem() {
        if (selectedListPosition != RecyclerView.NO_POSITION) {
            val item = listAdapter.getItem(selectedListPosition)
            editItem(item)
        }
    }

    private fun editItem(item: Item?) {
        createEditItemDialog(item?.value).apply {
            setOnOkClicked { value ->
                if (value != null) {
                    if (isItemExists(value) && (item == null || item.value != value)) {
                        showToast(requireContext(), TagFormatter(requireContext())
                                .pattern(R.string.item_already_exists)
                                .put("item", getItemText(value))
                                .format())
                    } else if (!isNullOrEmpty(getItemText(value))) {
                        listAdapter.replaceItem(item, Item(value))
                        persistItems()
                    }
                }
            }
        }.showDialog(requireActivity())
    }

    private fun removeSelectedItem() {
        if (selectedListPosition != RecyclerView.NO_POSITION) {
            val savedItems: List<Item> = ArrayList(listAdapter.getItems())
            val removedItems = listAdapter.removeItems(intArrayOf(selectedListPosition))
            persistItems()
            showUndoAction(removedItems, savedItems)
        }
    }

    private fun undoRemoveItem(lastItems: List<Item>) {
        listAdapter.setItems(lastItems)
        persistItems()
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
                .setAction(R.string.undo) { undoRemoveItem(lastItems) }
                .show()
    }

    private inner class Item(val value: String)

    private inner class ListAdapter : RecyclerView.Adapter<ItemViewHolder>() {

        private val items: MutableList<Item> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val inflater = LayoutInflater.from(context)
            return ItemViewHolder(inflater.inflate(R.layout.list_item_filter, parent, false))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = getItem(position)
            holder.textView.text = item?.let { getItemText(item.value) }
            holder.itemView.setOnClickListener {
                selectedListPosition = holder.adapterPosition
                editSelectedItem()
            }
            holder.itemView.setOnLongClickListener {
                selectedListPosition = holder.adapterPosition
                false
            }
            holder.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                requireActivity().menuInflater.inflate(R.menu.menu_context_filters, menu)
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun getItem(position: Int): Item? {
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
}