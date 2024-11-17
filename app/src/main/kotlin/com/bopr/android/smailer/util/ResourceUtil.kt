package com.bopr.android.smailer.util

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.messenger.telegram.TelegramException
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_CONNECTION
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_UPDATES
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.messenger.MessageState
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import java.util.Locale

/**
 * NOTE: To prevent drawables from being shrunk by R8's resource shrinker we have to
 * hold theirs hardcoded references.
 */
private val RES_DIRECTION_IMAGE = intArrayOf(
    R.drawable.ic_call_missed,
    R.drawable.ic_call_in,
    R.drawable.ic_call_out
)

/**
 * NOTE: To prevent drawables from being shrunk by R8's resource shrinker we have to
 * hold theirs hardcoded references.
 */
private val RES_STATE_IMAGE = intArrayOf(
    R.drawable.ic_hourglass,
    R.drawable.ic_state_done,
    R.drawable.ic_state_block
)

/**
 * NOTE: To prevent drawables from being shrunk by R8's resource shrinker we have to
 * hold theirs hardcoded references.
 */
private val RES_TYPE_IMAGE = intArrayOf(
    R.drawable.ic_message,
    R.drawable.ic_call
)

@DrawableRes
fun phoneCallTypeImage(info: PhoneCallInfo): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return if (info.isSms) {
        RES_TYPE_IMAGE[0]
    } else {
        RES_TYPE_IMAGE[1]
    }
}

@DrawableRes
fun phoneCallDirectionImage(info: PhoneCallInfo): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return when {
        info.isMissed ->
            RES_DIRECTION_IMAGE[0]

        info.isIncoming ->
            RES_DIRECTION_IMAGE[1]

        else ->
            RES_DIRECTION_IMAGE[2]
    }
}

@DrawableRes
fun messageStateImage(@MessageState state: Int): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return when (state) {
        STATE_PENDING ->
            RES_STATE_IMAGE[0]

        STATE_PROCESSED ->
            RES_STATE_IMAGE[1]

        STATE_IGNORED ->
            RES_STATE_IMAGE[2]

        else ->
            throw IllegalArgumentException("Unknown state")
    }
}

@StringRes
fun phoneCallTypeText(info: PhoneCallInfo): Int {
    return if (info.isSms) {
        if (info.isIncoming) {
            R.string.incoming_sms
        } else {
            R.string.outgoing_sms
        }
    } else if (info.isMissed) {
        R.string.missed_call
    } else if (info.isIncoming) {
        R.string.incoming_call
    } else {
        R.string.outgoing_call
    }
}

@StringRes
fun phoneCallTypePrefix(info: PhoneCallInfo): Int {
    return if (info.isSms) {
        if (info.isIncoming) {
            R.string.incoming_sms_from
        } else {
            R.string.outgoing_sms_to
        }
    } else if (info.isMissed) {
        R.string.missed_call_from
    } else if (info.isIncoming) {
        R.string.incoming_call_from
    } else {
        R.string.outgoing_call_to
    }
}

@StringRes
fun messageStateText(@MessageState state: Int): Int {
    return when (state) {
        STATE_PENDING ->
            R.string.pending

        STATE_PROCESSED ->
            R.string.processed

        STATE_IGNORED ->
            R.string.ignored

        else ->
            throw IllegalArgumentException("Unknown state")
    }
}

@StringRes
fun onOffText(value: Boolean): Int {
    return if (value) R.string.on else R.string.off
}

@StringRes
fun telegramErrorText(error: TelegramException): Int {
    return when (error.code) {
        TELEGRAM_REQUEST_FAILED,
        TELEGRAM_BAD_RESPONSE -> R.string.error_sending_test_message

        TELEGRAM_NO_TOKEN -> R.string.no_telegram_bot_token

        TELEGRAM_INVALID_TOKEN -> R.string.bad_telegram_bot_token

        TELEGRAM_NO_UPDATES -> R.string.unable_determine_chat

        TELEGRAM_NO_CHAT -> R.string.require_start_chat

        TELEGRAM_NO_CONNECTION -> R.string.no_telegram_network_try_later
    }
}

fun Context.localeResources(locale: Locale): Resources {
    return if (locale == Locale.getDefault()) {
        resources
    } else {
        val configuration = resources.configuration.apply {
            setLocale(locale)
        }
        createConfigurationContext(configuration).resources
    }
}
