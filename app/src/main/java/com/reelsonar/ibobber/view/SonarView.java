package com.reelsonar.ibobber.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.dsp.DspConstants;
import com.reelsonar.ibobber.model.FishSize;
import com.reelsonar.ibobber.model.FishSonarData;
import com.reelsonar.ibobber.model.RawSonarColors;
import com.reelsonar.ibobber.model.SonarData;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.util.MathUtil;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;


/**
 * View that shows the sonar data
 */
public class SonarView extends RelativeLayout {
    private final static String TAG = "SonarView";

    public final static double MAX_DEPTH_METERS = 41.148; // 135 ft

    private final static Random RANDOM = new Random();
    private final static LinkedList<SonarData> EMPTY_SONAR_DATA = new LinkedList<>();

    private LinkedList<SonarData> _sonarData;
    private double _lakeFloorDepth;
    private int _maxDataFrames;
    private boolean _plotFish;
    private boolean _showRawData;

    private Bitmap _smallFishBitmap;
    private Bitmap _largeFishBitmap;
    private BitmapShader[] _topographyPatternBitmaps;
    private Bitmap[] _seaweedBitmaps;

    private Paint _paint;
    private NewAxisView _newTestAxis;
    private AxisView _depthAxisView;
    private AxisView _distanceAxisView;
    private RawSonarColorGradientView _rawSonarColorGradientView;

    private RawSonarColors _rawSonarColors;
    private RawSonarView _rawSonarView;

    private int _smallFishWidth;
    private int _largeFishWidth;
    private float _pxPerDip = getContext().getResources().getDisplayMetrics().density;

    private int _axisWidth = (int) (47.0f * _pxPerDip);
    private int _topMargin = (int) (87.0f * _pxPerDip);
    private int _bottomMargin = (int) (20.0f * _pxPerDip);

    private int _depthLabelXOffset = (int) (27.0f * _pxPerDip);
    private int _depthLabelYOffset = (int) (76.0f * _pxPerDip);
    private int _rawColorsGradientHeight = (int) (25.0f * _pxPerDip);

    private boolean _showDepthLabel = true;

    public SonarView(Context context) {
        super(context);
        initView();
    }

    public SonarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public SonarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paint.setStrokeWidth(3);
        _paint.setAntiAlias(true);
        _paint.setDither(true);
        _paint.setFilterBitmap(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setTextSize(15 * _pxPerDip);
        _paint.setTextAlign(Paint.Align.CENTER);
        _paint.setColor(Color.WHITE);

        _maxDataFrames = 4;
        _plotFish = true;

        _smallFishBitmap = ((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.fish_large)).getBitmap();
        _smallFishWidth = _smallFishBitmap.getWidth();

        _largeFishBitmap = ((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.fish_xlarge)).getBitmap();
        _largeFishWidth = _largeFishBitmap.getWidth();

        updateGroundTextures();

        _seaweedBitmaps = new Bitmap[]{
                ((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.seaweed_70_1a)).getBitmap(),
                ((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.seaweed_70_2a)).getBitmap(),
                ((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.seaweed_70_1b)).getBitmap(),
                ((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.seaweed_70_2b)).getBitmap()
        };

        setGravity(Gravity.LEFT);

        _depthAxisView = new AxisView(getContext());
        _depthAxisView.setId(View.generateViewId());
        LayoutParams depthParams = new LayoutParams(_axisWidth, LayoutParams.MATCH_PARENT);
        depthParams.addRule(ALIGN_PARENT_LEFT);
        depthParams.topMargin = _topMargin;
        depthParams.bottomMargin = _bottomMargin;
        addView(_depthAxisView, depthParams);

        _distanceAxisView = new AxisView(getContext());
        _distanceAxisView.setId(View.generateViewId());
        _distanceAxisView.setVisibility(View.INVISIBLE);
        LayoutParams distanceParams = new LayoutParams(LayoutParams.WRAP_CONTENT, _bottomMargin + 1);
        distanceParams.addRule(RIGHT_OF, _depthAxisView.getId());
        distanceParams.addRule(ALIGN_PARENT_RIGHT);
        distanceParams.addRule(ALIGN_PARENT_BOTTOM);
        addView(_distanceAxisView, distanceParams);

        _newTestAxis = new NewAxisView(getContext());
        LayoutParams newdistanceParams = new LayoutParams(LayoutParams.WRAP_CONTENT, _bottomMargin + 1);
        _newTestAxis.setVisibility(View.INVISIBLE);
        newdistanceParams.addRule(ABOVE, _distanceAxisView.getId());
        newdistanceParams.addRule(ALIGN_START,_distanceAxisView.getId());
        addView(_newTestAxis, newdistanceParams);

        setWillNotDraw(false);
    }

