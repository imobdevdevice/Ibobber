// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class AxisView extends View {

    private static final float MAJOR_TIC_LENGTH = 30;
    private static final float _MAJOR_TIC_LENGTH = -30;
    private static final float MINOR_TIC_LENGTH = 20;

    private int _numOfTicks = 10;
    private int _widthOverride = 0;
    private int _maxValue = 0;
    private int _prevMaxValue = 0;

    private float _pxPerDip = getContext().getResources().getDisplayMetrics().density;


    public AxisView(Context context) {
        super(context);
    }

    public AxisView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AxisView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getHeight() > getWidth()) {
            drawVerticalAxis(canvas);
        } else {
            drawHorizontalAxis(canvas);

        }
    }

    public int getNumOfTicks() {
        return _numOfTicks;
    }

    public void setNumOfTicks(int numOfTicks) {
        if (numOfTicks != _numOfTicks) {
            invalidate();
        }
        _numOfTicks = numOfTicks;
    }

    public int getMaxValue() {
        return _maxValue;
    }

    public void setMaxValue(int maxValue) {
        if (maxValue != _maxValue) {
            invalidate();
        }

        _prevMaxValue = _maxValue;
        _maxValue = maxValue;
    }

    public int getPrevMaxValue() {
        return _prevMaxValue;
    }

    public int getWidthOverride() {
        return _widthOverride;
    }

    public void setWidthOverride(int widthOverride) {
        if (widthOverride != _widthOverride) {
            invalidate();
        }
        _widthOverride = widthOverride;
    }

    private void drawVerticalAxis(Canvas canvas) {
        float width = getWidth(), height = getHeight();
        float axisX = width - 1.f;

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(13.0f * _pxPerDip);
        paint.setFakeBoldText(true);

        Rect textBounds = new Rect();
        Log.d("axis", " == " + axisX + " -- " + height);
        Path path = new Path();
        path.moveTo(axisX, 0);
        path.lineTo(axisX, height);

        float distanceBetweenTicks = (float) _maxValue / (float) _numOfTicks;
        float majorTicX = axisX - MAJOR_TIC_LENGTH;
        float minorTicX = axisX - MINOR_TIC_LENGTH;

        float ticY = 0.f;
        float ticSpace = ((height - 1.f) / (float) _numOfTicks) / 2.f; // height - 1 to account for the 0th tic.

        for (int i = 0; i <= _numOfTicks; ++i) {
            path.moveTo(majorTicX, ticY);
            path.lineTo(axisX, ticY);

            if (_maxValue > 0) {
                int depth = Math.round(i * distanceBetweenTicks);
                String depthText = String.valueOf(depth);
                paint.getTextBounds(depthText, 0, depthText.length(), textBounds);

                float textY = ticY;
                if (i == 0) {
                    textY += textBounds.height();
                } else if (i == _numOfTicks) {
                    textY -= textBounds.height() / 4.f;
                } else {
                    textY += textBounds.height() / 2.f;
                }
                canvas.drawText(depthText, ((majorTicX - textBounds.width()) / 2.f), textY, paint);
            }

            ticY += ticSpace;

            if (i + 1 <= _numOfTicks) {
                path.moveTo(minorTicX, ticY);
                path.lineTo(axisX, ticY);

                ticY += ticSpace;
            }
        }

        canvas.drawPath(path, paint);
    }

    private void drawHorizontalAxis(Canvas canvas) {
        float width = Math.max(getWidth(), _widthOverride);
        float visibleWidth = Math.min(getWidth(), _widthOverride);

//        Paint tempPaint = new Paint();
//        tempPaint.setColor(Color.RED);
//        canvas.drawRect(0, 0, getWidth(), getHeight(), tempPaint);

        Paint paint = new Paint();

        paint.setColor(Color.WHITE);

        //        if (UserService.SUNLIGHT_TEST) {
        //            paint.setColor(Color.BLACK);
        //        } else {
        //            paint.setColor(Color.WHITE);
        //        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(13.0f * _pxPerDip);
        paint.setFakeBoldText(true);
        paint.setStrokeWidth(6f);
        Rect textBounds = new Rect();
        Path path = new Path();
        path.moveTo(0, getHeight() / 2);
        path.lineTo(width, getHeight() / 2);

        canvas.drawPath(path, paint);
        path.reset();

        float distanceBetweenTicks = (float) _maxValue / (float) _numOfTicks;

        float ticX = 0.f;
        float ticSpace = ((width - 1.f) / (float) _numOfTicks) / 2.f; // width - 1 to account for the 0th tic.
        float initialHeight = getHeight() / 2;
        for (int i = 0; i <= _numOfTicks; ++i) {
            // For vertical Line
            path.moveTo(ticX, initialHeight);
            path.lineTo(ticX, initialHeight + MAJOR_TIC_LENGTH);
            canvas.drawPath(path, paint);
            path.reset();

            if (_maxValue > 0) {
                int depth = Math.round(i * distanceBetweenTicks);
                String depthText = String.valueOf(depth);
                paint.getTextBounds(depthText, 0, depthText.length(), textBounds);

                if (ticX < width) {
                    float textX = ticX;
                    if (textX + textBounds.width() > width) {
                        textX -= textBounds.width() + 6.f;
                    } else if (i > 0) {
                        textX -= (textBounds.width() / 2.f);
                    }
                    canvas.drawText(depthText, textX, initialHeight + MAJOR_TIC_LENGTH + textBounds.height() + 6.f, paint);
                }
            }

            ticX += ticSpace;

            if (i + 1 <= _numOfTicks) {
                path.moveTo(ticX, initialHeight);
                path.lineTo(ticX, initialHeight + MINOR_TIC_LENGTH);
                canvas.drawPath(path, paint);
                path.reset();

                ticX += ticSpace;
            }
        }

    }
}