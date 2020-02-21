package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.ui.RecipientsFragment.Holder
import com.bopr.android.smailer.util.isValidEmailAddress
import com.bopr.android.smailer.util.underwivedText

/**
 * Recipients list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RecipientsFragment : EditableRecyclerFragment<String, Holder>(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings.registerChangeListener(this)
    }

    override fun onDestroy() {
        settings.unregisterChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == PREF_RECIPIENTS_ADDRESS) {
            refreshItems()
        }
    }

    override fun loadItems(): Collection<String> {
        return settings.getCommaList(PREF_RECIPIENTS_ADDRESS).sorted()
    }

    override fun saveItems(items: Collection<String>) {
        settings.edit()
                .putCommaSet(PREF_RECIPIENTS_ADDRESS, items)
                .apply()
    }

    override fun isValidItem(item: String): Boolean {
        return !item.isBlank()
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_recipient, parent, false)
        return Holder(view)
    }

    override fun bindViewHolder(item: String, holder: Holder) {
        holder.textView.text =
                if (isValidEmailAddress(item))
                    item
                else
                    underwivedText(requireContext(), item)
    }

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditEmailDialogFragment()
    }

    inner class Holder(view: View) : ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.text)

    }

}