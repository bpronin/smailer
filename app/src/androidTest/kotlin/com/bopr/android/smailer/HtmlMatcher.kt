package com.bopr.android.smailer

import androidx.test.platform.app.InstrumentationRegistry
import com.bopr.android.smailer.util.readStream
import org.hamcrest.CustomTypeSafeMatcher
import org.hamcrest.Description
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * Utility class to match HTML files ignoring whitespaces.
 */
internal open class HtmlMatcher private constructor(private val expected: String) :
        CustomTypeSafeMatcher<String?>("HTML matches") {

    private lateinit var actual: String
    private var expectedToken: String? = null
    private var actualToken: String? = null
    private val delimiters = Pattern.compile("(\\s|>|<|;)+")

    override fun matchesSafely(html: String?): Boolean {
        this.actual = html!!
        if (actual == expected) {
            return true
        } else {
            val expects = Scanner(expected).useDelimiter(delimiters)
            val actuals = Scanner(actual).useDelimiter(delimiters)
            expectedToken = null
            actualToken = null
            while (expects.hasNext() && actuals.hasNext()) {
                expectedToken = expects.next()
                actualToken = actuals.next()
                if (expectedToken != actualToken) {
                    return false
                }
            }
            return expects.hasNext() == actuals.hasNext()
        }
    }

    override fun describeMismatchSafely(item: String?, description: Description) {
        description
                .appendText("Expected token [")
                .appendText(expectedToken)
                .appendText("] but [")
                .appendText(actualToken)
                .appendText("] found.")

        description
                .appendText("\n-------\n")
                .appendText(actual)
    }

    companion object {

        fun htmlEquals(resourceName: String): HtmlMatcher {
            try {
                InstrumentationRegistry.getInstrumentation().context.assets.open(resourceName).use {
                    return HtmlMatcher(readStream(it))
                }
            } catch (x: IOException) {
                throw IllegalArgumentException("Invalid resource", x)
            }
        }
    }


}