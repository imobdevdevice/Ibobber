// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model;

import java.util.List;

public class SonarData {

    private List<FishSonarData> _fish;
    private PingDataProcessor _rawSonarPingDataProcessor;
    private double _depthMeters;
    private double _seaweedHeightMeters;
    private int _index;
    private int _bobberMaxAmplitude;

    public SonarData(final List<FishSonarData> fish, final PingDataProcessor rawSonarPingDataProcessor,
                     final double depthMeters, final int index, final int bobberMaxAmplitude) {
        _fish = fish;
        _rawSonarPingDataProcessor = rawSonarPingDataProcessor;
        _depthMeters = depthMeters;
        _index = index;
        _bobberMaxAmplitude = bobberMaxAmplitude;
    }

    public List<FishSonarData> getFish() {
        return _fish;
    }

    public PingDataProcessor getRawSonarPingDataProcessor() {
        return _rawSonarPingDataProcessor;
    }

    public void setFish(List<FishSonarData> fish) {
        _fish = fish;
    }

    public double getDepthMeters() {
        return _depthMeters;
    }

    public int getIndex() {
        return _index;
    }

    public int getBobberMaxAmplitude() {
        return _bobberMaxAmplitude;
    }

    public double getSeaweedHeightMeters() {
        return _seaweedHeightMeters;
    }

    public void setSeaweedHeightMeters(double seaweedHeightMeters) {
        _seaweedHeightMeters = seaweedHeightMeters;
    }
}
