package com.reelsonar.ibobber.model;

import android.os.SystemClock;

/**
 * Created by james on 9/2/14.
 */
public class FishSonarData {

    private FishSize _size;
    private int _amplitude;
    private double _depthMeters;
    private long _timestamp;

    public FishSonarData(final FishSize size, final int amplitude, final double depthMeters) {
        this(size, amplitude, depthMeters, SystemClock.uptimeMillis());
    }

    public FishSonarData(final FishSize size, final int amplitude, final double depthMeters, final long timestamp) {
        _size = size;
        _amplitude = amplitude;
        _depthMeters = depthMeters;
        _timestamp = timestamp;
    }

    public FishSize getSize() {
        return _size;
    }

    public int getAmplitude() {
        return _amplitude;
    }

    public double getDepthMeters() {
        return _depthMeters;
    }

    public long getTimestamp() {
        return _timestamp;
    }
}
