package com.bopr.android.smailer.util.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bopr.android.smailer.R;

/**
 * Draws wavy line under the text.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class WavyUnderlineSpan implements LineBackgroundSpan {

    private int color;
    private int lineWidth;
    private int waveSize;

    public WavyUnderlineSpan(Context context) {
        this(ContextCompat.getColor(context, R.color.errorLine));
    }

    public WavyUnderlineSpan(int color) {
        this(color, 1, 4);
    }

    public WavyUnderlineSpan(int color, int lineWidth, int waveSize) {
        this.color = color;
        this.lineWidth = lineWidth;
        this.waveSize = waveSize;
    }

    @Override
    public void drawBackground(@NonNull Canvas canvas,@NonNull Paint paint, int left, int right, int top,
                               int baseline, int bottom,
                               @NonNull CharSequence text, int start, int end, int lnum) {
        Paint p = new Paint(paint);
        p.setColor(color);
        p.setStrokeWidth(lineWidth);

        int width = (int) paint.measureText(text, start, end);
        int doubleWaveSize = waveSize * 2;
        for (int x = left; x < left + width; x += doubleWaveSize) {
            int stopY = bottom - waveSize;
            int stopX = x + waveSize;
            canvas.drawLine(x, bottom, stopX, stopY, p);
            canvas.drawLine(stopX, stopY, x + doubleWaveSize, bottom, p);
        }
    }

}
