package com.bopr.android.smailer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.FilterListFragment.Holder

/**
 * Base for black/whitelist fragments.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class FilterListFragment : RecyclerFragment<String, Holder>() {

    override fun getItems(): Collection<String> {
        return getItemsList(settings.getFilter()).sorted()
    }

    override fun putItems(items: Collection<String>) {
        val filter = settings.getFilter()
        setItemsList(filter, items)
        settings.edit()
                .putFilter(filter)
                .apply()
    }

    override fun isSameItem(item: String, other: String): Boolean {
        return item == other
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

    protected abstract fun getItemsList(filter: PhoneEventFilter): Collection<String>

    protected abstract fun setItemsList(filter: PhoneEventFilter, list: Collection<String>)

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {

        val textView: TextView = view.findViewById(R.id.text)
    }
}