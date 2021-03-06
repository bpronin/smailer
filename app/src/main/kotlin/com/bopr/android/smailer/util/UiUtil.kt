package com.bopr.android.smailer.util

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
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
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.WavyUnderlineSpan
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.Executors

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

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
/*
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
        */
/* it looks ugly on old devises *//*

        val view = toast.view
        view.background.colorFilter = createBlendModeColorFilterCompat(
                ContextCompat.getColor(this, R.color.colorAccent), BlendModeCompat.DARKEN)
        view.findViewById<TextView>(android.R.id.message)?.setTextColor(
                ContextCompat.getColor(this, R.color.colorAccentText))
    }
*/
    toast.show()
}

fun Context.showToast(@StringRes textRes: Int) {
    showToast(getString(textRes))
}

fun Fragment.showToast(text: String) {
    context?.showToast(text)
}

fun Fragment.showToast(@StringRes textRes: Int) {
    showToast(getString(textRes))
}

fun Resources.quantityString(@PluralsRes manyRes: Int, @StringRes zeroRes: Int, quantity: Number): String {
    return if (quantity.toInt() == 0 && zeroRes != 0)
        getString(zeroRes)
    else
        getQuantityString(manyRes, quantity.toInt(), quantity)
}

fun Fragment.getQuantityString(@PluralsRes manyRes: Int, @StringRes zeroRes: Int, quantity: Number): String {
    return resources.quantityString(manyRes, zeroRes, quantity)
}

fun Fragment.getQuantityString(@PluralsRes manyRes: Int, quantity: Number): String {
    return getQuantityString(manyRes, 0, quantity)
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

fun View.showSoftKeyboard() {
    if (requestFocus()) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, SHOW_IMPLICIT)
    }
}

fun RecyclerView.addOnItemSwipedListener(action: (RecyclerView.ViewHolder) -> Unit) {
    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(holder: RecyclerView.ViewHolder, swipeDir: Int) {
            action.invoke(holder)
        }
    }).also {
        it.attachToRecyclerView(this)
    }
}

fun <T> runInBackground(task: () -> T): Task<T> {
    return Tasks.call(Executors.newSingleThreadExecutor(), {
        task()  /* here we are in main thread */
    })
}

fun <T> Preference.runBackgroundTask(onPerform: () -> T?, onComplete: (T?) -> Unit) {
    val lastIcon = icon
    val progressIcon = AnimatedVectorDrawableCompat.create(context, R.drawable.animated_progress)!!
    icon = progressIcon
    progressIcon.start()

    runInBackground(onPerform).addOnCompleteListener {
        progressIcon.stop()
        icon = lastIcon
        onComplete(it.result)
    }
}
