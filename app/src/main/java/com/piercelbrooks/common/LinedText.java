
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

public abstract class LinedText extends Text
{
    private static final String TAG = "PLB-LinedText";

    private Rect rectangle;
    private Paint paint;

    protected abstract @ColorRes int getColor();

    public LinedText(Context context)
    {
        super(context);
    }

    public LinedText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LinedText(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    protected void onInitialize(Context context)
    {
        Log.d(TAG, "onInitialize "+Utilities.getIdentifier(this));
        rectangle = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(ContextCompat.getColor(context, getColor()));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int height = getHeight();
        int line_height = getLineHeight();
        int count = height / line_height;

        if (getLineCount() > count)
        {
            count = getLineCount();
        }

        Rect rectangle = this.rectangle;
        Paint paint = this.paint;
        int baseline = getLineBounds(0, rectangle);

        for (int i = 0; i != count; ++i)
        {
            canvas.drawLine(rectangle.left, baseline + 1, rectangle.right, baseline + 1, paint);
            baseline += getLineHeight();
        }

        super.onDraw(canvas);
    }
}
