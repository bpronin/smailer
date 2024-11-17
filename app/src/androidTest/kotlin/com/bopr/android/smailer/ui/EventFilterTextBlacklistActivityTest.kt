package com.bopr.android.smailer.ui


import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_BLACKLIST


class EventFilterTextBlacklistActivityTest : BaseEventFilterTextListActivityTest(
        EventFilterTextBlacklistActivity::class, TABLE_TEXT_BLACKLIST)
