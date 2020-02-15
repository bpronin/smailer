package com.bopr.android.smailer

import androidx.test.platform.app.InstrumentationRegistry
import com.bopr.android.smailer.util.TextUtil.readStream
import com.bopr.android.smailer.util.Util.safeEquals
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
        if (safeEquals(actual, expected)) {
            return true
        } else {
            val exs = Scanner(expected!!).useDelimiter(delimiters)
            val acs = Scanner(actual!!).useDelimiter(delimiters)
            expectedToken = null
            actualToken = null
            while (exs.hasNext() && acs.hasNext()) {
                expectedToken = exs.next()
                actualToken = acs.next()
                if (expectedToken != actualToken) {
                    return false
                }
            }
            return exs.hasNext() == acs.hasNext()
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

        @JvmStatic
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