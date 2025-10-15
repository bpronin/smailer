package com.bopr.android.smailer.messenger.telegram

interface TelegramFormatter {

    val format: String /* HTML or MarkdownV2 */
    
    fun formatMessage(): String

}
