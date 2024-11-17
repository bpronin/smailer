package com.bopr.android.smailer.ui


import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_WHITELIST


class EventFilterPhoneWhitelistActivityTest : BaseEventFilterListActivityTest(
        EventFilterPhoneWhitelistActivity::class, TABLE_PHONE_WHITELIST)
