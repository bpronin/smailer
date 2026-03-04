package com.bopr.android.smailer.control.web

import android.content.Context
import com.bopr.android.smailer.R
import kotlinx.html.a
import kotlinx.html.li
import kotlinx.html.ul

class IndexPage(context: Context) : BaseHtmlTemplate() {
    init {
        pageTitle {
            +context.getString(R.string.app_name)
        }
        content {
            ul {
                li {
                    a("/history") { +context.getString(R.string.history) }
                }
            }
        }
    }
}

