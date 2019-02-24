package com.bopr.android.smailer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Parses text to control actions.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class RemoteCommandParser {

    private static Logger log = LoggerFactory.getLogger("RemoteCommandParser");

    static final int ADD_PHONE_TO_BLACKLIST = 0;
    static final int REMOVE_PHONE_FROM_BLACKLIST = 1;
    static final int ADD_PHONE_TO_WHITELIST = 2;
    static final int REMOVE_PHONE_FROM_WHITELIST = 3;
    static final int ADD_TEXT_TO_BLACKLIST = 4;
    static final int REMOVE_TEXT_FROM_BLACKLIST = 5;
    static final int ADD_TEXT_TO_WHITELIST = 6;
    static final int REMOVE_TEXT_FROM_WHITELIST = 7;

    private static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile("\"(.*?)\"");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("([\\d\\-*]+)");

    RemoteCommandParser() {
    }

    @Nullable
    Result parse(@NonNull MailMessage message) {
        if (isEmpty(message.getBody())) {
            return null;
        }

        String subject = message.getSubject();
        /* remove quotations, line separators and so on */
        String body = message.getBody()
                .replaceAll(">(.*)\\r\\n", "")
                .split("\\r\\n\\r\\n")[0]
                .replaceAll("\\r", "")
                .replaceAll("\\n", " ")
                .replaceAll("\\n\\n", "\n")
                .toLowerCase(Locale.ROOT);

//        String text = message.getBody()
//                .split("\\r\\n")[0]
//                .toLowerCase(Locale.ROOT);

        log.debug("Parsing: " + body);

        if (isEmpty(body)) {
            return null;
        }

        if (body.contains("blacklist")) {
            if (body.contains("delete") || body.contains("remove")) {
                if (body.contains("text")) {
                    return new Result(REMOVE_TEXT_FROM_BLACKLIST, parseTextArgument(body));
                } else {
                    return new Result(REMOVE_PHONE_FROM_BLACKLIST, parsePhoneArgument(subject, body));
                }
            } else {
                if (body.contains("text")) {
                    return new Result(ADD_TEXT_TO_BLACKLIST, parseTextArgument(body));
                } else {
                    return new Result(ADD_PHONE_TO_BLACKLIST, parsePhoneArgument(subject, body));
                }
            }
        } else if (body.contains("whitelist")) {
            if (body.contains("delete") || body.contains("remove")) {
                if (body.contains("text")) {
                    return new Result(REMOVE_TEXT_FROM_WHITELIST, parseTextArgument(body));
                } else {
                    return new Result(REMOVE_PHONE_FROM_WHITELIST, parsePhoneArgument(subject, body));
                }
            } else {
                if (body.contains("text")) {
                    return new Result(ADD_TEXT_TO_WHITELIST, parseTextArgument(body));
                } else {
                    return new Result(ADD_PHONE_TO_WHITELIST, parsePhoneArgument(subject, body));
                }
            }
        }
        return null;
    }

    private String parsePhoneArgument(String subject, String body) {
        String s = findNumber(body);
        return s != null ? s : findNumber(subject);
    }

    private String parseTextArgument(String body) {
        return findFirstQuotedFragment(body);
    }

    private String findNumber(String text) {
        String s = findFirstQuotedFragment(text);
        return s != null ? s : findFirstNumberFragment(text);
    }

    private String findFirstQuotedFragment(String text) {
        Matcher matcher = QUOTED_TEXT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String findFirstNumberFragment(String text) {
        Matcher matcher = PHONE_NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    class Result {
        final int action;
        final String argument;

        private Result(int action, String argument) {
            this.action = action;
            this.argument = argument;
        }

        @Override
        @NonNull
        public String toString() {
            return "Result{" +
                    "action=" + action +
                    ", argument='" + argument + '\'' +
                    '}';
        }
    }
}
