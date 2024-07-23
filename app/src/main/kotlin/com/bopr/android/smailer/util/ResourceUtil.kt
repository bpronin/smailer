package com.bopr.android.smailer.util

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.processor.telegram.TelegramException
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CONNECTION
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.provider.EventState
import com.bopr.android.smailer.provider.EventState.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import java.util.Locale

/**
 * To prevent drawables from being shrinked by R8's resource shrinker we have to
 * hold theirs hardcoded references.
 */
private val RES_DIRECTION_IMAGE = intArrayOf(
    R.drawable.ic_call_missed,
    R.drawable.ic_call_in,
    R.drawable.ic_call_out
)

/**
 * To prevent drawables from being shrinked by R8's resource shrinker we have to
 * hold theirs hardcoded references.
 */
private val RES_STATE_IMAGE = intArrayOf(
    R.drawable.ic_hourglass,
    R.drawable.ic_state_done,
    R.drawable.ic_state_block
)

/**
 * To prevent drawables from being shrinked by R8's resource shrinker we have to
 * hold theirs hardcoded references.
 */
private val RES_TYPE_IMAGE = intArrayOf(
    R.drawable.ic_message,
    R.drawable.ic_call
)

@DrawableRes
fun eventTypeImage(event: PhoneEventData): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return if (event.isSms) {
        RES_TYPE_IMAGE[0]
    } else {
        RES_TYPE_IMAGE[1]
    }
}

@DrawableRes
fun eventDirectionImage(event: PhoneEventData): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return when {
        event.isMissed ->
            RES_DIRECTION_IMAGE[0]

        event.isIncoming ->
            RES_DIRECTION_IMAGE[1]

        else ->
            RES_DIRECTION_IMAGE[2]
    }
}

@DrawableRes
fun eventStateImage(@EventState state: Int): Int {
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
fun eventTypeText(event: PhoneEventData): Int {
    return if (event.isSms) {
        if (event.isIncoming) {
            R.string.incoming_sms
        } else {
            R.string.outgoing_sms
        }
    } else if (event.isMissed) {
        R.string.missed_call
    } else if (event.isIncoming) {
        R.string.incoming_call
    } else {
        R.string.outgoing_call
    }
}

@StringRes
fun eventTypePrefix(event: PhoneEventData): Int {
    return if (event.isSms) {
        if (event.isIncoming) {
            R.string.incoming_sms_from
        } else {
            R.string.outgoing_sms_to
        }
    } else if (event.isMissed) {
        R.string.missed_call_from
    } else if (event.isIncoming) {
        R.string.incoming_call_from
    } else {
        R.string.outgoing_call_to
    }
}

@StringRes
fun eventStateText(@EventState state: Int): Int {
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

        TELEGRAM_NO_CHAT -> R.string.require_start_chat

        TELEGRAM_NO_CONNECTION -> R.string.no_network_try_later
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
