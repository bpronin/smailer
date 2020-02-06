package com.bopr.android.smailer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.util.AddressUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static com.bopr.android.smailer.util.Util.QUOTED_TEXT_REGEX;
import static com.bopr.android.smailer.util.Util.extractQuoted;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Parses mail message into remote control task.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class RemoteCommandParser {

    private static Logger log = LoggerFactory.getLogger("RemoteCommandParser");

    static final String ADD_PHONE_TO_BLACKLIST = "add_phone_to_blacklist";
    static final String REMOVE_PHONE_FROM_BLACKLIST = "remove_phone_from_blacklist";
    static final String ADD_PHONE_TO_WHITELIST = "add_phone_to_whitelist";
    static final String REMOVE_PHONE_FROM_WHITELIST = "remove_phone_from_whitelist";
    static final String ADD_TEXT_TO_BLACKLIST = "add_text_to_blacklist";
    static final String REMOVE_TEXT_FROM_BLACKLIST = "remove_text_from_blacklist";
    static final String ADD_TEXT_TO_WHITELIST = "add_text_to_whitelist";
    static final String REMOVE_TEXT_FROM_WHITELIST = "remove_text_from_whitelist";

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
                    return new Task(REMOVE_TEXT_FROM_BLACKLIST, extractQuoted(body));
                } else {
                    return new Task(REMOVE_PHONE_FROM_BLACKLIST, extractPhone(subject, body));
                }
            } else {
                if (command.contains("text")) {
                    return new Task(ADD_TEXT_TO_BLACKLIST, extractQuoted(body));
                } else {
                    return new Task(ADD_PHONE_TO_BLACKLIST, extractPhone(subject, body));
                }
            }
        } else if (command.contains("whitelist")) {
            if (command.contains("delete") || command.contains("remove")) {
                if (command.contains("text")) {
                    return new Task(REMOVE_TEXT_FROM_WHITELIST, extractQuoted(body));
                } else {
                    return new Task(REMOVE_PHONE_FROM_WHITELIST, extractPhone(subject, body));
                }
            } else {
                if (command.contains("text")) {
                    return new Task(ADD_TEXT_TO_WHITELIST, extractQuoted(body));
                } else {
                    return new Task(ADD_PHONE_TO_WHITELIST, extractPhone(subject, body));
                }
            }
        }
        return null;
    }

    private String extractPhone(String subject, String body) {
        String s = extractPhoneFromText(body);
        return s != null ? s : extractPhoneFromText(subject);
    }

    private String extractPhoneFromText(String text) {
        String s = extractQuoted(text);
        return s != null ? s : AddressUtil.extractPhone(text);
    }

    class Task {
        final String action;
        final String argument;

        private Task(String action, String argument) {
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
