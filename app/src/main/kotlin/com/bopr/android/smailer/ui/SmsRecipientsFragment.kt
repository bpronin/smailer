package com.bopr.android.smailer.ui

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_SMS_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.ui.SmsRecipientsFragment.Holder

/**
 * Sms recipients list fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class SmsRecipientsFragment : EditableRecyclerFragment<String, Holder>() {

    private lateinit var settingsListener: OnSharedPreferenceChangeListener

    override fun onStart() {
        super.onStart()
        settingsListener = settings.registerListener { _, key ->
            if (key == PREF_SMS_MESSENGER_RECIPIENTS) refreshItems()
        }
    }

    override fun onStop() {
        settings.unregisterListener(settingsListener)
        super.onStop()
    }

    override fun loadItems(): Collection<String> {
        return settings.getStringList(PREF_SMS_MESSENGER_RECIPIENTS).sorted()
    }

    override fun saveItems(items: Collection<String>) {
        settings.update {
            putStringList(PREF_SMS_MESSENGER_RECIPIENTS, items)
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
        holder.textView.text = item
    }

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment(R.string.enter_phone_number)
    }

    class Holder(view: View) : ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text)
    }
}