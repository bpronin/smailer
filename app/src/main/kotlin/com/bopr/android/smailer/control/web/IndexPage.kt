package com.bopr.android.smailer.control.web

import kotlinx.html.a
import kotlinx.html.li
import kotlinx.html.ul

class IndexPage : BaseHtmlTemplate() {
    init {
        pageTitle {
            +"SMailer"
        }
        content {
            ul {
                li {
                    a("/history") { +"Call history" }
                }
            }
        }
    }
}

