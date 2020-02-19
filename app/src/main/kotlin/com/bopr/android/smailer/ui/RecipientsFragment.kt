package com.bopr.android.smailer.ui

import android.content.Context
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
import com.bopr.android.smailer.ui.RecipientsFragment.Item
import com.bopr.android.smailer.util.TextUtil.isValidEmailAddress
import com.bopr.android.smailer.util.UiUtil.underwivedText

/**
 * Recipients list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RecipientsFragment : RecyclerFragment<Item, Holder>() {

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

    override fun getItems(): Collection<Item> {
        return settings.getCommaList(PREF_RECIPIENTS_ADDRESS).sorted().map { Item(it) }
    }

    override fun putItems(items: Collection<Item>) {
        settings.edit()
                .putCommaSet(PREF_RECIPIENTS_ADDRESS, items.map { it.address })
                .apply()
    }

    override fun getItemTitle(item: Item): String {
        return item.address
    }

    override fun isValidItem(item: Item): Boolean {
        return !item.address.isBlank()
    }

    override fun isSameItem(item: Item, other: Item): Boolean {
        return item.address == other.address
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.list_item_recipient, parent, false))
    }

    override fun bindViewHolder(item: Item, holder: Holder) {
        holder.textView.text =
                if (isValidEmailAddress(item.address))
                    item.address
                else
                    underwivedText(requireContext(), item.address)
    }

    override fun createEditDialog(): BaseEditDialogFragment<Item> {
        return EditRecipientDialogFragment()
    }

    class Item(val address: String)

    inner class Holder(view: View) : ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text)
    }

    private inner class SettingsListener(context: Context) : BaseSettingsListener(context) {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == PREF_RECIPIENTS_ADDRESS) {
                reloadItems()
            }
            super.onSharedPreferenceChanged(sharedPreferences, key)
        }
    }

}