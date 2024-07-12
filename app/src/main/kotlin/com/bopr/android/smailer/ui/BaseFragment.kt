package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.R

/**
 * Base application fragment with default behaviour.
 */
open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(FragmentMenuProvider())
    }

    private fun onShowAbout() {
        AboutDialogFragment().show(this)
    }

    inner class FragmentMenuProvider : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return if (menuItem.itemId == R.id.action_about) {
                onShowAbout()
                true
            } else
                false
        }
    }
}