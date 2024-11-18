package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.ui.MailRecipientsFragment.Holder
import com.bopr.android.smailer.util.isValidEmailAddress
import com.bopr.android.smailer.util.underwivedText

/**
 * Email recipients list fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MailRecipientsFragment : EditableRecyclerFragment<String, Holder>(),
    Settings.ChangeListener {

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = Settings(requireContext(), this)
    }

    override fun onDestroy() {
        settings.dispose()

        super.onDestroy()
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        if (key == PREF_MAIL_MESSENGER_RECIPIENTS) {
            refreshItems()
        }
    }

    override fun loadItems(): Collection<String> {
        return settings.getStringList(PREF_MAIL_MESSENGER_RECIPIENTS).sorted()
    }

    override fun saveItems(items: Collection<String>) {
        settings.update {
            putStringList(PREF_MAIL_MESSENGER_RECIPIENTS, items)
        }
    }

    override fun isValidItem(item: String): Boolean {
        return item.isNotBlank()
    }

    override fun createViewHolder(parent: ViewGroup): Holder {
        val view = from(context).inflate(R.layout.list_item_recipient, parent, false)
        return Holder(view)
    }

    override fun bindViewHolder(item: String, holder: Holder) {
        holder.textView.text = if (isValidEmailAddress(item))
            item
        else
            requireContext().underwivedText(item)
    }

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditMailDialogFragment()
    }

    class Holder(view: View) : ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text)
    }
}