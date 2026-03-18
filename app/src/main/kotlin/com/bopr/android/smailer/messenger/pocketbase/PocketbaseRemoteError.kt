package com.bopr.android.smailer.messenger.pocketbase

class PocketbaseRemoteError(message: String, response: ErrorResponse) :
    Exception("$message - ${response.message}") {
}