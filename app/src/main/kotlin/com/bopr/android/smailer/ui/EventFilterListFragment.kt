package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.R
import com.bopr.android.smailer.StringDataset
import com.bopr.android.smailer.ui.EventFilterListFragment.Holder

/**
 * Base for black/whitelist fragments.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class EventFilterListFragment(private val listName: String) : EditableRecyclerFragment<String, Holder>() {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver
    private lateinit var list: StringDataset

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext()

        database = Database(context)
        list = database.eventFilterList.getValue(listName)
        databaseListener = context.registerDatabaseListener { tables ->
            if (tables.contains(listName)) refreshItems()
        }
    }

    override fun onDestroy() {
        requireContext().unregisterDatabaseListener(databaseListener)
        database.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear ->
                onClearData()
            R.id.action_sort ->
                onSort()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateItemContextMenu(menu: ContextMenu, item: String) {
        requireActivity().menuInflater.inflate(R.menu.menu_context_filters, menu)
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

    override fun loadItems(): Collection<String> {
        return list
    }

    override fun saveItems(items: Collection<String>) {
        database.commit { batch { list.replaceAll(items) } }
    }

    override fun isValidItem(item: String): Boolean {
        return !item.isBlank()
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.list_item_filter, parent, false))
    }

    override fun bindViewHolder(item: String, holder: Holder) {
        holder.textView.text = getItemTitle(item)
    }

    private fun onClearData() {
        ConfirmDialog(getString(R.string.ask_clear_list)) {
            saveItems(emptyList())
        }.show(this)
    }

    private fun onSort() {
        saveItems(loadItems().sorted())
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text)
    }

}