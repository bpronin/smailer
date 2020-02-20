package com.bopr.android.smailer

import androidx.test.platform.app.InstrumentationRegistry
import com.bopr.android.smailer.util.readStream
import org.hamcrest.CustomTypeSafeMatcher
import org.hamcrest.Description
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

internal open class HtmlMatcher private constructor(private val expected: String?) : CustomTypeSafeMatcher<String?>(expected) {

    private var expectedToken: String? = null
    private var actualToken: String? = null
    private val delimiters = Pattern.compile("(\\s|>|<|;)+")

    override fun matchesSafely(actual: String?): Boolean {
        if (actual == expected) {
            return true
        } else {
            val expects = Scanner(expected!!).useDelimiter(delimiters)
            val actuals = Scanner(actual!!).useDelimiter(delimiters)
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
        super.describeMismatchSafely(item, description)
        description
                .appendText("\nExpected token: '")
                .appendText(expectedToken)
                .appendText("' but found: '")
                .appendText(actualToken)
                .appendText("'")
    }

    companion object {

        fun htmlEqualsRes(resource: String): HtmlMatcher {
            try {
                InstrumentationRegistry.getInstrumentation().context.assets.open(resource).use {
                    return HtmlMatcher(readStream(it))
                }
            } catch (x: IOException) {
                throw IllegalArgumentException("Invalid resource", x)
            }
        }
    }


}