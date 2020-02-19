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
import com.bopr.android.smailer.util.Dialogs.showConfirmationDialog
import com.bopr.android.smailer.util.UiUtil.showToast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

abstract class RecyclerFragment<I, H : ViewHolder> : BaseFragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var listAdapter: ListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)

        listAdapter = ListAdapter().apply {
            registerAdapterDataObserver(object : AdapterDataObserver() {

                override fun onChanged() {
                    updateEmptyText()
                }
            })
        }

        recycler = view.findViewById<RecyclerView>(android.R.id.list).apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = listAdapter
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder,
                                target: ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(holder: ViewHolder, swipeDir: Int) {
                removeItems(holder.adapterPosition)
            }
        }).also {
            it.attachToRecyclerView(recycler)
        }

        view.findViewById<FloatingActionButton>(R.id.button_add).setOnClickListener {
            editItem(null, NO_POSITION)
        }

        reloadItems()

        return view
    }

    protected fun reloadItems() {
        listAdapter.setItems(getItems())
    }

    protected fun clearItems() {
        showConfirmationDialog(requireContext(),
                messageRes = R.string.ask_clear_list,
                buttonTextRes = R.string.clear) {
            listAdapter.setItems(listOf())
            persistItems()
        }
    }

    protected abstract fun getItems(): Collection<I>

    protected abstract fun putItems(items: Collection<I>)

    protected abstract fun getItemTitle(item: I): String

    protected abstract fun isSameItem(item: I, other: I): Boolean

    protected abstract fun isValidItem(item: I): Boolean

    protected abstract fun createViewHolder(parent: ViewGroup): H

    protected abstract fun bindViewHolder(item: I, holder: H)

    protected abstract fun createEditDialog(): BaseEditDialogFragment<I>

    private fun persistItems() {
        putItems(listAdapter.getItems())
    }

    private fun removeItems(vararg positions: Int) {
        val savedItems = ArrayList(listAdapter.getItems())
        listAdapter.removeItems(positions)
        persistItems()
        showUndoRemoved(positions.size, savedItems)
    }

    private fun showUndoRemoved(removedCount: Int, savedItems: List<I>) {
        val title = if (removedCount == 1) {
            getString(R.string.item_removed)
        } else {
            getString(R.string.items_removed).format(removedCount)
        }

        Snackbar.make(recycler, title, Snackbar.LENGTH_LONG)
                .setActionTextColor(getColor(requireContext(), R.color.colorAccentText))
                .setAction(R.string.undo) {
                    listAdapter.setItems(savedItems)
                    persistItems()
                }
                .show()
    }

    private fun editItem(item: I?, position: Int) {
        createEditDialog().apply {
            setTitle(if (position == NO_POSITION) R.string.add else R.string.edit)
            setValue(item)
            setOnOkClicked { newItem ->
                if (newItem != null) {
                    val exists = listAdapter.getItems().any {
                        isSameItem(it, newItem)
                    }
                    if (exists) {
                        showToast(requireContext(),
                                getString(R.string.item_already_exists).format(getItemTitle(newItem)))
                    } else if (isValidItem(newItem)) {
                        listAdapter.replaceItem(position, newItem)
                        persistItems()
                    }
                }
            }
        }.showDialog(requireActivity())
    }

    private fun updateEmptyText() {
        view?.apply {
            findViewById<View>(R.id.text_empty).visibility =
                    if (listAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private inner class ListAdapter : Adapter<H>() {

        private lateinit var items: MutableList<I>

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
            return createViewHolder(parent)
        }

        override fun onBindViewHolder(holder: H, position: Int) {
            val item = items[position]
            bindViewHolder(item, holder)
            holder.itemView.setOnClickListener {
                editItem(item, position)
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun getItems(): List<I> {
            return items
        }

        fun setItems(items: Collection<I>) {
            this.items = ArrayList(items)
            notifyDataSetChanged()
        }

        fun replaceItem(position: Int, item: I) {
            if (position == NO_POSITION) {
                items.add(item)
            } else {
                items[position] = item
            }
            notifyDataSetChanged()
        }

        fun removeItems(positions: IntArray) {
            for (position in positions) {
                items.removeAt(position)
            }
            notifyDataSetChanged()
        }
    }
}