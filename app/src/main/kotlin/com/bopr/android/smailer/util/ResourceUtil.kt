package com.bopr.android.smailer.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.R

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
fun eventTypeImage(event: PhoneEvent): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return if (event.isSms) {
        RES_TYPE_IMAGE[0]
    } else {
        RES_TYPE_IMAGE[1]
    }
}

@DrawableRes
fun eventDirectionImage(event: PhoneEvent): Int {
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
fun eventStateImage(event: PhoneEvent): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return when (event.state) {
        PhoneEvent.STATE_PENDING ->
            RES_STATE_IMAGE[0]

        PhoneEvent.STATE_PROCESSED ->
            RES_STATE_IMAGE[1]

        PhoneEvent.STATE_IGNORED ->
            RES_STATE_IMAGE[2]

        else ->
            throw IllegalArgumentException("Unknown state")
    }
}

@StringRes
fun eventTypeText(event: PhoneEvent): Int {
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
fun eventTypePrefix(event: PhoneEvent): Int {
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
fun eventStateText(event: PhoneEvent): Int {
    return when (event.state) {
        PhoneEvent.STATE_PENDING ->
            R.string.pending

        PhoneEvent.STATE_PROCESSED ->
            R.string.sent_email

        PhoneEvent.STATE_IGNORED ->
            R.string.ignored

        else ->
            throw IllegalArgumentException("Unknown state")
    }
}