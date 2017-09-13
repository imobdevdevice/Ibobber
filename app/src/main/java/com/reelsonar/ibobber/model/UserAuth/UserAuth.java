package com.reelsonar.ibobber.model.UserAuth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manoj Singh
 */

public class UserAuth {
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("accessToken")
    @Expose
    private String accessToken;
    @SerializedName("data")
    @Expose
    private UserInfo data;

    @SerializedName("nouser")
    @Expose
    private Boolean nouser;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UserInfo getData() {
        return data;
    }

    public void setData(UserInfo data) {
        this.data = data;
    }

    public Boolean getNouser() {
        return nouser;
    }

    public void setNouser(Boolean nouser) {
        this.nouser = nouser;
    }
}
