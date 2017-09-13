package com.reelsonar.ibobber.view;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.reelsonar.ibobber.R;

class DividerViewController {

    private int dividerSize = 3, dividerColor = Color.BLACK, dividerDirection;

    private int focusedColor;
    private int currentColor;

    private Rect rect = new Rect();
    private Paint paint = new Paint();
    private int height, width;


    void init(TypedArray a) {

        if (a != null) {
            dividerColor = a.getColor(R.styleable.DividerTextView_dividerColor, Color.BLACK);
            dividerSize = a.getDimensionPixelSize(R.styleable.DividerTextView_dividerSize, 3);
            dividerDirection = a.getInt(R.styleable.DividerTextView_dividerDirection, 0);
            currentColor = dividerColor;
            if (a.hasValue(R.styleable.DividerTextView_dividerColorHighlight)) {
                focusedColor = a.getColor(R.styleable.DividerTextView_dividerColorHighlight, Color.BLACK);
            } else
                focusedColor = dividerColor;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(dividerColor);
        paint.setStrokeWidth(dividerSize);
    }

/*
    public int alterColor(int color) {
        float factor = 0.5f;
        int r = Color.red(color);
        int b = Color.blue(color);
        int g = Color.green(color);

        return Color.rgb((int) (r * factor), (int) (g * factor), (int) (b * factor));
    }*/


    void onDraw(Canvas canvas) {
        paint.setColor(currentColor);
        if (dividerDirection == 12) {
            canvas.drawRect(0, 0, rect.right, rect.bottom, paint);
        } else if (dividerDirection == 8) {
            canvas.drawLine(0, rect.bottom, rect.right, rect.bottom, paint);
        } else if (dividerDirection == 4) {
            canvas.drawLine(0, rect.top, rect.right, rect.top, paint);
        } else if (dividerDirection == 2) {
            canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
        } else if (dividerDirection == 1) {
            canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);
        }
    }

    void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        height = View.MeasureSpec.getSize(heightMeasureSpec);
        rect.set(0, 0, widthMeasureSpec, heightMeasureSpec);
    }

    void onFocusChange(boolean focus, View view) {
        if (focus)
            currentColor = focusedColor;
        else
            currentColor = dividerColor;

        view.invalidate();


    }

}
