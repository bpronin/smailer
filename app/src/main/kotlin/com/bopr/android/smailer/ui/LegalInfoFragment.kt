package com.bopr.android.smailer.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.ListFragment
import com.bopr.android.smailer.R
import androidx.core.net.toUri

/**
 * Legal info fragment. Displays list of used open source libs.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class LegalInfoFragment : ListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View? {
        val data: MutableList<Item> = mutableListOf()
        for (line in resources.getStringArray(R.array.open_source)) {
            val s = line.split("|")
            data.add(Item(s[0], s[1].toUri()))
        }

        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, data)

        return inflater.inflate(android.R.layout.list_content, container, false)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val item = requireListAdapter().getItem(position) as Item
        startActivity(Intent(Intent.ACTION_VIEW, item.url))
    }

    private inner class Item(val name: String, val url: Uri) {

        override fun toString(): String {
            return name
        }

    }
}