package com.bopr.android.smailer.processor.telegram


open class TelegramException(
    val code: Code,
    message: String,
    cause: Throwable? = null
) :
//    Exception("Messenger error [${code.name}]${message?.let { " - $message" }?:""}", cause) {
    Exception("Messenger error [${code.name}] - $message", cause) {

    enum class Code {
        TELEGRAM_REQUEST_FAILED,
        TELEGRAM_BAD_RESPONSE,
        TELEGRAM_NO_CHAT,
        TELEGRAM_NO_TOKEN,
        TELEGRAM_INVALID_TOKEN,
        TELEGRAM_NO_CONNECTION
    }

}
