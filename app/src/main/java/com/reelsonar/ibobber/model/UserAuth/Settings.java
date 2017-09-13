package com.reelsonar.ibobber.model.UserAuth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("share_post")
    @Expose
    private String sharePost;
    @SerializedName("push_all")
    @Expose
    private String pushAll;
    @SerializedName("push_follow")
    @Expose
    private String pushFollow;
    @SerializedName("push_comment")
    @Expose
    private String pushComment;
    @SerializedName("push_vote")
    @Expose
    private String pushVote;
    @SerializedName("push_message")
    @Expose
    private String pushMessage;
    @SerializedName("email_summary")
    @Expose
    private String emailSummary;

    public String getSharePost() {
        return sharePost;
    }

    public void setSharePost(String sharePost) {
        this.sharePost = sharePost;
    }

    public String getPushAll() {
        return pushAll;
    }

    public void setPushAll(String pushAll) {
        this.pushAll = pushAll;
    }

    public String getPushFollow() {
        return pushFollow;
    }

    public void setPushFollow(String pushFollow) {
        this.pushFollow = pushFollow;
    }

    public String getPushComment() {
        return pushComment;
    }

    public void setPushComment(String pushComment) {
        this.pushComment = pushComment;
    }

    public String getPushVote() {
        return pushVote;
    }

    public void setPushVote(String pushVote) {
        this.pushVote = pushVote;
    }

    public String getPushMessage() {
        return pushMessage;
    }

    public void setPushMessage(String pushMessage) {
        this.pushMessage = pushMessage;
    }

    public String getEmailSummary() {
        return emailSummary;
    }

    public void setEmailSummary(String emailSummary) {
        this.emailSummary = emailSummary;
    }

}