    public void initRawSonar(final int maxDataFrames) {
        _showRawData = true;
        _maxDataFrames = maxDataFrames;

        _rawSonarColors = new RawSonarColors(DspConstants.MAX_AMPLITUDE, SonarViewUtil.RAW_SONAR_NUM_COLORS);
        _rawSonarColorGradientView = new RawSonarColorGradientView(getContext());
        _rawSonarColorGradientView.setColors(_rawSonarColors.getAllColors());

        LayoutParams rawColorGradientParams = new LayoutParams(LayoutParams.WRAP_CONTENT, _rawColorsGradientHeight);
        rawColorGradientParams.addRule(RIGHT_OF, _depthAxisView.getId());
        rawColorGradientParams.topMargin = _topMargin - _rawColorsGradientHeight;
        addView(_rawSonarColorGradientView, rawColorGradientParams);

        LayoutParams rawSonarLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        rawSonarLayoutParams.topMargin = _topMargin;
        rawSonarLayoutParams.addRule(RIGHT_OF, _depthAxisView.getId());
        rawSonarLayoutParams.addRule(ALIGN_PARENT_RIGHT);

        _rawSonarView = new RawSonarView(getContext());
        _rawSonarView.init(maxDataFrames, _bottomMargin);
        addView(_rawSonarView, rawSonarLayoutParams);
    }

