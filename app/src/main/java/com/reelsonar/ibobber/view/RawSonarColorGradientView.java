package com.reelsonar.ibobber.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collections;
import java.util.List;

/**
 * @author Brian Gebala
 * @version 2/16/16
 */
public class RawSonarColorGradientView extends View {
    private List<Integer> _colors = Collections.emptyList();
    private Paint _paint;

    public RawSonarColorGradientView(final Context context) {
        super(context);
        init();
    }

    public RawSonarColorGradientView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RawSonarColorGradientView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        _paint = new Paint();
    }

    public void setColors(final List<Integer> colors) {
        _colors = colors;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float colorWidth = width / _colors.size();
        float height = getHeight();

        for (int i = 0; i < _colors.size(); i++) {
            float left = (i * colorWidth);
            int color = _colors.get(i);

            _paint.setColor(color);
            canvas.drawRect(left, 0.f, left + colorWidth, height, _paint);
        }
    }
}
