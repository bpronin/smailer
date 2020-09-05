package com.bopr.android.smailer.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.bopr.android.smailer.R

/**
 * Abstract [TextView] input validator.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class TextValidator(protected val view: TextView) : TextWatcher {

    private var underwaveSpan: WavyUnderlineSpan? = null

    /**
     * Returns true if text should be underlined with red wavy line.
     */
    protected open fun shouldUnderwave(text: String?): Boolean {
        return false
    }

    /**
     * Returns validation error message.
     */
    protected open fun getErrorMessage(text: String?): String? {
        return null
    }

    override fun afterTextChanged(editable: Editable) {
        val text = editable.toString()

        view.error = getErrorMessage(text)

        if (shouldUnderwave(text)) {
            if (underwaveSpan == null){
                underwaveSpan = WavyUnderlineSpan(getColor(view.context, R.color.errorLine))
            }
            editable.setSpan(underwaveSpan, 0, editable.length, 0)
        } else {
            editable.removeSpan(underwaveSpan)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        /* do nothing */
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        /* do nothing */
    }
}