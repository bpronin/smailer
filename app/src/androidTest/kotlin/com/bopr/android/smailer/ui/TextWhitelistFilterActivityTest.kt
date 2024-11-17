package com.bopr.android.smailer.ui


import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_WHITELIST


class TextWhitelistFilterActivityTest : BaseFilterTextActivityTest(
        TextWhitelistFilterActivity::class, TABLE_TEXT_WHITELIST)
