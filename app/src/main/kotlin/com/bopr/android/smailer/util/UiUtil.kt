package com.bopr.android.smailer.util

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.ParagraphStyle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat.createBlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.WavyUnderlineSpan

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

@DrawableRes
fun eventTypeImage(event: PhoneEvent): Int {
    return if (event.isSms) {
        R.drawable.ic_message
    } else {
        R.drawable.ic_call
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
        STATE_PENDING ->
            R.string.pending
        STATE_PROCESSED ->
            R.string.sent_email
        STATE_IGNORED ->
            R.string.ignored
        else ->
            throw IllegalArgumentException("Unknown state")
    }
}

@DrawableRes
fun eventDirectionImage(event: PhoneEvent): Int {
    return when {
        event.isMissed ->
            R.drawable.ic_call_missed
        event.isIncoming ->
            R.drawable.ic_call_in
        else ->
            R.drawable.ic_call_out
    }
}

@DrawableRes
fun eventStateImage(event: PhoneEvent): Int {
    return when (event.state) {
        STATE_PENDING ->
            R.drawable.ic_hourglass
        STATE_PROCESSED ->
            R.drawable.ic_state_done
        STATE_IGNORED ->
            R.drawable.ic_state_block
        else ->
            throw IllegalArgumentException("Unknown state")
    }
}

/**
 * Returns text underlined with wavy red line.
 */
fun Context.underwivedText(value: CharSequence?): Spannable {
    val spannable: Spannable = SpannableString(value)
    val span: ParagraphStyle = WavyUnderlineSpan(ContextCompat.getColor(this, R.color.errorLine))
    spannable.setSpan(span, 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
}

/**
 * Returns text of accent color.
 */
fun Context.accentedText(value: CharSequence?): Spannable {
    val spannable: Spannable = SpannableString(value)
    val span: CharacterStyle = ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorAccent))
    spannable.setSpan(span, 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
}

@ColorInt
fun Context.getColorFromAttr(@AttrRes attr: Int): Int {
    val a = obtainStyledAttributes(intArrayOf(attr))
    val color = a.getResourceId(0, 0)
    a.recycle()
    return ContextCompat.getColor(this, color)
}

fun Context.showToast(text: String) {
    val toast: Toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
        /* it looks ugly on old devises */
        val view = toast.view
        view.background.colorFilter = createBlendModeColorFilterCompat(
                ContextCompat.getColor(this, R.color.colorAccent), BlendModeCompat.DARKEN)
        view.findViewById<TextView>(android.R.id.message)?.setTextColor(
                ContextCompat.getColor(this, R.color.colorAccentText))
    }
    toast.show()
}

fun Context.showToast(@StringRes textRes: Int) {
    showToast(getString(textRes))
}

fun Fragment.showToast(text: String) {
    requireContext().showToast(text)
}

fun Fragment.showToast(@StringRes textRes: Int) {
    showToast(getString(textRes))
}

fun Fragment.getQuantityString(@PluralsRes resId: Int, quantity: Number): String {
    return resources.getQuantityString(resId, quantity.toInt(), quantity)
}

fun View.showAnimated(@AnimRes animationRes: Int, delay: Long) {
    if (visibility != VISIBLE) {
        clearAnimation()
        val animation = loadAnimation(context, animationRes).apply {
            startOffset = delay

            setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {
                    visibility = VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                    /* nothing */
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    /* nothing */
                }
            })
        }
        visibility = INVISIBLE /* to properly animate coordinates ensure it is not GONE here */
        startAnimation(animation)
    }
}