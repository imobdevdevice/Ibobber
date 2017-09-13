package com.reelsonar.ibobber;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.reelsonar.ibobber.databinding.ActivitySingupBinding;
import com.reelsonar.ibobber.dialogs.SimpleCustomDialog;
import com.reelsonar.ibobber.model.UserAuth.UserAuth;
import com.reelsonar.ibobber.sonar.SonarLiveActivity;
import com.reelsonar.ibobber.util.ApiLoader;
import com.reelsonar.ibobber.util.AppUtils;
import com.reelsonar.ibobber.util.CallBack;
import com.reelsonar.ibobber.util.RestConstants;
import com.reelsonar.ibobber.util.Validation;
import com.reelsonar.ibobber.view.DividerEditText;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;
import static com.reelsonar.ibobber.util.RestConstants.APP_TYPE;
import static com.reelsonar.ibobber.util.RestConstants.DEVICE_TOKEN;
import static com.reelsonar.ibobber.util.RestConstants.DEVICE_TYPE;
import static com.reelsonar.ibobber.util.RestConstants.EMAIL;
import static com.reelsonar.ibobber.util.RestConstants.FIRST_NAME;
import static com.reelsonar.ibobber.util.RestConstants.LAST_NAME;
import static com.reelsonar.ibobber.util.RestConstants.PASSWORD;
import static com.reelsonar.ibobber.util.RestConstants.UNIQUE_TOKEN;
import static com.reelsonar.ibobber.util.RestConstants.USERNAME;
import static com.reelsonar.ibobber.util.RestConstants.USERTYPE;

/**
 * Created by Manoj Singh
 */

public class SignupActivity extends BaseActivity {
    private ActivitySingupBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_singup);
    }

    public void onSignUp(View view) {
        validateDetails();
    }

    private void validateDetails() {
        String userName = binding.edtUserName.getText().toString().trim();
        if (AppUtils.isBadWordEntered(this, userName)) {
            SimpleCustomDialog simpleCustomDialog = new SimpleCustomDialog(this, getString(R.string.inappropriate_content),
                    getString(R.string.bed_username), "");
            simpleCustomDialog.show();
            return;
        }

        String deviceToken = AppUtils.getDeviceId(getApplicationContext());
        if (!isEmpty(deviceToken)) {
            callSignUpApi(deviceToken);
        } /*else {
            checkPhoneReadStatePermission(SignupActivity.this);
        }*/
    }

    private void callSignUpApi(String deviceToken) {
        String gcmToken = FirebaseInstanceId.getInstance().getToken();
        if (AppUtils.isNetworkAvailable(SignupActivity.this)) {
            boolean ibobber = AppUtils.isAppInstalled(this, "com.reelsonar.ibobber");

            String email = AppUtils.getText(binding.edtEmail);
            String password = AppUtils.getText(binding.edtPassword);
            String userName = AppUtils.getText(binding.edtUserName);
            String fname = AppUtils.getText(binding.edtFirstName);
            String lname = AppUtils.getText(binding.edtLastName);

            String errEmail = Validation.checkEmail(email);
            String errPassword = Validation.checkPassword(password);
            String errUserName = Validation.checkUserName(userName);
            String errFname = Validation.checkName(fname);
            String errLname = Validation.checkName(lname);

            if (!isEmpty(errFname)) {
                onEditTextError(binding.edtUserName, errFname);
                return;
            }
//
            if (binding.edtFirstName.length() < 2) {
                onEditTextError(binding.edtFirstName, getString(R.string.error_fname));
                return;
            }
//
//            if (!isEmpty(errLname)) {
//                onLastNameError(errLname);
//                return;
//            }
//
//            if (!isEmpty(errEmail)) {
//                onUserEmailError(errEmail);
//                return;
//            }
//            if (!isEmpty(errUserName)) {
//                onUserNameError(errUserName);
//                return;
//            }
//            if (!isEmpty(errPassword)) {
//                onPasswordError(errPassword);
//                return;
//            }
//            if (!isEmpty(errConfirmPassword)) {
//                onConfirmPasswordError(errConfirmPassword);
//                return;
//            }
//
//            if (!cbIsChecked) {
//                onAgreeTermsError();
//                return;
//            }

            if (AppUtils.isNetworkAvailable(SignupActivity.this)) {
                showProgressBar();//

                HashMap<String, String> registerParams = new HashMap<>();
                try {
                    String deviceModel = android.os.Build.MODEL;
                    String osVersion = String.valueOf(android.os.Build.VERSION.SDK_INT);
//                    app_type=1&deviceType=1&userType=1&uniqueToken=82AB8D43-E4F2-4806-BEAD-2EE1F2CC619C&user_name=vivek.lathiya&first_name=Vivek&last_name=Lathiya&email=vivek.lathiya@imobdev.com&password=7c222fb2927d828af22f592134e8932480637c0d&language_code=en
                    registerParams.put(FIRST_NAME, fname);
                    registerParams.put(LAST_NAME, lname);
                    registerParams.put(EMAIL, email);
                    registerParams.put(USERNAME, userName);
                    registerParams.put(PASSWORD, AppUtils.SHA1(password));
                    registerParams.put(APP_TYPE, "1");
                    registerParams.put(DEVICE_TYPE, RestConstants.DEVICE_ANDROID);
                    registerParams.put(USERTYPE, "1");
                    registerParams.put(UNIQUE_TOKEN, AppUtils.getDeviceId(SignupActivity.this));
                    registerParams.put(DEVICE_TOKEN, gcmToken);
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                ApiLoader.getRegister(SignupActivity.this, registerParams, new CallBack() {
                    @Override
                    public void onResponse(Call call, Response response, String msg) {
                        UserAuth userAuth = new Gson().fromJson((response.body()).toString(), UserAuth.class);
                        if (userAuth != null) {
                            if (userAuth.getStatus()) {
                                sucessLogin();
                            } else {
                                AppUtils.showToast(SignupActivity.this, userAuth.getMessage());
                            }
                        } else {
                            AppUtils.showToast(SignupActivity.this, getString(R.string.somethingwrong));
                        }
                        hideProgressBar();
                    }

                    @Override
                    public void onFail(Call call, Throwable e) {
                        AppUtils.showToast(SignupActivity.this, getString(R.string.err_timeout));
                        hideProgressBar();
                    }

                    @Override
                    public void onSocketTimeout(Call call, Throwable e) {
                        AppUtils.showToast(SignupActivity.this, getString(R.string.err_timeout));
                        hideProgressBar();
                    }
                });
            }

//            getPresenter().validateSignUpDetails(AppUtils.getText(edtFirstName), AppUtils.getText(edtLastName),
//                    cbTermsOfUse.isChecked(), AppUtils.getText(edtEmail),
//                    AppUtils.getText(edtPassword), AppUtils.getText(edtConfirmPassword),
//                    AppUtils.getText(edtUserName), AppUtils.getText(edtPromoCode), location,
//                    signUpMethod, facebookId, twitterId, gcmToken, deviceToken, ibobber, AppUtils.getGAClientID());
        } else
            AppUtils.showToast(SignupActivity.this, getString(R.string.err_network));
    }

    private void onEditTextError(DividerEditText edtUserName, String errMessage) {
        edtUserName.setError(errMessage);
        edtUserName.requestFocus();
    }

    public void onBack(View view) {
        onBackPressed();
    }

    private void sucessLogin() {
        Intent in = new Intent(SignupActivity.this, SonarLiveActivity.class);
        startActivity(in);
        finish();
    }
}
