package com.bopr.android.smailer.control

internal data class ControlCommand(
    val target: String?
) {

    lateinit var action: Action
    val arguments: MutableMap<String, String?> = mutableMapOf()
    var argument: String?
        get() = arguments["value"]
        set(value) {
            arguments["value"] = value
        }

    enum class Action {
        ADD_PHONE_TO_BLACKLIST,
        REMOVE_PHONE_FROM_BLACKLIST,
        ADD_PHONE_TO_WHITELIST,
        REMOVE_PHONE_FROM_WHITELIST,
        ADD_TEXT_TO_BLACKLIST,
        REMOVE_TEXT_FROM_BLACKLIST,
        ADD_TEXT_TO_WHITELIST,
        REMOVE_TEXT_FROM_WHITELIST,
        SEND_SMS_TO_CALLER
    }

}