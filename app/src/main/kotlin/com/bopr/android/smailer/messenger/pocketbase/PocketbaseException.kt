package com.bopr.android.smailer.messenger.pocketbase


open class PocketbaseException(
    val code: Code,
    message: String,
    cause: Throwable? = null
) : Exception("Pocketbase error: [${code.name}] - $message", cause) {

    enum class Code {
        POCKETBASE_BAD_ADDRESS,
        POCKETBASE_BAD_RESPONSE,
        POCKETBASE_BAD_CREDENTIALS,
    }

}
