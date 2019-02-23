package com.bopr.android.smailer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

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

    RemoteCommandParser() {
    }

    @Nullable Result parse(@NonNull MailMessage message) {
        if (isEmpty(message.body)) {
            return null;
        }

        /* remove quotation and so on */
        String text = message.body
                .replaceAll(">(.*)\\r\\n", "")
                .split("\\r\\n\\r\\n")[0]
                .toLowerCase(Locale.ROOT);

        log.debug("Action text: " + text);

        if (isEmpty(text)) {
            return null;
        }

        String argument = null;
        if (text.contains("blacklist")) {
            if (text.contains("delete") || text.contains("remove")) {
                if (text.contains("text")) {
                    return new Result(REMOVE_TEXT_FROM_BLACKLIST, argument);
                } else {
                    return new Result(REMOVE_PHONE_FROM_BLACKLIST, argument);
                }
            } else {
                if (text.contains("text")) {
                    return new Result(ADD_TEXT_TO_BLACKLIST, argument);
                } else {
                    return new Result(ADD_PHONE_TO_BLACKLIST, argument);
                }
            }
        } else if (text.contains("whitelist")) {
            if (text.contains("delete") || text.contains("remove")) {
                if (text.contains("text")) {
                    return new Result(REMOVE_TEXT_FROM_WHITELIST, argument);
                } else {
                    return new Result(REMOVE_PHONE_FROM_WHITELIST, argument);
                }
            } else {
                if (text.contains("text")) {
                    return new Result(ADD_TEXT_TO_WHITELIST, argument);
                } else {
                    return new Result(ADD_PHONE_TO_WHITELIST, argument);
                }
            }
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