    public void updateGroundTextures() {
        if (UserService.getInstance(BobberApp.getContext()).getAntiGlare()) {
            _topographyPatternBitmaps = new BitmapShader[]{
                    new BitmapShader(((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.daylight_pattern1)).getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT),
                    new BitmapShader(((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.daylight_pattern2)).getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT),
                    new BitmapShader(((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.daylight_pattern3)).getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            };
        } else {
            _topographyPatternBitmaps = new BitmapShader[]{
                    new BitmapShader(((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.pattern1)).getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT),
                    new BitmapShader(((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.pattern2)).getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT),
                    new BitmapShader(((BitmapDrawable) getContext().getResources().getDrawable(R.drawable.pattern3)).getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            };
        }
    }

    public AxisView getDepthAxisView() {
        return _depthAxisView;
    }
    public NewAxisView getNewAxisView() {
        return _newTestAxis;
    }
    public AxisView getDistanceAxisView() {
        return _distanceAxisView;
    }

    public double getLakeFloorDepth() {
        return _lakeFloorDepth;
    }

    public void setLakeFloorDepth(double lakeFloorDepth) {
        _lakeFloorDepth = lakeFloorDepth;
    }

    public LinkedList<SonarData> getSonarData() {
        return _sonarData;
    }

    public void setSonarData(LinkedList<SonarData> sonarData) {
        _sonarData = sonarData;
    }

    public int getMaxDataFrames() {
        return _maxDataFrames;
    }

    public void setMaxDataFrames(int maxDataFrames) {
        _maxDataFrames = maxDataFrames;
    }

    public boolean isPlotFish() {
        return _plotFish;
    }

    public void setPlotFish(boolean plotFish) {
        _plotFish = plotFish;
    }

    public boolean isShowRawData() {
        return _showRawData;
    }

    public void setShowRawData(boolean showRawData) {
        _showRawData = showRawData;
    }

    public void setShowDepthLabel(boolean showDepthLabel) {
        _showDepthLabel = showDepthLabel;
    }

    public int getBottomMargin() {
        return _bottomMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        _bottomMargin = (int) (bottomMargin * _pxPerDip);

        LayoutParams depthParams = (LayoutParams) _depthAxisView.getLayoutParams();
        depthParams.bottomMargin = _bottomMargin;
        _depthAxisView.setLayoutParams(depthParams);

        LayoutParams distanceParams = (LayoutParams) _distanceAxisView.getLayoutParams();
        distanceParams.height = _bottomMargin + 1;
        _distanceAxisView.setLayoutParams(distanceParams);
    }

    public void setTopMargin(int topMargin) {
        _topMargin = (int) (topMargin * _pxPerDip);

        LayoutParams depthParams = (LayoutParams) _depthAxisView.getLayoutParams();
        depthParams.topMargin = _topMargin;
        _depthAxisView.setLayoutParams(depthParams);
    }

    public void clear() {
        setLakeFloorDepth(0);
        setSonarData(EMPTY_SONAR_DATA);
        getDepthAxisView().setMaxValue(0);
        getDepthAxisView().setNumOfTicks(10);
        rawDataLayout();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!_showRawData) {
            plotTopography(canvas);

            if (_plotFish) {
                plotFish(canvas);
            }
        }

        if (_showDepthLabel) drawDepthLabel(canvas);
    }

    public void rawDataLayout() {
        if (!_showRawData) {
            return;
        }

        if (_sonarData == null || _sonarData.size() == 0) {
            _rawSonarView.setData(EMPTY_SONAR_DATA, 0, false);
        } else {
            boolean depthMeterMaxChanged = _depthAxisView.getMaxValue() != _depthAxisView.getPrevMaxValue();
            _rawSonarView.setData(_sonarData, _lakeFloorDepth, depthMeterMaxChanged);
        }

        _rawSonarView.requestLayout();
    }

    private int getDataFrameWidth() {
        return ((getWidth() - _axisWidth) / _maxDataFrames);
    }

    private int getDataFrameX(int index, int dataFrameWidth) {
        return _axisWidth + (dataFrameWidth * (_maxDataFrames - 1 - index));
    }

    private double getPixelsPerUnitOfMeasurement() {
        return (getHeight() - _topMargin - _bottomMargin) / _lakeFloorDepth;
    }

    private int getYForDepth(double depth) {
        return (int) (_topMargin + (getPixelsPerUnitOfMeasurement() * depth));
    }

    private void plotFish(Canvas c) {
        if (_sonarData == null || _sonarData.size() == 0) {
            return;
        }

        int dataFrameWidth = getDataFrameWidth();

        int i = 0;
        for (SonarData sonarData : _sonarData) {
            for (FishSonarData fishData : sonarData.getFish()) {
                if (!SonarViewUtil.shouldPlotFish(fishData, sonarData)) {
                    continue;
                }
                double fishDepth = MathUtil.metersToUnitOfMeasure(fishData.getDepthMeters(), getContext());

                int fishX = getDataFrameX(i, dataFrameWidth);
                int fishY = getYForDepth(fishDepth) - (int) (40.0f * _pxPerDip);

                if (fishData.getSize() == FishSize.XLARGE) {
                    c.drawBitmap(_largeFishBitmap, fishX, fishY, _paint);
                    c.drawText(MathUtil.getFishDepthText(fishDepth), fishX + _largeFishWidth - (int) (16.0f * _pxPerDip), fishY + (int) (20.0f * _pxPerDip), _paint);
                } else {
                    c.drawBitmap(_smallFishBitmap, fishX, fishY, _paint);
                    c.drawText(MathUtil.getFishDepthText(fishDepth), fishX + _smallFishWidth - (int) (16.0f * _pxPerDip), fishY + (int) (17.0f * _pxPerDip), _paint);
                }
            }

            ++i;
            if (i >= _maxDataFrames) {
                break;
            }
        }
    }

    private final static float VISUAL_CUE_OFFSET_STEP = 3;
    private final static float CONTROL_POINT_OFFSET_Y = 2;
    private final static float[] VISUAL_CUE_OFFSET_STEPS = {-VISUAL_CUE_OFFSET_STEP, 0, VISUAL_CUE_OFFSET_STEP};

    private int _topographyPatternIndex;
    private int _topograhyVisualCueOffsetIndex;

    private void plotTopography(Canvas c) {
        if (_sonarData == null || _sonarData.size() == 0) {
            return;
        }

        float dataFrameWidth = getDataFrameWidth();
        double prevDepth = MathUtil.metersToUnitOfMeasure(_sonarData.getFirst().getDepthMeters(), getContext());
        float startPointX = getWidth(), startPointY = getYForDepth(prevDepth);

        ListIterator<SonarData> sonarDataIterator = _sonarData.listIterator();
        while (sonarDataIterator.hasNext()) {
            int i = sonarDataIterator.nextIndex();
            SonarData sonarData = sonarDataIterator.next();

            double depth = MathUtil.metersToUnitOfMeasure(sonarData.getDepthMeters(), getContext());

            Path path = new Path();
            path.moveTo(startPointX, getHeight());
            path.lineTo(startPointX, startPointY);

            double visualCueOffset = VISUAL_CUE_OFFSET_STEPS[_topograhyVisualCueOffsetIndex];
            _topograhyVisualCueOffsetIndex = (_topograhyVisualCueOffsetIndex + 1) % VISUAL_CUE_OFFSET_STEPS.length;
            float endPointX = getDataFrameX(i, (int) dataFrameWidth), endPointY = getYForDepth(depth);
            float midPointX = (float) (endPointX + (dataFrameWidth / 2) + visualCueOffset), midPointY = endPointY;

            float leftCPX = (float) (endPointX + (dataFrameWidth * 0.6)), leftCPY = startPointY - CONTROL_POINT_OFFSET_Y;
            float rightCPX = (float) (endPointX + (dataFrameWidth * 0.8)), rightCPY = startPointY + CONTROL_POINT_OFFSET_Y;
            path.cubicTo(leftCPX, leftCPY, rightCPX, rightCPY, midPointX, midPointY);

            leftCPX = (float) (endPointX + (dataFrameWidth * 0.2));
            leftCPY = endPointY - CONTROL_POINT_OFFSET_Y;
            rightCPX = (float) (endPointX + (dataFrameWidth * 0.4));
            rightCPY = endPointY + CONTROL_POINT_OFFSET_Y;
            path.cubicTo(leftCPX, leftCPY, rightCPX, rightCPY, endPointX, endPointY);

            path.lineTo(endPointX, getHeight());
            path.close();

            if (sonarData.getSeaweedHeightMeters() > 0.0) {
                drawSeaweed(c, sonarData.getSeaweedHeightMeters(), prevDepth, depth, (int) endPointX);
            }

            Paint topographyPaint = new Paint();
            topographyPaint.setStyle(Paint.Style.FILL);
            topographyPaint.setShader(_topographyPatternBitmaps[_topographyPatternIndex]);
            _topographyPatternIndex = (_topographyPatternIndex + 1) % _topographyPatternBitmaps.length;

            c.drawPath(path, topographyPaint);

            startPointX = endPointX;
            startPointY = endPointY;
            prevDepth = depth;
        }
    }


    private void drawDepthLabel(Canvas canvas) {

        if (_sonarData != null) {
            if (_sonarData.size() > 0) {
                double depth = 0;
                SonarData lastSonarData = _sonarData.getFirst();
                depth = MathUtil.metersToUnitOfMeasure(lastSonarData.getDepthMeters(), getContext());

                String depthString = String.format("%.1f", depth);

                canvas.drawText(depthString, _depthLabelXOffset, _depthLabelYOffset, _paint);
            }
        }

    }

    private void drawSeaweed(Canvas canvas,
                             double heightMeters,
                             double depth,
                             double prevDepth,
                             int xPos) {
        float seaweedOffset = 4 * _pxPerDip;

        double pixelsPerUnitOfMeasurement = getPixelsPerUnitOfMeasurement();
        int dataFrameWidth = getDataFrameWidth();

        Bitmap seaweed1 = _seaweedBitmaps[RANDOM.nextInt(_seaweedBitmaps.length)];
        Bitmap seaweed2 = _seaweedBitmaps[RANDOM.nextInt(_seaweedBitmaps.length)];
        Rect seaweedRect = new Rect();

        int seaweedImageHeight = (int) (Math.max(pixelsPerUnitOfMeasurement * MathUtil.metersToUnitOfMeasure(heightMeters, getContext()), 40) * _pxPerDip);
        int smallFrameWidth = dataFrameWidth + (dataFrameWidth / 2);

        int offset1 = RANDOM.nextInt(12);
        int offset2 = RANDOM.nextInt(12);
        int width1 = RANDOM.nextInt(10);
        int width2 = RANDOM.nextInt(10);

        // We don't want this seaweed hanging off the edge. The topography
        // view creates a downward slope going from prevDepth down to depth.
        if (prevDepth >= depth) {
            int width = (int) (Math.min(30 + width1, smallFrameWidth) * _pxPerDip);
            int originX = xPos + (int) (offset1 * _pxPerDip);
            int originY = (int) seaweedOffset + getYForDepth(prevDepth) - seaweedImageHeight;

            seaweedRect.set(originX, originY, originX + width, originY + seaweedImageHeight);
            canvas.drawBitmap(seaweed1, null, seaweedRect, null);
        } else {

            int width = (int) (Math.min(30 + width2, smallFrameWidth) * _pxPerDip);
            int originX = xPos + (dataFrameWidth / 2) - (int) (offset2 * _pxPerDip);
            int originY = (int) seaweedOffset + getYForDepth(depth) - seaweedImageHeight;

            seaweedRect.set(originX, originY, originX + width, originY + seaweedImageHeight);
            canvas.drawBitmap(seaweed2, null, seaweedRect, null);
        }
    }
}