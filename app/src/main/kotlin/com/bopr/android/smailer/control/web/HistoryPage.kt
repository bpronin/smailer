package com.bopr.android.smailer.control.web

import android.content.Context
import android.text.format.DateFormat
import com.bopr.android.smailer.R
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.phoneCallTypeText
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe

class HistoryPage(private val context: Context) : BaseHtmlTemplate() {
    init {
        extraStyles {
            unsafe {
                +"table { border-collapse: collapse; width: 100%; }"
                +"th, td { border: 1px solid #ccc; padding: 8px; }"
                +"th { background-color: #f2f2f2; font-weight:700; }"
                +"th.col-type { width: 10%;}"
                +"th.col-phone { width: 20%; }"
                +"th.col-text { width: auto;}"
                +"th.col-time { width: 20%; }"
//                        +"td.col-text { font-size: 60%;}"
            }
        }
        pageTitle {
            +context.getString(R.string.history)
        }
        content {
            table {
                thead {
                    tr {
                        th(classes = "col-type") { +context.getString(R.string.type) }
                        th(classes = "col-phone") { +context.getString(R.string.phone_number) }
                        th(classes = "col-text") { +context.getString(R.string.sms_text) }
                        th(classes = "col-time") { +context.getString(R.string.event_time) }
                    }
                }

                for (event in context.database.events.drain()) {
                    (event.payload as? PhoneCallData)?.let {
                        tr {
                            td(classes = "col-type") {
                                +context.getString(phoneCallTypeText(it))
                            }
                            td(classes = "col-phone") { +it.phone }
                            td(classes = "col-text") { +(it.text ?: "") }
                            td(classes = "col-time") {
                                +DateFormat.format(
                                    context.getString(R.string._time_pattern),
                                    it.startTime
                                ).toString()
                            }
                        }
                    }
                }
            }
        }
    }

}