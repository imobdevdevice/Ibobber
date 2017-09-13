package com.reelsonar.ibobber.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Example {

    @SerializedName("status")
    @Expose
    private Boolean status;

    @SerializedName("message")
    @Expose
    private String message;

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

    public Boolean getNouser() {
        return nouser;
    }

    public void setNouser(Boolean nouser) {
        this.nouser = nouser;
    }

    @Override
    public String toString() {
        return "Example{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", nouser=" + nouser +
                '}';
    }
}
