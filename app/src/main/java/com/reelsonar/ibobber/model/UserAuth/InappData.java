package com.reelsonar.ibobber.model.UserAuth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InappData {

@SerializedName("subscription_status")
@Expose
private Integer subscriptionStatus;
@SerializedName("transaction_id")
@Expose
private String transactionId;
@SerializedName("activation_date")
@Expose
private String activationDate;

public Integer getSubscriptionStatus() {
return subscriptionStatus;
}

public void setSubscriptionStatus(Integer subscriptionStatus) {
this.subscriptionStatus = subscriptionStatus;
}

public String getTransactionId() {
return transactionId;
}

public void setTransactionId(String transactionId) {
this.transactionId = transactionId;
}

public String getActivationDate() {
return activationDate;
}

public void setActivationDate(String activationDate) {
this.activationDate = activationDate;
}

}