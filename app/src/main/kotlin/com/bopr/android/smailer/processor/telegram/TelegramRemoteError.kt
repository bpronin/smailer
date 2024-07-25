package com.bopr.android.smailer.processor.telegram


data class TelegramRemoteError(
    val errorCode: Int,
    val description: String? = null
) {

    override fun toString(): String {
        return "[$errorCode] - $description"
    }
}
