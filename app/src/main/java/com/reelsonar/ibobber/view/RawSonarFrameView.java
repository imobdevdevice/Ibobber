package com.reelsonar.ibobber.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.reelsonar.ibobber.model.RawSonarColors;
import com.reelsonar.ibobber.model.SonarData;

import java.util.Map;

/**
 * @author Brian Gebala
 * @version 2/15/16
 */
public class RawSonarFrameView extends View {
    private final static int MAX_FISH = 10;

    private float _pxPerDip = getContext().getResources().getDisplayMetrics().density;
    private float _fishHeight = 4 * _pxPerDip;
    private float _halfFishHeight = _fishHeight / 2.f;

    private Paint _paint = new Paint();
    private SonarData _sonarData;
    private RawSonarColors _rawSonarColors = SonarViewUtil.RAW_SONAR_COLORS;
    private Map<Integer, Float> _fishLocationAmplitudes;
    private float _bottomOffset;
    private float _blockHeight;

    public RawSonarFrameView(final Context context) {
        super(context);
    }

    public RawSonarFrameView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public RawSonarFrameView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void drawRawSonarData(final SonarData sonarData,
                                 final Map<Integer, Float> fishLocationAmplitudes,
                                 final RawSonarColors rawSonarColors,
                                 final float bottomOffset,
                                 final float blockHeight) {
        _sonarData = sonarData;
        _fishLocationAmplitudes = fishLocationAmplitudes;
        _rawSonarColors = rawSonarColors;
        _bottomOffset = bottomOffset;
        _blockHeight = blockHeight;

        invalidate();
    }

    public void drawNoRawSonarData() {
        _sonarData = null;
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (_sonarData != null) {
            int[] pings = _sonarData.getRawSonarPingDataProcessor().getPingAmplitudes().buffer();
            int bottomIndex = _sonarData.getRawSonarPingDataProcessor().getBottom().getLocation();
            float blockBottomY = _bottomOffset;

            if (pings.length > 0) {
                boolean startDrawing = false;
                int blockColor = 0;
                float blockTopY = 0;

                for (int i = 0; i < pings.length; i++) {
                    if (i >= bottomIndex && blockBottomY < height) {
                        int amplitude = pings[i];
                        int color = _rawSonarColors.getColorForAmplitude(amplitude);

                        if (!startDrawing) {
                            startDrawing = true;
                            blockColor = color;
                            blockTopY = blockBottomY;
                        }
                        else if (color != blockColor) {
                            _paint.setColor(blockColor);
                            canvas.drawRect(0, blockTopY, width, blockBottomY, _paint);
                            blockColor = color;
                            blockTopY = blockBottomY;
                        }
                    }

                    blockBottomY += _blockHeight;

                    if (blockBottomY > height) {
                        break;
                    }
                }

                if (startDrawing) {
                    _paint.setColor(blockColor);
                    canvas.drawRect(0, blockTopY, width, blockBottomY, _paint);
                }

                int fishCount = 0;

                for (Map.Entry<Integer, Float> entry : _fishLocationAmplitudes.entrySet()) {
                    int amplitude = entry.getKey();
                    float fishLocationY = entry.getValue();
                    int color = _rawSonarColors.getColorForAmplitude(amplitude);

                    _paint.setColor(color);
                    canvas.drawRect(0.f, fishLocationY - _halfFishHeight, width, fishLocationY + _fishHeight, _paint);

                    fishCount++;

                    if (fishCount >= MAX_FISH) {
                        break;
                    }
                }
            }
        }
    }
}
