package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.UiUtil.showToast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

abstract class RecyclerFragment<I, H : ViewHolder> : BaseFragment() {

    private lateinit var listAdapter: ListAdapter
    private lateinit var listView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)

        listView = view.findViewById<RecyclerView>(android.R.id.list).apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder,
                                target: ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(holder: ViewHolder, swipeDir: Int) {
                removeItemsAt(holder.adapterPosition)
            }
        }).also {
            it.attachToRecyclerView(listView)
        }

        view.findViewById<FloatingActionButton>(R.id.button_add).apply {
            setOnClickListener { addItem() }
        }

        reloadItems()

        return view
    }

    protected abstract fun loadItems(): List<I>

    protected abstract fun saveItems(items: List<I>)

    protected abstract fun getItemTitle(item: I): String

    protected abstract fun isSameItem(item: I, other: I): Boolean

    protected abstract fun isValidItem(item: I): Boolean

    protected abstract fun createViewHolder(parent: ViewGroup): H

    protected abstract fun bindViewHolder(item: I, holder: H)

    protected abstract fun createEditDialog(): BaseEditDialogFragment<I>

    protected fun reloadItems() {
        listAdapter = ListAdapter().apply {
            registerAdapterDataObserver(object : AdapterDataObserver() {

                override fun onChanged() {
                    updateEmptyText()
                }
            })

            items = loadItems()
        }
        listView.adapter = listAdapter
    }

    private fun saveItems() {
        saveItems(listAdapter.items)
    }

    private fun updateEmptyText() {
        view?.apply {
            findViewById<View>(R.id.text_empty).visibility =
                    if (listAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun addItem() {
        showItemEditor(null)
    }

    private fun editItem(item: I) {
        showItemEditor(item)
    }

    private fun removeItemsAt(vararg positions: Int) {
        val (removedItems, savedItems) = listAdapter.removeItemsAt(positions)
        saveItems()
        showUndoAction(removedItems, savedItems)
    }

    private fun undoRemove(savedItems: List<I>) {
        listAdapter.items = savedItems
        saveItems()
    }

    private fun isItemExists(item: I): Boolean {
        return listAdapter.items.any { isSameItem(item, it) }
    }

    private fun showItemEditor(item: I?) {
        createEditDialog().apply {
            setTitle(if (item == null) R.string.add else R.string.edit)
            setValue(item)
            setOnOkClicked { value ->
                if (value != null) {
                    if (isItemExists(value)) {
                        showToast(requireContext(),
                                getString(R.string.item_already_exists).format(getItemTitle(value)))
                    } else if (isValidItem(value)) {
                        listAdapter.replaceItem(item, value)
                        saveItems()
                    }
                }
            }
        }.showDialog(requireActivity())
    }

    private fun showUndoAction(removedItems: List<I>, savedItems: List<I>) {
        val title = if (removedItems.size == 1) {
            getString(R.string.item_removed)
        } else {
            getString(R.string.items_removed).format(removedItems.size)
        }

        Snackbar.make(listView, title, Snackbar.LENGTH_LONG)
                .setActionTextColor(getColor(requireContext(), R.color.colorAccentText))
                .setAction(R.string.undo) {
                    undoRemove(savedItems)
                }
                .show()
    }

    private inner class ListAdapter : Adapter<H>() {

        private lateinit var _items: MutableList<I>

        var items: List<I>
            get() {
                return _items
            }
            set(value) {
                _items = ArrayList(value)
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
            return createViewHolder(parent)
        }

        override fun onBindViewHolder(holder: H, position: Int) {
            val item = _items[position]
            bindViewHolder(item, holder)
            holder.itemView.setOnClickListener {
                editItem(item)
            }
        }

        override fun getItemCount(): Int {
            return _items.size
        }

        fun removeItemsAt(positions: IntArray): Pair<List<I>, List<I>> {
            val savedItems: List<I> = ArrayList(_items)
            val removedItems: MutableList<I> = ArrayList()

            for (position in positions) {
                val item = _items[position]
                removedItems.add(item)
                _items.remove(item)
            }

            notifyDataSetChanged()
            return Pair(removedItems, savedItems)
        }

        fun replaceItem(oldItem: I?, newItem: I) {
            val position = _items.indexOf(oldItem)

            if (position == -1) {
                _items.add(newItem)
            } else {
                _items[position] = newItem
            }

            notifyDataSetChanged()
        }
    }
}