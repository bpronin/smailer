package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.addOnItemSwipedListener
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.showAnimated
import com.bopr.android.smailer.util.showToast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * Base fragment with editable recycler view.
 * 
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
abstract class EditableRecyclerFragment<I, H : ViewHolder> : RecyclerFragment<I, H>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!

        recycler.addOnItemSwipedListener {
            updateSelectedItemPosition(it)
            removeSelectedItem()
        }

        view.findViewById<FloatingActionButton>(R.id.button_add).apply {
            setOnClickListener {
                editItem(NO_POSITION)
            }
            showAnimated(R.anim.fab_show, 300)
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

    protected open fun isSameItem(item1: I, item2: I): Boolean {
        return item1 == item2
    }

    private fun editItem(position: Int) {
        val item = listAdapter.getItemAt(position)
        createEditDialog().apply {
            setTitle(if (item == null) R.string.add else R.string.edit)
            setValue(item)
            setOnOkClicked { replaceItem(it, position) }
        }.show(this)
    }

    private fun replaceItem(item: I?, position: Int) {
        if (item != null) {
            listAdapter.run {
                if (getItems().any { isSameItem(it, item) }) {
                    showToast(getString(R.string.item_already_exists, getItemTitle(item)))
                } else if (isValidItem(item)) {
                    replaceItemAt(position, item)
                    persistItems()
                }
            }
        }
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