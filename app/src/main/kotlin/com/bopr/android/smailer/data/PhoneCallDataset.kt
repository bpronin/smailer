package com.bopr.android.smailer.data

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.data.Database.Companion.COLUMN_END_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_IS_INCOMING
import com.bopr.android.smailer.data.Database.Companion.COLUMN_IS_MISSED
import com.bopr.android.smailer.data.Database.Companion.COLUMN_PHONE
import com.bopr.android.smailer.data.Database.Companion.COLUMN_START_TIME
import com.bopr.android.smailer.data.Database.Companion.COLUMN_TARGET
import com.bopr.android.smailer.data.Database.Companion.COLUMN_TEXT
import com.bopr.android.smailer.data.Database.Companion.COLUMN_TIMESTAMP
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_CALLS
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.util.stringArrayOf

class PhoneCallDataset(helper: SQLiteOpenHelper) :
    ReadonlyDataset<PhoneCallInfo>(TABLE_PHONE_CALLS, helper) {

    override val keyColumns = stringArrayOf(COLUMN_TIMESTAMP, COLUMN_TARGET)

    override fun query() = read {
        queryRecords(tableName, order = "$COLUMN_START_TIME DESC")
    }

    override fun get(cursor: Cursor) = cursor.run {
        PhoneCallInfo(
            phone = getString(COLUMN_PHONE),
            isIncoming = getBoolean(COLUMN_IS_INCOMING),
            startTime = getLong(COLUMN_START_TIME),
            endTime = getLongOrNull(COLUMN_END_TIME),
            isMissed = getBoolean(COLUMN_IS_MISSED),
            text = getStringOrNull(COLUMN_TEXT)
        )
    }

}