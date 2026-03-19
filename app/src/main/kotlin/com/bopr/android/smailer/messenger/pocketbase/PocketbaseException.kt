package com.bopr.android.smailer.messenger.pocketbase


open class PocketbaseException(
    val code: Code,
    cause: Throwable? = null
) : Exception("Pocketbase error: [${code.name}]", cause) {

    enum class Code {
        POCKETBASE_BAD_ADDRESS,
        POCKETBASE_BAD_RESPONSE,
        POCKETBASE_BAD_CREDENTIALS,
    }

}
