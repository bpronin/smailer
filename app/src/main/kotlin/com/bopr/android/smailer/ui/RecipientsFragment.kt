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
import com.bopr.android.smailer.util.TextUtil.isValidEmailAddress
import com.bopr.android.smailer.util.UiUtil.underwivedText

/**
 * Recipients list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RecipientsFragment : EditableRecyclerFragment<String, Holder>() {

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

    override fun getItems(): Collection<String> {
        return settings.getCommaList(PREF_RECIPIENTS_ADDRESS).sorted()
    }

    override fun putItems(items: Collection<String>) {
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

    private inner class SettingsListener(context: Context) : BaseSettingsListener(context) {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == PREF_RECIPIENTS_ADDRESS) {
                reloadItems()
            }
            super.onSharedPreferenceChanged(sharedPreferences, key)
        }
    }

}