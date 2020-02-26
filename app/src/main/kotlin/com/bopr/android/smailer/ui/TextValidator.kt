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
abstract class TextValidator(private val view: TextView) : TextWatcher {

    private val span = WavyUnderlineSpan(getColor(view.context, R.color.errorLine))

    /**
     * Returns true if input is valid.
     */
    abstract fun isValidInput(view: TextView, editable: Editable, text: String?): Boolean

    override fun afterTextChanged(editable: Editable) {
        if (isValidInput(view, editable, editable.toString())) {
            editable.removeSpan(span)
        } else {
            editable.setSpan(span, 0, editable.length, 0)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        /* do nothing */
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        /* do nothing */
    }
}