package com.reelsonar.ibobber.model.UserAuth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Referral {

@SerializedName("referred_by")
@Expose
private String referredBy;
@SerializedName("code")
@Expose
private String code;

public String getReferredBy() {
return referredBy;
}

public void setReferredBy(String referredBy) {
this.referredBy = referredBy;
}

public String getCode() {
return code;
}

public void setCode(String code) {
this.code = code;
}

}