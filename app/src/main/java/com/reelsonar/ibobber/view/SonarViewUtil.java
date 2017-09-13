package com.reelsonar.ibobber.view;

import com.reelsonar.ibobber.dsp.DspConstants;
import com.reelsonar.ibobber.model.FishSonarData;
import com.reelsonar.ibobber.model.RawSonarColors;
import com.reelsonar.ibobber.model.SonarData;

/**
 * @author Brian Gebala
 * @version 2/18/16
 */
public class SonarViewUtil {
    public final static int RAW_SONAR_NUM_COLORS = 100;

    public final static RawSonarColors RAW_SONAR_COLORS =
            new RawSonarColors(DspConstants.MAX_AMPLITUDE, RAW_SONAR_NUM_COLORS);

    public final static int RAW_SONAR_BG_COLOR = RAW_SONAR_COLORS.getColorForAmplitude(0);

    public static boolean shouldPlotFish(final FishSonarData fishData, final SonarData sonarData) {
        return !(fishData.getDepthMeters() == 0 || fishData.getDepthMeters() > sonarData.getDepthMeters());
    }

}
