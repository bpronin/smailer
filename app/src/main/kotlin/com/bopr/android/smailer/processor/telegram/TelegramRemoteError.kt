package com.bopr.android.smailer.processor.telegram


class TelegramRemoteError(exceptionCode: Code, errorCode: Int, description: String? = null) :
    TelegramException(exceptionCode, "[$errorCode] - $description")
