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
 * Parses mail message into remote control task.
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

    private static final String QUOTED_TEXT_REGEX = "\"(.*?)\"";
    private static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile(QUOTED_TEXT_REGEX);
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("([\\d\\+\\-*]+)");

    RemoteCommandParser() {
    }

    @Nullable
    Task parse(@NonNull MailMessage message) {
        if (isEmpty(message.getBody())) {
            return null;
        }

        String subject = message.getSubject();

        /* remove quotations, line separators etc. */
        String body = message.getBody()
                .replaceAll(">(.*)\\r\\n", "")
                .split("\\r\\n\\r\\n")[0]
                .replaceAll("\\r", "")
                .replaceAll("\\n\\n", "\n")
                .replaceAll("\\n", " ");

        log.debug("Parsing: " + body);

        if (isEmpty(body)) {
            return null;
        }

        String command = body
                .replaceAll(QUOTED_TEXT_REGEX, "") /* remove all quoted text */
                .toLowerCase(Locale.ROOT);

        if (command.contains("blacklist")) {
            if (command.contains("delete") || command.contains("remove")) {
                if (command.contains("text")) {
                    return new Task(REMOVE_TEXT_FROM_BLACKLIST, parseTextArgument(body));
                } else {
                    return new Task(REMOVE_PHONE_FROM_BLACKLIST, parsePhoneArgument(subject, body));
                }
            } else {
                if (command.contains("text")) {
                    return new Task(ADD_TEXT_TO_BLACKLIST, parseTextArgument(body));
                } else {
                    return new Task(ADD_PHONE_TO_BLACKLIST, parsePhoneArgument(subject, body));
                }
            }
        } else if (command.contains("whitelist")) {
            if (command.contains("delete") || command.contains("remove")) {
                if (command.contains("text")) {
                    return new Task(REMOVE_TEXT_FROM_WHITELIST, parseTextArgument(body));
                } else {
                    return new Task(REMOVE_PHONE_FROM_WHITELIST, parsePhoneArgument(subject, body));
                }
            } else {
                if (command.contains("text")) {
                    return new Task(ADD_TEXT_TO_WHITELIST, parseTextArgument(body));
                } else {
                    return new Task(ADD_PHONE_TO_WHITELIST, parsePhoneArgument(subject, body));
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

    class Task {
        final int action;
        final String argument;

        private Task(int action, String argument) {
            this.action = action;
            this.argument = argument;
        }

        @Override
        @NonNull
        public String toString() {
            return "Task{" +
                    "action=" + action +
                    ", argument='" + argument + '\'' +
                    '}';
        }
    }
}
