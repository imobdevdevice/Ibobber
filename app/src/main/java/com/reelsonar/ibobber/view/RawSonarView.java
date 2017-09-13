package com.reelsonar.ibobber.view;

import android.content.Context;
import android.widget.RelativeLayout;
import com.reelsonar.ibobber.dsp.DspConstants;
import com.reelsonar.ibobber.model.FishSonarData;
import com.reelsonar.ibobber.model.RawSonarColors;
import com.reelsonar.ibobber.model.SonarData;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.util.MathUtil;

import java.util.*;

/**
 * @author Brian Gebala
 * @version 2/18/16
 */
public class RawSonarView extends RelativeLayout {

    private int _bobberMaxAmplitude;
    private double _lakeFloorDepth;
    private int _maxDataFrames;
    private boolean _depthMeterMaxChanged;
    private float _bottomMargin;
    private RawSonarColors _rawSonarColors = SonarViewUtil.RAW_SONAR_COLORS;
    private List<RawSonarFrameView> _rawSonarFrameViews = Collections.emptyList();
    private List<SonarData> _sonarData = Collections.emptyList();

    public RawSonarView(final Context context) {
        super(context);
    }

    public void init(final int maxDataFrames, final float bottomMargin) {
        _maxDataFrames = maxDataFrames;
        _bottomMargin = bottomMargin;
        _rawSonarFrameViews = new ArrayList<>(_maxDataFrames);

        for (int i = 0; i < _maxDataFrames; i++) {
            RawSonarFrameView rawSonarFrameView = new RawSonarFrameView(getContext());
            _rawSonarFrameViews.add(rawSonarFrameView);
            addView(rawSonarFrameView);
        }

        if (UserService.getInstance(getContext()).getAntiGlare()) {
            setBackgroundColor(getResources().getColor(android.R.color.black));
        }
        else {
            setBackgroundColor(SonarViewUtil.RAW_SONAR_BG_COLOR);
        }
    }

    private float getYForDepth(final float depth) {
        return (getPixelsPerUnitOfMeasurement() * depth);
    }

    private float getPixelsPerUnitOfMeasurement() {
        return ((float)getHeight() - _bottomMargin) / (float)_lakeFloorDepth;
    }

    public void setData(final LinkedList<SonarData> sonarData,
                        final double lakeFloorDepth,
                        final boolean depthMeterMaxChanged) {
        _sonarData = sonarData;
        _lakeFloorDepth = lakeFloorDepth;
        _depthMeterMaxChanged = depthMeterMaxChanged;
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        float dataFrameWidth = (float)getWidth() / (float)_maxDataFrames;
        float height = getHeight();

        int maxBottomPingIndex = 0;

        for (int i = 0; i < _sonarData.size() && i < _maxDataFrames; i++) {
            SonarData sonarData = _sonarData.get(i);

            if (!isValidRawData(sonarData)) {
                continue;
            }

            int bottomPingIndex = sonarData.getRawSonarPingDataProcessor().getBottom().getLocation();
            maxBottomPingIndex = Math.max(bottomPingIndex, maxBottomPingIndex);
        }

        // Some shallow bottom peaks are only a few amplitude positions into a ping array. In order
        // to make the blocks smaller, and thus the display more useful and interesting, use
        // `PING_SIZE/2` for `numOfBlocks` when calculating `blockHeight`.
        int numOfBlocks = Math.max(maxBottomPingIndex, DspConstants.PING_SIZE / 2);
        float blockHeight = (height - _bottomMargin) / numOfBlocks;

        // If _depthMeterMaxDepth has not changed, we can re-use the previously drawn RawSonarFrameView instances.
        // Shift the contents of `rawDataFrameViews` to the right by a single position. These views will only have
        // their frame set, but not be redrawn. The new head will be drawn to render the new SonarData that
        // just arrived.
        if (!_depthMeterMaxChanged) {
            RawSonarFrameView newHead = _rawSonarFrameViews.get(_rawSonarFrameViews.size() - 1);

            for (int i = _rawSonarFrameViews.size() - 1; i > 0; i--) {
                _rawSonarFrameViews.set(i, _rawSonarFrameViews.get(i - 1));
            }

            _rawSonarFrameViews.set(0, newHead);
        }

        for (int i = 0; i < _maxDataFrames; i++)  {
            RawSonarFrameView rawSonarFrameView = _rawSonarFrameViews.get(i);
            float dataFrameX = (dataFrameWidth * (float)(_maxDataFrames - 1 - i));

            if (i < _sonarData.size()) {
                SonarData sonarData = _sonarData.get(i);

                if (!isValidRawData(sonarData)) { continue; }

                if (sonarData.getBobberMaxAmplitude() != _bobberMaxAmplitude) {
                    _bobberMaxAmplitude = sonarData.getBobberMaxAmplitude();
                    _rawSonarColors = new RawSonarColors(_bobberMaxAmplitude, SonarViewUtil.RAW_SONAR_NUM_COLORS);
                }

                if (i == 0 || _depthMeterMaxChanged) {
                    Map<Integer, Float> fishAmplitudeLocations = new HashMap<>();

                    for (FishSonarData fishData : sonarData.getFish()) {
                        if (!SonarViewUtil.shouldPlotFish(fishData, sonarData)) {
                            continue;
                        }

                        double depth = MathUtil.metersToUnitOfMeasure(fishData.getDepthMeters(), getContext());
                        float fishLocationY = getYForDepth((float)depth);
                        fishAmplitudeLocations.put(fishData.getAmplitude(), fishLocationY);
                    }

                    double depth = MathUtil.metersToUnitOfMeasure(sonarData.getDepthMeters(), getContext());
                    float depthMeterOffsetY = getYForDepth((float)depth);
                    float rawDataFrameBottomOffset = depthMeterOffsetY
                            - (blockHeight * sonarData.getRawSonarPingDataProcessor().getBottom().getLocation());

                    rawSonarFrameView.drawRawSonarData(
                            sonarData,
                            fishAmplitudeLocations,
                            _rawSonarColors,
                            rawDataFrameBottomOffset,
                            blockHeight
                    );
                }
            }
            else {
                rawSonarFrameView.drawNoRawSonarData();
            }

            rawSonarFrameView.layout((int)(dataFrameX), 0, (int)(dataFrameX) + (int)(dataFrameWidth + 0.5), (int)height);
        }
    }

    private boolean isValidRawData(final SonarData sonarData) {
        return sonarData != null
                && sonarData.getRawSonarPingDataProcessor() != null
                && sonarData.getRawSonarPingDataProcessor().getBottom() != null;
    }
}
