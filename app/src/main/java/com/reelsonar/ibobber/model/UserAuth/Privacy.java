package com.reelsonar.ibobber.model.UserAuth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Privacy {

@SerializedName("hometown")
@Expose
private String hometown;
@SerializedName("gender")
@Expose
private String gender;
@SerializedName("date_of_birth")
@Expose
private String dateOfBirth;
@SerializedName("catches")
@Expose
private String catches;
@SerializedName("name")
@Expose
private String name;

public String getHometown() {
return hometown;
}

public void setHometown(String hometown) {
this.hometown = hometown;
}

public String getGender() {
return gender;
}

public void setGender(String gender) {
this.gender = gender;
}

public String getDateOfBirth() {
return dateOfBirth;
}

public void setDateOfBirth(String dateOfBirth) {
this.dateOfBirth = dateOfBirth;
}

public String getCatches() {
return catches;
}

public void setCatches(String catches) {
this.catches = catches;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

}