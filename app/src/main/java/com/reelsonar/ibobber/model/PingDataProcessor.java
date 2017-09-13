// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model;

import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.util.GrowableIntArray;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;


public class PingDataProcessor
{
    public static class BobberOutOfWater{}
    public static class BobberOutOfWaterSoft{}  // Derived from low amplitude value of ping

    private static int IN_WATER_AMPLITUDE = 250;
    private static int FISH_MEDIAN_INDEX = 4;
    private static int FISH_DETECTION_AMPLITUDE_DELTA = 75;

    private static int BAD_BOBBER_THRESHOLD = 750; //if first ping response is over this - bobber has peak issue
    private static int DATAPOINTS_TO_IGNORE_FOR_BAD_BOBBER = 14;  //addresses issue with false positive amplitude peak on new bobbers

    private GrowableIntArray _pingAmplitudes;
    private GrowableIntArray _medianPingAmplitudes;
    private EchoPeak _bottom;
    private List<EchoPeak> _fishEchoPeaks;
    private GrowableIntArray _fishAmplitudeMedians;

    public PingDataProcessor(GrowableIntArray pingData) {
        _pingAmplitudes = pingData;
        processData();
    }

    public PingDataProcessor(int[] pingData) {
        this(new GrowableIntArray(pingData));
    }

    public GrowableIntArray getPingAmplitudes() {
        return _pingAmplitudes;
    }

    public GrowableIntArray getMedianPingAmplitudes() {
        return _medianPingAmplitudes;
    }

    public GrowableIntArray getFishAmplitudeMedians() {
        return _fishAmplitudeMedians;
    }

    public List<EchoPeak> getFishEchoPeaks() {
        return _fishEchoPeaks;
    }

    public EchoPeak getBottom() {
        return _bottom;
    }

    //if response include initial large value - skip first pings
    private int getDataPointsToSkip() {
        int pingsToSkip = 0;
        if (isPingFromBadBobber()) pingsToSkip = DATAPOINTS_TO_IGNORE_FOR_BAD_BOBBER;
        return pingsToSkip;
    }

    private boolean isPingFromBadBobber() {
        if (_pingAmplitudes != null && _pingAmplitudes.get(0) > BAD_BOBBER_THRESHOLD) return true;
        return false;
    }

    private void processData() {
        if (_pingAmplitudes.size() == 0) {
            return;
        }

        int maxAmplitude = 0;
        GrowableIntArray.IntInterator iter = _pingAmplitudes.intInterator();
        while (iter.hasNext()) {
            int i = iter.nextIndex();
            int amplitude = iter.next();

            //don't use potentially bad pings in max amp calculation
            if (amplitude > maxAmplitude && i >= getDataPointsToSkip()) {
                maxAmplitude = amplitude;
            }
        }

        if (maxAmplitude >= IN_WATER_AMPLITUDE) {
            int depthAmplitudeThreshold = maxAmplitude / 2;

            iter = _pingAmplitudes.intInterator();
            while (iter.hasNext()) {
                int i = iter.nextIndex();
                int amplitude = iter.next();
                //don't use potentially bad pings in depth calculation
                if (amplitude >= depthAmplitudeThreshold && i >= getDataPointsToSkip()) {
                    _bottom = new EchoPeak(i, amplitude, EchoPeak.Type.BOTTOM, isPingFromBadBobber());
                    _fishEchoPeaks = new ArrayList<>();
                    _fishAmplitudeMedians = new GrowableIntArray();
                    break;
                }
            }

            if( !BTService.getSingleInstance().hasWaterDetection() )  // This bobber device doesn't have hardware water detection, so set this value based on our algorithmic (soft) mechanism
                BTService.getSingleInstance().setWaterDetected(1);

        } else {
            EventBus.getDefault().post(new BobberOutOfWaterSoft());

            if( !BTService.getSingleInstance().hasWaterDetection() )  // This bobber device doesn't have hardware water detection, so set this value based on our algorithmic (soft) mechanism
                BTService.getSingleInstance().setWaterDetected(0);
        }

        if (_bottom != null) {
            _medianPingAmplitudes = applyMedianFilterToPingData(_pingAmplitudes.size());

            GrowableIntArray medianAmplitudes = applyMedianFilterToPingData(_bottom.getLocation());
            GrowableIntArray detections = findDetectionsInPingDataWithMedians(medianAmplitudes);
            GrowableIntArray fishIndices = findFishInDetections(detections);

            iter = fishIndices.intInterator();
            while (iter.hasNext()) {
                int fishIndex = iter.next();
                int fishAmplitude = _pingAmplitudes.get(fishIndex);
                int medianAmplitude = medianAmplitudes.get(fishIndex);
                EchoPeak echoPeak = new EchoPeak(fishIndex, fishAmplitude, EchoPeak.Type.FISH, isPingFromBadBobber());

                if (fishAmplitude >= medianAmplitude + 125) {
                    echoPeak.setFishSize(FishSize.XLARGE);
                } else {
                    echoPeak.setFishSize(FishSize.LARGE);
                }

                _fishEchoPeaks.add(echoPeak);
                _fishAmplitudeMedians.add(medianAmplitude);
            }
        }
    }

