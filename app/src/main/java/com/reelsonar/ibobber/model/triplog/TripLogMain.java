package com.reelsonar.ibobber.model.triplog;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Manoj Singh
 */

public class TripLogMain implements Serializable{
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("data")
    @Expose
    private TripLogDetails data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public TripLogDetails getData() {
        return data;
    }

    public void setData(TripLogDetails data) {
        this.data = data;
    }
}
