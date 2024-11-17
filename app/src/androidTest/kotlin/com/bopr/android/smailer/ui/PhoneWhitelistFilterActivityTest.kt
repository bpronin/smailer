package com.bopr.android.smailer.ui


import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_WHITELIST


class PhoneWhitelistFilterActivityTest : BaseFilterActivityTest(
        PhoneWhitelistFilterActivity::class, TABLE_PHONE_WHITELIST)