    private GrowableIntArray applyMedianFilterToPingData(final int count) {
        GrowableIntArray medianAmplitudes = new GrowableIntArray(count);
        int pingDataCount = _pingAmplitudes.size();

        for (int i = 0; i < count; i++) {
            int subArrayStartIndex = i - FISH_MEDIAN_INDEX;
            int subArrayEndIndex = i + FISH_MEDIAN_INDEX;

            if (i < FISH_MEDIAN_INDEX) {
                subArrayStartIndex = 0;
                subArrayEndIndex = i + FISH_MEDIAN_INDEX;
            }
            else if (i + FISH_MEDIAN_INDEX >= pingDataCount) {
                subArrayEndIndex = pingDataCount - 1;
            }

            int medianAmplitude = findMedian(_pingAmplitudes.subArray(subArrayStartIndex, subArrayEndIndex + 1));
            medianAmplitudes.add(medianAmplitude);
        }

        return medianAmplitudes;
    }

    private GrowableIntArray findFishInDetections(GrowableIntArray detections) {
        GrowableIntArray fishIndices = new GrowableIntArray();

        if (detections.size() < 2) {
            return fishIndices;
        }

        // Pad detections with a trailing INT_MAX so the loop below can "look back"
        // at the last value by being one position beyond it.
        GrowableIntArray detectionsWithEndPad = new GrowableIntArray(detections);
        detectionsWithEndPad.add(Integer.MAX_VALUE);

        int clusterStartIndex = 0;

        for (int i = 1; i < detectionsWithEndPad.size(); ++i) {
            int index = detectionsWithEndPad.get(i);
            int priorIndex = detectionsWithEndPad.get(i - 1);

            if (priorIndex + 1 != index) {
                int detectionIndex, fishIndex;

                if (clusterStartIndex + 1 != i) {
                    // Get the detection in the middle (or one beyond for an array of even length).
                    detectionIndex = clusterStartIndex + ((i - clusterStartIndex) / 2);
                }
                else {
                    detectionIndex = clusterStartIndex;
                }

                fishIndex = detectionsWithEndPad.get(detectionIndex);
                fishIndices.add(fishIndex);

                clusterStartIndex = i;
            }
        }

        return fishIndices;
    }

    private GrowableIntArray findDetectionsInPingDataWithMedians(GrowableIntArray medians) {
        GrowableIntArray detections = new GrowableIntArray();

        for (int i = 0; i < _bottom.getLocation(); ++i) {
            int amplitude = _pingAmplitudes.get(i);
            int medianAmplitude = medians.get(i);

            if (amplitude >= medianAmplitude + FISH_DETECTION_AMPLITUDE_DELTA) {
                detections.add(i);
            }
        }

        return detections;
    }

    private static int findMedian(GrowableIntArray pings) {
        pings.sort();

        if (pings.size() == 0) {
            return 0;
        } else if (pings.size() == 1) {
            return pings.get(0);
        } else if (pings.size() % 2 == 0) {
            int middleIndex = pings.size() / 2;
            int val1 = pings.get(middleIndex);
            int val2 = pings.get(middleIndex - 1);
            return (val1 + val2) / 2;
        } else {
            int middleIndex = pings.size() / 2;
            return pings.get(middleIndex);
        }
    }

}
