package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.bopr.android.smailer.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

abstract class RecyclerFragment<I, H : ViewHolder> : BaseFragment() {

    protected lateinit var recycler: RecyclerView
    protected lateinit var listAdapter: ListAdapter
    protected var selectedItemPosition = NO_POSITION

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)

        listAdapter = ListAdapter().apply {
            registerAdapterDataObserver(object : AdapterDataObserver() {

                override fun onChanged() {
                    updateEmptyText()
                }
            })
        }

        recycler = view.findViewById<RecyclerView>(android.R.id.list).apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = listAdapter
        }

        view.findViewById<FloatingActionButton>(R.id.button_add).visibility = View.GONE

        return view
    }

    override fun onStart() {
        super.onStart()
        refreshItems()
    }

    protected fun refreshItems() {
        listAdapter.setItems(loadItems())
    }

    protected fun updateSelectedItemPosition(holder: ViewHolder) {
        selectedItemPosition = holder.adapterPosition
    }

    protected fun getSelectedItem(): I? {
        return listAdapter.getItemAt(selectedItemPosition)
    }

    protected abstract fun loadItems(): Collection<I>

    protected open fun getItemTitle(item: I): String {
        return item.toString()
    }

    protected open fun onCreateItemContextMenu(menu: ContextMenu, item: I) {}

    protected open fun onItemClick(item: I) {}

    protected open fun onItemLongClick(item: I) {}

    protected abstract fun createViewHolder(parent: ViewGroup): H

    protected abstract fun bindViewHolder(item: I, holder: H)

    private fun updateEmptyText() {
        view?.apply {
            findViewById<View>(R.id.text_empty).visibility =
                    if (listAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    protected inner class ListAdapter : Adapter<H>() {

        private lateinit var items: MutableList<I>

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
            return createViewHolder(parent)
        }

        override fun onBindViewHolder(holder: H, position: Int) {
            val item = items[position]
            bindViewHolder(item, holder)

            holder.itemView.apply {
                setOnClickListener {
                    updateSelectedItemPosition(holder)
                    onItemClick(item)
                }
                setOnLongClickListener {
                    updateSelectedItemPosition(holder)
                    onItemLongClick(item)
                    false
                }
                setOnCreateContextMenuListener { menu, _, _ ->
                    onCreateItemContextMenu(menu, item)
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun getItems(): Collection<I> {
            return items
        }

        fun setItems(items: Collection<I>) {
            this.items = ArrayList(items)
            notifyDataSetChanged()
        }

        fun getItemAt(position: Int): I? {
            return if (position != NO_POSITION) {
                items[position]
            } else {
                null
            }
        }

        fun replaceItemAt(position: Int, item: I) {
            if (position == NO_POSITION) {
                items.add(item)
            } else {
                items[position] = item
            }
            notifyDataSetChanged()
        }

        fun removeItemsAt(positions: IntArray) {
            for (position in positions) {
                items.removeAt(position)
            }
            notifyDataSetChanged()
        }
    }

}