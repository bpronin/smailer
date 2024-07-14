package com.bopr.android.smailer.ui

import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_WHITELIST


/**
 * Phone number whitelist fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventFilterWhitelistFragment : EventFilterPhoneListFragment(TABLE_PHONE_WHITELIST)