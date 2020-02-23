package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.CallFilterListFragment.Holder
import com.bopr.android.smailer.util.Dialogs.showConfirmationDialog

/**
 * Base for black/whitelist fragments.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class CallFilterListFragment(private val settingName: String) : EditableRecyclerFragment<String, Holder>(),
        OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings.registerChangeListener(this)
    }

    override fun onDestroy() {
        settings.unregisterChangeListener(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_log_clear) {
            onClearData()
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == settingName) {
            refreshItems()
        }
    }

    override fun loadItems(): Collection<String> {
        return getItemsList(settings.callFilter).sorted()
    }

    override fun saveItems(items: Collection<String>) {
        val filter = settings.callFilter

        getItemsList(filter).apply {
            clear()
            addAll(items)
        }

        settings.edit()
                .putFilter(filter)
                .apply()
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

    private fun getItemsList(filter: PhoneEventFilter): MutableCollection<String> {
        return filter.getList(settingName)
    }

    private fun onClearData() {
        showConfirmationDialog(requireContext(), messageRes = R.string.ask_clear_list,
                buttonTextRes = R.string.clear) {
            saveItems(listOf())
        }
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text)
    }
}