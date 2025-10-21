package com.bopr.android.smailer.util

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.WavyUnderlineSpan
import java.util.concurrent.Executor
import java.util.concurrent.Executors.*

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */

/**
 * Returns text underlined with wavy red line.
 */
fun Context.underwivedText(value: CharSequence?): Spannable {
    val span = WavyUnderlineSpan(ContextCompat.getColor(this, R.color.errorLine))
    return SpannableString(value).apply {
        setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

/**
 * Returns text of accent color.
 */
fun Context.accentedText(value: CharSequence?): Spannable {
    val span = ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorAccent))
    return SpannableString(value).apply {
        setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

@ColorInt
fun Context.getColorFromAttr(@AttrRes attr: Int): Int {
    val a = obtainStyledAttributes(intArrayOf(attr))
    val color = a.getResourceId(0, 0)
    a.recycle()
    return ContextCompat.getColor(this, color)
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Fragment.showToast(text: String) {
    context?.showToast(text)
}

fun Fragment.showToast(@StringRes textRes: Int) {
    showToast(getString(textRes))
}

fun Resources.quantityString(
    @PluralsRes manyRes: Int,
    @StringRes zeroRes: Int,
    quantity: Number
): String {
    return if (quantity.toInt() == 0 && zeroRes != 0)
        getString(zeroRes)
    else
        getQuantityString(manyRes, quantity.toInt(), quantity)
}

fun Fragment.getQuantityString(
    @PluralsRes manyRes: Int,
    @StringRes zeroRes: Int,
    quantity: Number
): String {
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
    ItemTouchHelper(object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(holder: RecyclerView.ViewHolder, swipeDir: Int) {
            action.invoke(holder)
        }
    }).also {
        it.attachToRecyclerView(this)
    }
}

fun <T> runLater(
    onPerform: () -> T,
    onComplete: () -> Unit,
    onSuccess: (T) -> Unit,
    onError: (Throwable) -> Unit
) {
    val result = runCatching(onPerform)
    Handler(Looper.getMainLooper()).post {
        onComplete()
        result.fold(onSuccess, onError)
    }
}

fun <T> runInBackground(
    onComplete: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (Throwable) -> Unit = {},
    onPerform: () -> T
) {
    newSingleThreadExecutor().execute(onComplete, onSuccess, onError, onPerform)
}

fun <T> Executor.execute(
    onComplete: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (Throwable) -> Unit = {},
    onPerform: () -> T
) {
    execute {
        runLater(onPerform, onComplete, onSuccess, onError)
    }
}
