package com.reelsonar.ibobber.model.UserAuth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Singh
 */

public class UserInfo {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("user_first_name")
    @Expose
    private String userFirstName;
    @SerializedName("user_last_name")
    @Expose
    private String userLastName;
    @SerializedName("user_user_name")
    @Expose
    private String userUserName;
    @SerializedName("user_type")
    @Expose
    private String userType;
    @SerializedName("user_email")
    @Expose
    private String userEmail;
    @SerializedName("user_location")
    @Expose
    private String userLocation;
    @SerializedName("user_latitude")
    @Expose
    private String userLatitude;
    @SerializedName("user_longitude")
    @Expose
    private String userLongitude;
    @SerializedName("user_about_me")
    @Expose
    private String userAboutMe;
    @SerializedName("user_dob")
    @Expose
    private String userDob;
    @SerializedName("user_gender")
    @Expose
    private String userGender;
    @SerializedName("user_created_at")
    @Expose
    private String userCreatedAt;
    @SerializedName("user_loyalty_points")
    @Expose
    private Integer userLoyaltyPoints;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("banner_image_url")
    @Expose
    private String bannerImageUrl;
    @SerializedName("premium_plan")
    @Expose
    private Boolean premiumPlan;
    @SerializedName("language_code")
    @Expose
    private String languageCode;
    @SerializedName("referral")
    @Expose
    private Referral referral;
    @SerializedName("privacy")
    @Expose
    private Privacy privacy;
    @SerializedName("settings")
    @Expose
    private Settings settings;
    @SerializedName("inapp_data")
    @Expose
    private InappData inappData;
    @SerializedName("redeem_info")
    @Expose
    private List<Object> redeemInfo = null;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserUserName() {
        return userUserName;
    }

    public void setUserUserName(String userUserName) {
        this.userUserName = userUserName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(String userLatitude) {
        this.userLatitude = userLatitude;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(String userLongitude) {
        this.userLongitude = userLongitude;
    }

    public String getUserAboutMe() {
        return userAboutMe;
    }

    public void setUserAboutMe(String userAboutMe) {
        this.userAboutMe = userAboutMe;
    }

    public String getUserDob() {
        return userDob;
    }

    public void setUserDob(String userDob) {
        this.userDob = userDob;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserCreatedAt() {
        return userCreatedAt;
    }

    public void setUserCreatedAt(String userCreatedAt) {
        this.userCreatedAt = userCreatedAt;
    }

    public Integer getUserLoyaltyPoints() {
        return userLoyaltyPoints;
    }

    public void setUserLoyaltyPoints(Integer userLoyaltyPoints) {
        this.userLoyaltyPoints = userLoyaltyPoints;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public Boolean getPremiumPlan() {
        return premiumPlan;
    }

    public void setPremiumPlan(Boolean premiumPlan) {
        this.premiumPlan = premiumPlan;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Referral getReferral() {
        return referral;
    }

    public void setReferral(Referral referral) {
        this.referral = referral;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public InappData getInappData() {
        return inappData;
    }

    public void setInappData(InappData inappData) {
        this.inappData = inappData;
    }

    public List<Object> getRedeemInfo() {
        return redeemInfo;
    }

    public void setRedeemInfo(List<Object> redeemInfo) {
        this.redeemInfo = redeemInfo;
    }
}
