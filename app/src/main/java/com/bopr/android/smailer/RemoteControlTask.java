package com.bopr.android.smailer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

class RemoteControlTask {

    static final String ADD_PHONE_TO_BLACKLIST = "add_phone_to_blacklist";
    static final String REMOVE_PHONE_FROM_BLACKLIST = "remove_phone_from_blacklist";
    static final String ADD_PHONE_TO_WHITELIST = "add_phone_to_whitelist";
    static final String REMOVE_PHONE_FROM_WHITELIST = "remove_phone_from_whitelist";
    static final String ADD_TEXT_TO_BLACKLIST = "add_text_to_blacklist";
    static final String REMOVE_TEXT_FROM_BLACKLIST = "remove_text_from_blacklist";
    static final String ADD_TEXT_TO_WHITELIST = "add_text_to_whitelist";
    static final String REMOVE_TEXT_FROM_WHITELIST = "remove_text_from_whitelist";
    static final String SEND_SMS_TO_CALLER = "send_sms_to_caller";

    private String acceptor;
    private String action;
    private final Map<String, String> arguments = new HashMap<>();

    @Nullable
    String getAction() {
        return action;
    }

    void setAction(String action) {
        this.action = action;
    }

    @Nullable
    String getAcceptor() {
        return acceptor;
    }

    void setAcceptor(String acceptor) {
        this.acceptor = acceptor;
    }

    @Nullable
    String getArgument() {
        return getArgument("value");
    }

    @Nullable
    String getArgument(String key) {
        return arguments.get(key);
    }

    void setArgument(String value) {
        setArgument("value", value);
    }

    void setArgument(String key, String value) {
        arguments.put(key, value);
    }

    @Override
    @NonNull
    public String toString() {
        return "RemoteControlTask{" +
                "acceptor='" + acceptor + '\'' +
                ", action='" + action + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
