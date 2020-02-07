package com.bopr.android.smailer;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.bopr.android.smailer.util.Util.readStream;

class HtmlMatcher extends CustomTypeSafeMatcher<String> {

    private final String expected;
    private String expectedToken;
    private String actualToken;

    static Matcher<String> htmlEquals(String expected) {
        return new HtmlMatcher(expected);
    }

    static Matcher<String> htmlEqualsRes(String resource) {
        try {
            InputStream stream = getInstrumentation().getContext().getAssets().open(resource);
            String expected = readStream(stream);
            stream.close();
            return htmlEquals(expected);
        } catch (IOException x) {
            throw new IllegalArgumentException(x);
        }
    }


    private HtmlMatcher(String expected) {
        super(expected);
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(String actual) {
        Pattern delimiters = Pattern.compile("(\\s|>|<|;)+");
        Scanner es = new Scanner(expected).useDelimiter(delimiters);
        Scanner as = new Scanner(actual).useDelimiter(delimiters);

        expectedToken = null;
        actualToken = null;

        while (es.hasNext() && as.hasNext()) {
            expectedToken = es.next();
            actualToken = as.next();
            if (!expectedToken.equals(actualToken)) {
                return false;
            }
        }
        return es.hasNext() == as.hasNext();
    }

    @Override
    protected void describeMismatchSafely(String item, Description description) {
        super.describeMismatchSafely(item, description);
        description
                .appendText("\nExpected token: '")
                .appendText(expectedToken)
                .appendText("' but found: '")
                .appendText(actualToken)
                .appendText("'");
    }
}
