package com.reelsonar.ibobber.model.triplog;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manoj Singh
 */

public class CatchTripLogDetails {

    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("catch_id")
    @Expose
    private String catchId;
    @SerializedName("highfive_count")
    @Expose
    private String highfiveCount;
    @SerializedName("highfive_flag")
    @Expose
    private String highfiveFlag;
    @SerializedName("comment_flag")
    @Expose
    private String commentFlag;
    @SerializedName("comment_count")
    @Expose
    private String commentCount;
    @SerializedName("comment_remaining_count")
    @Expose
    private Integer commentRemainingCount;
    @SerializedName("image_count")
    @Expose
    private String imageCount;
    @SerializedName("catch_created_at")
    @Expose
    private String catchCreatedAt;
    @SerializedName("user_data")
    @Expose
    private UserData userData;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCatchId() {
        return catchId;
    }

    public void setCatchId(String catchId) {
        this.catchId = catchId;
    }

    public String getHighfiveCount() {
        return highfiveCount;
    }

    public void setHighfiveCount(String highfiveCount) {
        this.highfiveCount = highfiveCount;
    }

    public String getHighfiveFlag() {
        return highfiveFlag;
    }

    public void setHighfiveFlag(String highfiveFlag) {
        this.highfiveFlag = highfiveFlag;
    }

    public String getCommentFlag() {
        return commentFlag;
    }

    public void setCommentFlag(String commentFlag) {
        this.commentFlag = commentFlag;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getCommentRemainingCount() {
        return commentRemainingCount;
    }

    public void setCommentRemainingCount(Integer commentRemainingCount) {
        this.commentRemainingCount = commentRemainingCount;
    }

    public String getImageCount() {
        return imageCount;
    }

    public void setImageCount(String imageCount) {
        this.imageCount = imageCount;
    }

    public String getCatchCreatedAt() {
        return catchCreatedAt;
    }

    public void setCatchCreatedAt(String catchCreatedAt) {
        this.catchCreatedAt = catchCreatedAt;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }
}
