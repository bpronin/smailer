package com.bopr.android.smailer.ui


import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_BLACKLIST


class PhoneBlacklistFilterActivityTest : BaseFilterActivityTest(
        PhoneBlacklistFilterActivity::class, TABLE_PHONE_BLACKLIST)
