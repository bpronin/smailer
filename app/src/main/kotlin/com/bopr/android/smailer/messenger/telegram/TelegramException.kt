package com.bopr.android.smailer.messenger.telegram


open class TelegramException(
    val code: Code,
    message: String,
    cause: Throwable? = null
) : Exception("Telegram error [${code.name}] - $message", cause) {

    enum class Code {
        TELEGRAM_BAD_RESPONSE,
        TELEGRAM_NO_TOKEN,
        TELEGRAM_NO_UPDATES,
        TELEGRAM_INVALID_TOKEN,
    }

}
