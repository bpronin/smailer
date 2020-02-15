package com.bopr.android.smailer.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import androidx.core.content.ContextCompat
import com.bopr.android.smailer.R

/**
 * Draws wavy line under the text.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class WavyUnderlineSpan @JvmOverloads constructor(
        private val context: Context,
        private val lineColor: Int = ContextCompat.getColor(context, R.color.errorLine),
        private val lineWidth: Int = 1,
        private val waveSize: Int = 4) : LineBackgroundSpan {

    override fun drawBackground(canvas: Canvas, paint: Paint,
                                left: Int, right: Int, top: Int,
                                baseline: Int, bottom: Int,
                                text: CharSequence, start: Int, end: Int, lnum: Int) {
        val p = Paint(paint).apply {
            color = lineColor
            strokeWidth = lineWidth.toFloat()
        }

        val width = paint.measureText(text, start, end).toInt()
        val doubleWaveSize = waveSize * 2
        var x = left
        while (x < left + width) {
            val stopY = bottom - waveSize
            val stopX = x + waveSize
            canvas.drawLine(x.toFloat(), bottom.toFloat(), stopX.toFloat(), stopY.toFloat(), p)
            canvas.drawLine(stopX.toFloat(), stopY.toFloat(), x + doubleWaveSize.toFloat(), bottom.toFloat(), p)
            x += doubleWaveSize
        }
    }
}