package com.bopr.android.smailer.control.web

import io.ktor.server.html.Placeholder
import io.ktor.server.html.Template
import io.ktor.server.html.insert
import kotlinx.html.BODY
import kotlinx.html.H1
import kotlinx.html.HTML
import kotlinx.html.STYLE
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

abstract class BaseHtmlTemplate : Template<HTML> {
    val pageTitle = Placeholder<H1>()
    val content = Placeholder<BODY>()
    val extraStyles = Placeholder<STYLE>()

    final override fun HTML.apply() {
        head {
            title { +"SMailer" }
            meta { charset = "UTF-8" }
            link {
                rel = "preconnect"
                href = "https://fonts.googleapis.com"
            }
            link {
                rel = "preconnect"
                href = "https://fonts.gstatic.com"
                attributes["crossorigin"] = ""
            }
            link {
                rel = "stylesheet"
                href = "https://fonts.googleapis.com/css2?family=Roboto:wght@100..900&display=swap"
            }
            style {
                unsafe {
                    +"body { font-family: 'Roboto', sans-serif; }"
                    +"h1 { font-size: large; font-weight: 700; }"
                }
                insert(extraStyles)
            }
        }
        body {
            h1 { 
                insert(pageTitle) 
            }
            insert(content)
        }
    }

}