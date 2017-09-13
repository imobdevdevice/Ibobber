package com.reelsonar.ibobber.model.triplog;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Singh
 */

public class TripLogDetails {

    @SerializedName("catch_id")
    @Expose
    private String catchId;
    @SerializedName("catch_species_id")
    @Expose
    private String catchSpeciesId;
    @SerializedName("catch_lure")
    @Expose
    private String catchLure;
    @SerializedName("catch_reel")
    @Expose
    private String catchReel;
    @SerializedName("catch_rod")
    @Expose
    private String catchRod;
    @SerializedName("catch_bait")
    @Expose
    private String catchBait;
    @SerializedName("catch_location")
    @Expose
    private String catchLocation;
    @SerializedName("catch_latitude")
    @Expose
    private String catchLatitude;
    @SerializedName("catch_longitude")
    @Expose
    private String catchLongitude;
    @SerializedName("catch_lake_name")
    @Expose
    private String catchLakeName;
    @SerializedName("catch_length")
    @Expose
    private String catchLength;
    @SerializedName("catch_weight")
    @Expose
    private String catchWeight;
    @SerializedName("catch_description")
    @Expose
    private String catchDescription;
    @SerializedName("image_count")
    @Expose
    private String imageCount;
    @SerializedName("images")
    @Expose
    private List<Object> images = null;
    @SerializedName("catch_created_at")
    @Expose
    private String catchCreatedAt;
    @SerializedName("catch_title")
    @Expose
    private String catchTitle;
    @SerializedName("catch_h2o")
    @Expose
    private String catchH2o;
    @SerializedName("catch_depth")
    @Expose
    private String catchDepth;
    @SerializedName("catch_temperature")
    @Expose
    private String catchTemperature;
    @SerializedName("catch_privacy")
    @Expose
    private String catchPrivacy;
    @SerializedName("catch_timestamp")
    @Expose
    private Integer catchTimestamp;

    public String getCatchId() {
        return catchId;
    }

    public void setCatchId(String catchId) {
        this.catchId = catchId;
    }

    public String getCatchSpeciesId() {
        return catchSpeciesId;
    }

    public void setCatchSpeciesId(String catchSpeciesId) {
        this.catchSpeciesId = catchSpeciesId;
    }

    public String getCatchLure() {
        return catchLure;
    }

    public void setCatchLure(String catchLure) {
        this.catchLure = catchLure;
    }

    public String getCatchReel() {
        return catchReel;
    }

    public void setCatchReel(String catchReel) {
        this.catchReel = catchReel;
    }

    public String getCatchRod() {
        return catchRod;
    }

    public void setCatchRod(String catchRod) {
        this.catchRod = catchRod;
    }

    public String getCatchBait() {
        return catchBait;
    }

    public void setCatchBait(String catchBait) {
        this.catchBait = catchBait;
    }

    public String getCatchLocation() {
        return catchLocation;
    }

    public void setCatchLocation(String catchLocation) {
        this.catchLocation = catchLocation;
    }

    public String getCatchLatitude() {
        return catchLatitude;
    }

    public void setCatchLatitude(String catchLatitude) {
        this.catchLatitude = catchLatitude;
    }

    public String getCatchLongitude() {
        return catchLongitude;
    }

    public void setCatchLongitude(String catchLongitude) {
        this.catchLongitude = catchLongitude;
    }

    public String getCatchLakeName() {
        return catchLakeName;
    }

    public void setCatchLakeName(String catchLakeName) {
        this.catchLakeName = catchLakeName;
    }

    public String getCatchLength() {
        return catchLength;
    }

    public void setCatchLength(String catchLength) {
        this.catchLength = catchLength;
    }

    public String getCatchWeight() {
        return catchWeight;
    }

    public void setCatchWeight(String catchWeight) {
        this.catchWeight = catchWeight;
    }

    public String getCatchDescription() {
        return catchDescription;
    }

    public void setCatchDescription(String catchDescription) {
        this.catchDescription = catchDescription;
    }

    public String getImageCount() {
        return imageCount;
    }

    public void setImageCount(String imageCount) {
        this.imageCount = imageCount;
    }

    public List<Object> getImages() {
        return images;
    }

    public void setImages(List<Object> images) {
        this.images = images;
    }

    public String getCatchCreatedAt() {
        return catchCreatedAt;
    }

    public void setCatchCreatedAt(String catchCreatedAt) {
        this.catchCreatedAt = catchCreatedAt;
    }

    public String getCatchTitle() {
        return catchTitle;
    }

    public void setCatchTitle(String catchTitle) {
        this.catchTitle = catchTitle;
    }

    public String getCatchH2o() {
        return catchH2o;
    }

    public void setCatchH2o(String catchH2o) {
        this.catchH2o = catchH2o;
    }

    public String getCatchDepth() {
        return catchDepth;
    }

    public void setCatchDepth(String catchDepth) {
        this.catchDepth = catchDepth;
    }

    public String getCatchTemperature() {
        return catchTemperature;
    }

    public void setCatchTemperature(String catchTemperature) {
        this.catchTemperature = catchTemperature;
    }

    public String getCatchPrivacy() {
        return catchPrivacy;
    }

    public void setCatchPrivacy(String catchPrivacy) {
        this.catchPrivacy = catchPrivacy;
    }

    public Integer getCatchTimestamp() {
        return catchTimestamp;
    }

    public void setCatchTimestamp(Integer catchTimestamp) {
        this.catchTimestamp = catchTimestamp;
    }
}
