package com.reelsonar.ibobber.util;

import android.util.Patterns;
import android.widget.EditText;

public final class Validation {

    private static final String ERR_MISSING_INFO = "Please complete field";
    private static final String ERR_INVALID_EMIAL = "invalid Email";
    private static final String ERR_PHONE = "Invalid Phone Number";
    private static final String ERR_PASSWORD = "Enter Valid Password";
    private static final String ERR_FNAME = "First Name cannot be empty";
    private static final String ERR_CONFIRM_PASSWORD = "Your passwords do not match";
    private static final String ERR_PASSWORD_LENGTH = "Password must be at least 6 characters";
    private static final String ERR_MISSING_USERNAME = "Please enter user name";



    public static String checkEmail(String email) {
        String errMessage = "";

        if (email.trim().isEmpty()) {
            errMessage = ERR_MISSING_INFO;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errMessage = ERR_INVALID_EMIAL;

        }
        return errMessage;
    }

    public static String checkPassword(String password) {

        String errMessage = "";
        if (password.trim().isEmpty()) {
            errMessage = ERR_MISSING_INFO;

        } else if (password.length() < 6) {
            errMessage = ERR_PASSWORD_LENGTH;
        }
        return errMessage;
    }

    public static String checkName(String name) {

        String errMessage = "";
        if (name.trim().isEmpty()) {
            errMessage = ERR_MISSING_INFO;
        }
        return errMessage;
    }

    public static String checkPhone(String phone) {

        String errMessage = "";
        if (phone.trim().isEmpty()) {
            errMessage = ERR_MISSING_INFO;

        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            errMessage = ERR_PHONE;

        }
        return errMessage;
    }

    public static String checkConfPass(String pass, String cpass) {
        String errMessage = "";
        if (cpass.trim().isEmpty()) {
            errMessage = ERR_MISSING_INFO;

        } else if (!pass.trim().equals(cpass)) {
            errMessage = ERR_CONFIRM_PASSWORD;

        }
        return errMessage;
    }

    public static String checkUserName(String userName)
    {
        String errMessage = "";
        if(userName.trim().isEmpty())
        {
            errMessage = ERR_MISSING_USERNAME;
        }
        return errMessage;
    }

    public static String getTrimtext(EditText et) {
        return et.getText().toString().trim();
    }
}
