// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.weather;

public class WeatherData {
    WeatherData() { }

    private Number mTempF = 99;
    private Number mTempC = 40;
    private Number mWindSpeedMPH = 8;
    private Number mWindSpeedKPH = 8;
    private Number mWindDirection = 45;
    private Number mPressureIN = 30;
    private Number mPressureMB = 28;
    private Number mRainFall = 10;
    private String mIcon = "sunny";

    private Number mMaxTempF = 99;
    private Number mMaxTempC = 40;
    private Number mMinTempF = 79;
    private Number mMinTempC = 20;

    // Added Fall 2016
    private String mWeatherCode = "";
    private String mCloudCode = "";
    private String mCardinalWindDirection = "";


//    public String getCardinalWindDirection() {   // Deprecated Fall 2016
//        String windDir = " N";
//
//        if (mWindDirection != null) {
//            long winNum = mWindDirection.longValue();
//
//            if ( winNum >= 0 && winNum <= 22 ) windDir = "N ";
//            if ( winNum >= 23 && winNum <= 44 ) windDir = "NNE ";
//            if ( winNum >= 45 && winNum <= 66 ) windDir = "NE ";
//            if ( winNum >= 67 && winNum <= 88 ) windDir = "E ";
//            if ( winNum >= 89 && winNum <= 110 ) windDir = "ENE ";
//            if ( winNum >= 111 && winNum <= 132 ) windDir = "ESE ";
//            if ( winNum >= 133 && winNum <= 154 ) windDir = "SE ";
//            if ( winNum >= 155 && winNum <= 176 ) windDir = "SSE ";
//            if ( winNum >= 177 && winNum <= 198 ) windDir = "S ";
//            if ( winNum >= 199 && winNum <= 220 ) windDir = "SSW ";
//            if ( winNum >= 221 && winNum <= 242 ) windDir = "SW ";
//            if ( winNum >= 243 && winNum <= 264 ) windDir = "WSW ";
//            if ( winNum >= 265 && winNum <= 287 ) windDir = "W ";
//            if ( winNum >= 288 && winNum <= 309 ) windDir = "WNW ";
//            if ( winNum >= 310 && winNum <= 331 ) windDir = "NW ";
//            if ( winNum >= 332 && winNum <= 354 ) windDir = "NNW ";
//            if ( winNum >= 355 && winNum <= 360 ) windDir = "N ";
//        }
//
//        return (windDir);
//    }

    public Number getTempF() {
        return mTempF;
    }

    public Number getTempC() {
        return mTempC;
    }

    public Number getWindSpeedMPH() {
        return mWindSpeedMPH;
    }

    public Number getWindSpeedKPH() {
        return mWindSpeedKPH;
    }

    public Number getWindDirection() {
        return mWindDirection;
    }

    public Number getPressureIN() {
        return mPressureIN;
    }

    public Number getPressureMB() {
        return mPressureMB;
    }

    public Number getRainFall() {
        return mRainFall;
    }

    public String getIcon() {
        return mIcon;
    }

    public Number getMaxTempF() {
        return mMaxTempF;
    }

    public Number getMaxTempC() {
        return mMaxTempC;
    }

    public Number getMinTempF() {
        return mMinTempF;
    }

    public Number getMinTempC() {
        return mMinTempC;
    }

    public String getCloudCode() { return mCloudCode; }

    public String getWeatherCode() { return mWeatherCode; }

    public String getCardinalWindDirection() { return mCardinalWindDirection;}


    void setTempF(Number tempF) {
        mTempF = tempF;
    }

    void setTempC(Number tempC) {
        mTempC = tempC;
    }

    void setWindSpeedMPH(Number windSpeedMPH) {
        mWindSpeedMPH = windSpeedMPH;
    }

    void setWindSpeedKPH(Number windSpeedKPH) {
        mWindSpeedKPH = windSpeedKPH;
    }

    void setWindDirection(Number windDirection) {
        mWindDirection = windDirection;
    }

    void setPressureIN(Number pressureIN) {
        mPressureIN = pressureIN;
    }

    void setPressureMB(Number pressureMB) {
        mPressureMB = pressureMB;
    }

    void setRainFall(Number rainFall) {
        mRainFall = rainFall;
    }

    void setIcon(String icon) {
        mIcon = icon;
    }

    void setMaxTempF(Number maxTempF) {
        mMaxTempF = maxTempF;
    }

    void setMaxTempC(Number maxTempC) {
        mMaxTempC = maxTempC;
    }

    void setMinTempF(Number minTempF) {
        mMinTempF = minTempF;
    }

    void setMinTempC(Number minTempC) {
        mMinTempC = minTempC;
    }

    void setCloudCode(String cloudCode) { this.mCloudCode = cloudCode; }

    void setWeatherCode(String weatherCode) { this.mWeatherCode = weatherCode; }
    void setCardinalWindDirection( String windDirection ) {this.mCardinalWindDirection = windDirection; }
}
