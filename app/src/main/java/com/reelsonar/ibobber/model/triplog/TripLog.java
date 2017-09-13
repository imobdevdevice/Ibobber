// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model.triplog;

import com.reelsonar.ibobber.form.IdName;

import java.util.Date;

public class TripLog {

    public static <E extends Enum & IdName> E constantFromId(final Class<E> enumClass,
                                                             final int id) {
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.getId() == id) {
                return constant;
            }
        }
        return null;
    }

    private long idTrip;
    private Date date;
    private double latitude, longitude;
    private String title, notes;
    private String lure;
    private LureType lureType;
    private Condition condition;
    private FishingType fishingType;
    private double waterTemp;
    private double waterDepth;
    private double airTemp;
    private long netFishId;

    public TripLog() {
        this.idTrip = -1;
        this.date = new Date();
    }

    public TripLog(final long idTrip, final Date date, final String title) {
        this.idTrip = idTrip;
        this.date = date;
        this.title = title;
    }

    public TripLog(long idTrip,
                   Date date,
                   double longitude,
                   double latitude,
                   String title,
                   String lure,
                   LureType lureType,
                   Condition condition,
                   FishingType fishingType,
                   String notes,
                   double waterTemp,
                   double waterDepth,
                   double airTemp) {
        this.idTrip = idTrip;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
        this.title = title;
        this.lure = lure;
        this.lureType = lureType;
        this.condition = condition;
        this.fishingType = fishingType;
        this.notes = notes;
        this.waterTemp = waterTemp;
        this.waterDepth = waterDepth;
        this.airTemp = airTemp;
    }

    public long getIdTrip() {
        return idTrip;
    }

    public void setIdTrip(long idTrip) {
        this.idTrip = idTrip;
    }

    public Date getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLure() {
        return lure;
    }

    public void setLure(String lure) {
        this.lure = lure;
    }

    public LureType getLureType() {
        return lureType;
    }

    public void setLureType(LureType lureType) {
        this.lureType = lureType;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public FishingType getFishingType() {
        return fishingType;
    }

    public void setFishingType(FishingType fishingType) {
        this.fishingType = fishingType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getAirTemp() {
        return airTemp;
    }

    public double getWaterTemp() {
        return waterTemp;
    }

    public void setWaterTemp(double waterTemp) {
        this.waterTemp = waterTemp;
    }

    public double getWaterDepth() {
        return waterDepth;
    }

    public void setWaterDepth(double waterDepth) {
        this.waterDepth = waterDepth;
    }

    public void setAirTemp(double airTemp) {
        this.airTemp = airTemp;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getNetFishId() {
        return netFishId;
    }

    public void setNetFishId(long netFishId) {
        this.netFishId = netFishId;
    }
}
