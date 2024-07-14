package com.bopr.android.smailer.util

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
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
fun eventTypeImage(event: PhoneEventInfo): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return if (event.isSms) {
        RES_TYPE_IMAGE[0]
    } else {
        RES_TYPE_IMAGE[1]
    }
}

@DrawableRes
fun eventDirectionImage(event: PhoneEventInfo): Int {
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
fun eventStateImage(event: PhoneEventInfo): Int {
    /* do not use direct drawable resources references here due to shrinker issue */
    return when (event.state) {
        PhoneEventInfo.STATE_PENDING ->
            RES_STATE_IMAGE[0]

        PhoneEventInfo.STATE_PROCESSED ->
            RES_STATE_IMAGE[1]

        PhoneEventInfo.STATE_IGNORED ->
            RES_STATE_IMAGE[2]

        else ->
            throw IllegalArgumentException("Unknown state")
    }
}

@StringRes
fun eventTypeText(event: PhoneEventInfo): Int {
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
fun eventTypePrefix(event: PhoneEventInfo): Int {
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
fun eventStateText(event: PhoneEventInfo): Int {
    return when (event.state) {
        PhoneEventInfo.STATE_PENDING ->
            R.string.pending

        PhoneEventInfo.STATE_PROCESSED ->
            R.string.processed

        PhoneEventInfo.STATE_IGNORED ->
            R.string.ignored

        else ->
            throw IllegalArgumentException("Unknown state")
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