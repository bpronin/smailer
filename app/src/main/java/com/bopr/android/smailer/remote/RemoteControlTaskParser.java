package com.bopr.android.smailer.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Scanner;

import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_PHONE_TO_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_PHONE_TO_WHITELIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_TEXT_TO_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_TEXT_TO_WHITELIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_PHONE_FROM_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_PHONE_FROM_WHITELIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_TEXT_FROM_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_TEXT_FROM_WHITELIST;
import static com.bopr.android.smailer.util.AddressUtil.PHONE_PATTERN;
import static com.bopr.android.smailer.util.TextUtil.QUOTED_TEXT_PATTERN;

/**
 * Parses text into remote control task.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class RemoteControlTaskParser {
    
    RemoteControlTask parse(String text) {
        RemoteControlTask task = new RemoteControlTask();
        Scanner scanner = new Scanner(text).useDelimiter("\\W+");

        if (hasNextToken(scanner, "(?i:DEVICE)")) {
            task.setAcceptor(nextQuoted(scanner));

            switch (nextToken(scanner, "(?i:ADD|REMOVE|SEND)")) {
                case "SEND":
                    if (hasNextToken(scanner, "(?i:SMS)")) {
                        task.setAction(RemoteControlTask.SEND_SMS_TO_CALLER);
                        task.setArgument("text", nextQuoted(scanner));
                        task.setArgument("phone", nextPhone(scanner));
                    }
                    break;
                case "ADD":
                    switch (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                        case "PHONE":
                            task.setArgument(nextPhone(scanner));
                            switch (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                case "BLACKLIST":
                                    task.setAction(ADD_PHONE_TO_BLACKLIST);
                                    break;
                                case "WHITELIST":
                                    task.setAction(ADD_PHONE_TO_WHITELIST);
                                    break;
                            }
                            break;
                        case "TEXT":
                            task.setArgument(nextQuoted(scanner));
                            switch (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                case "BLACKLIST":
                                    task.setAction(ADD_TEXT_TO_BLACKLIST);
                                    break;
                                case "WHITELIST":
                                    task.setAction(ADD_TEXT_TO_WHITELIST);
                                    break;
                            }
                            break;
                    }
                case "REMOVE":
                    switch (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                        case "PHONE":
                            task.setArgument(nextPhone(scanner));
                            switch (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                case "BLACKLIST":
                                    task.setAction(REMOVE_PHONE_FROM_BLACKLIST);
                                    break;
                                case "WHITELIST":
                                    task.setAction(REMOVE_PHONE_FROM_WHITELIST);
                                    break;
                            }
                            break;
                        case "TEXT":
                            task.setArgument(nextQuoted(scanner));
                            switch (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                case "BLACKLIST":
                                    task.setAction(REMOVE_TEXT_FROM_BLACKLIST);
                                    break;
                                case "WHITELIST":
                                    task.setAction(REMOVE_TEXT_FROM_WHITELIST);
                                    break;
                            }
                            break;
                    }
                    break;
            }
        }

        return task;
    }

    private boolean hasNextToken(Scanner scanner, String pattern) {
        return scanner.findWithinHorizon(pattern, 0) != null;
    }

    @NonNull
    private String nextToken(Scanner scanner, String pattern) {
        String s = scanner.findWithinHorizon(pattern, 0);
        return s != null ? s.toUpperCase() : "";
    }

    @Nullable
    private String nextQuoted(Scanner scanner) {
        if (scanner.findWithinHorizon(QUOTED_TEXT_PATTERN, 0) != null) {
            return scanner.match().group(1);
        }
        return null;
    }

    @Nullable
    private String nextPhone(Scanner scanner) {
        return scanner.findWithinHorizon(PHONE_PATTERN, 0);
    }
}
