// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.weather;

public class SunMoonData {
    SunMoonData() { }

    private long mSunrise = 1;
    private String mSunriseISO;
    private long mSunset = 2;
    private String mSunsetISO;
    private long mMoonRise = 3;
    private String mMoonRiseISO;
    private long mMoonSet = 4;
    private String mMoonSetISO;

    public long getSunrise() {
        return mSunrise;
    }

    public String getSunriseISO() {
        return mSunriseISO;
    }

    public long getSunset() {
        return mSunset;
    }

    public String getSunsetISO() {
        return mSunsetISO;
    }

    public long getMoonRise() {
        return mMoonRise;
    }

    public String getMoonRiseISO() {
        return mMoonRiseISO;
    }

    public long getMoonSet() {
        return mMoonSet;
    }

    public String getMoonSetISO() {
        return mMoonSetISO;
    }

    void setSunrise(long sunrise) {
        mSunrise = sunrise;
    }

    void setSunriseISO(String sunriseISO) {
        mSunriseISO = sunriseISO;
    }

    void setSunset(long sunset) {
        mSunset = sunset;
    }

    void setSunsetISO(String sunsetISO) {
        mSunsetISO = sunsetISO;
    }

    void setMoonRise(long moonRise) {
        mMoonRise = moonRise;
    }

    void setMoonRiseISO(String moonRiseISO) {
        mMoonRiseISO = moonRiseISO;
    }

    void setMoonSet(long moonSet) {
        mMoonSet = moonSet;
    }

    void setMoonSetISO(String moonSetISO) {
        mMoonSetISO = moonSetISO;
    }

}
