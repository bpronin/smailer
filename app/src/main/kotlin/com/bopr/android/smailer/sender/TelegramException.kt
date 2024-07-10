package com.bopr.android.smailer.sender


class TelegramException(val errorCode: Code, cause: Throwable? = null) :
    Exception("Messenger error.", cause) {

    enum class Code {
        TELEGRAM_REQUEST_FAILED,
        TELEGRAM_NO_TOKEN,
        TELEGRAM_INVALID_TOKEN,
        TELEGRAM_BAD_RESPONSE,
        TELEGRAM_NO_CHAT
    }
}
