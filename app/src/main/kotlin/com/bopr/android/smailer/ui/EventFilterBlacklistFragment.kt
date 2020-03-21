package com.bopr.android.smailer.ui

import com.bopr.android.smailer.Database.Companion.TABLE_PHONE_BLACKLIST

/**
 * Phone number blacklist fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventFilterBlacklistFragment : EventFilterPhoneListFragment(TABLE_PHONE_BLACKLIST)