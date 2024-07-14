package com.bopr.android.smailer.external


class TelegramException(val code: Code, cause: Throwable? = null) :
    Exception("Messenger error [${code.name}]", cause) {

    enum class Code {
        TELEGRAM_REQUEST_FAILED,
        TELEGRAM_BAD_RESPONSE,
        TELEGRAM_NO_CHAT,
        TELEGRAM_NO_TOKEN,
        TELEGRAM_INVALID_TOKEN
    }
    
}
