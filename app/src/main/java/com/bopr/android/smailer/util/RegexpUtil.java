package com.bopr.android.smailer.util;

/**
 * Class PhoneEventFilter.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
abstract public class RegexpUtil {

    public static String caseInsensitive(String expression) {
        return "(?i)" + expression;
    }

    public static String patternContains(String... words) {
        return ".*(" + Util.separated("|", words) + ").*$";
    }

    public static String patternDoesNotContain(String... words) {
        return "^(?!.*(" + Util.separated("|", words) + ")).*$";
    }

}
