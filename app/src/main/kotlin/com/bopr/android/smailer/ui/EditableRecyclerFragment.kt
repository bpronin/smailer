package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.UiUtil.getQuantityString
import com.bopr.android.smailer.util.UiUtil.showAnimated
import com.bopr.android.smailer.util.UiUtil.showToast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

abstract class EditableRecyclerFragment<I, H : ViewHolder> : RecyclerFragment<I, H>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder,
                                target: ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(holder: ViewHolder, swipeDir: Int) {
                updateSelectedItemPosition(holder)
                removeSelectedItem()
            }
        }).also {
            it.attachToRecyclerView(recycler)
        }

        view.findViewById<FloatingActionButton>(R.id.button_add).apply {
            setOnClickListener {
                editItem(NO_POSITION)
            }
            showAnimated(this, R.anim.fab_show, 0)
            show()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        refreshItems()
    }

    override fun onItemClick(item: I) {
        editSelectedItem()
    }

    protected fun editSelectedItem() {
        editItem(selectedItemPosition)
    }

    protected fun removeSelectedItem() {
        removeItems(selectedItemPosition)
    }

    protected abstract fun saveItems(items: Collection<I>)

    protected abstract fun createEditDialog(): BaseEditDialogFragment<I>

    protected open fun isValidItem(item: I): Boolean {
        return true
    }

    private fun editItem(position: Int) {
        val item = listAdapter.getItemAt(position)
        createEditDialog().apply {
            setTitle(if (item == null) R.string.add else R.string.edit)
            setValue(item)
            setOnOkClicked { newItem ->
                if (newItem != null) {
                    val exists = listAdapter.getItems().any {
                        it == newItem
                    }
                    if (exists) {
                        showToast(requireContext(),
                                getString(R.string.item_already_exists, getItemTitle(newItem)))
                    } else if (isValidItem(newItem)) {
                        listAdapter.replaceItemAt(position, newItem)
                        persistItems()
                    }
                }
            }
        }.show(requireActivity())
    }

    private fun removeItems(vararg positions: Int) {
        val savedItems = ArrayList(listAdapter.getItems())
        listAdapter.removeItemsAt(positions)
        persistItems()
        showUndoRemoved(positions.size, savedItems)
    }

    private fun showUndoRemoved(count: Int, savedItems: List<I>) {
        Snackbar.make(recycler,
                getQuantityString(R.plurals.items_removed, count),
                Snackbar.LENGTH_LONG)
                .setActionTextColor(getColor(requireContext(), R.color.colorAccentText))
                .setAction(R.string.undo) {
                    listAdapter.setItems(savedItems)
                    persistItems()
                }
                .show()
    }

    private fun persistItems() {
        saveItems(listAdapter.getItems())
    }

}