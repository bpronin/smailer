package com.bopr.android.smailer.control

import androidx.annotation.StringDef
import kotlin.annotation.AnnotationRetention.SOURCE

internal data class RemoteControlTask(val acceptor: String?, @Action var action: String? = null,
                                      val arguments: MutableMap<String, String?> = mutableMapOf()) {

    constructor(acceptor: String?, @Action action: String? = null, argument: String?)
            : this(acceptor, action, mutableMapOf(VALUE to argument))

    var argument: String?
        get() = arguments[VALUE]
        set(value) {
            arguments[VALUE] = value
        }

    @StringDef(
            ADD_PHONE_TO_BLACKLIST,
            REMOVE_PHONE_FROM_BLACKLIST,
            ADD_PHONE_TO_WHITELIST,
            REMOVE_PHONE_FROM_WHITELIST,
            ADD_TEXT_TO_BLACKLIST,
            REMOVE_TEXT_FROM_BLACKLIST,
            ADD_TEXT_TO_WHITELIST,
            REMOVE_TEXT_FROM_WHITELIST,
            SEND_SMS_TO_CALLER
    )
    @Retention(SOURCE)
    annotation class Action

    companion object {

        const val VALUE = "value"

        const val ADD_PHONE_TO_BLACKLIST = "add_phone_to_blacklist"
        const val REMOVE_PHONE_FROM_BLACKLIST = "remove_phone_from_blacklist"
        const val ADD_PHONE_TO_WHITELIST = "add_phone_to_whitelist"
        const val REMOVE_PHONE_FROM_WHITELIST = "remove_phone_from_whitelist"
        const val ADD_TEXT_TO_BLACKLIST = "add_text_to_blacklist"
        const val REMOVE_TEXT_FROM_BLACKLIST = "remove_text_from_blacklist"
        const val ADD_TEXT_TO_WHITELIST = "add_text_to_whitelist"
        const val REMOVE_TEXT_FROM_WHITELIST = "remove_text_from_whitelist"
        const val SEND_SMS_TO_CALLER = "send_sms_to_caller"
    }
}