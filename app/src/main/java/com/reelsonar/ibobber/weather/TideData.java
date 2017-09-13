// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.weather;

import java.util.Date;

public class TideData {

    private Number mHighTide = null;
    private Number mLowTide = null;
    private Date mHighTideDate = null;
    private Date mLowTideDate = null;

    TideData() { }

    public Date getmLowTideDate() {
        return mLowTideDate;
    }

    public Date getmHighTideDate() {
        return mHighTideDate;
    }

    public Number getmLowTide() {
        return mLowTide;
    }

    public Number getmHighTide() {
        return mHighTide;
    }
    void setmLowTideDate(Date mLowTideDate) {
        this.mLowTideDate = mLowTideDate;
    }

    void setmHighTideDate(Date mHighTideDate) {
        this.mHighTideDate = mHighTideDate;
    }

    void setmLowTide(Number mLowTide) {
        this.mLowTide = mLowTide;
    }

    void setmHighTide(Number mHighTide) {
        this.mHighTide = mHighTide;
    }
}
