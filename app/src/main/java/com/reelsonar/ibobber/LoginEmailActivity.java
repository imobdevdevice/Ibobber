package com.reelsonar.ibobber;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.reelsonar.ibobber.databinding.ActivityEmailLoginBinding;
import com.reelsonar.ibobber.dialogs.SimpleCustomDialog;
import com.reelsonar.ibobber.model.UserAuth.UserAuth;
import com.reelsonar.ibobber.util.Actions;
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
import static com.reelsonar.ibobber.util.RestConstants.LANGUAGE_CODE;
import static com.reelsonar.ibobber.util.RestConstants.PASSWORD;
import static com.reelsonar.ibobber.util.RestConstants.UNIQUE_TOKEN;
import static com.reelsonar.ibobber.util.RestConstants.USERTYPE;

/**
 * Created by Manoj Singh
 */

public class LoginEmailActivity extends BaseActivity {
    private ActivityEmailLoginBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_email_login);
    }

    public void onLogin(View view) {
        validateDetails();
    }

    private void validateDetails() {
        String email = binding.edtEmail.getText().toString().trim();
        if (AppUtils.isBadWordEntered(this, email)) {
            SimpleCustomDialog simpleCustomDialog = new SimpleCustomDialog(this, getString(R.string.inappropriate_content),
                    getString(R.string.bed_username), "");
            simpleCustomDialog.show();
            return;
        }

        String deviceToken = AppUtils.getDeviceId(getApplicationContext());
        if (!isEmpty(deviceToken)) {
            callLoginApi(deviceToken);
        } /*else {
            checkPhoneReadStatePermission(SignupActivity.this);
        }*/
    }


    private void callLoginApi(String deviceToken) {
        String gcmToken = FirebaseInstanceId.getInstance().getToken();
        if (AppUtils.isNetworkAvailable(LoginEmailActivity.this)) {
            boolean ibobber = AppUtils.isAppInstalled(this, "com.reelsonar.ibobber");

            String password = AppUtils.getText(binding.edtPassword);
            String email = AppUtils.getText(binding.edtEmail);

            String errPassword = Validation.checkPassword(password);
            String errEmail = Validation.checkUserName(email);

            if (!isEmpty(errEmail)) {
                onEditTextError(binding.edtEmail, errEmail);
                return;
            }

            if (!isEmpty(errPassword)) {
                onEditTextError(binding.edtPassword, errPassword);
                return;
            }

            if (AppUtils.isNetworkAvailable(LoginEmailActivity.this)) {
                showProgressBar();//

                HashMap<String, String> registerParams = new HashMap<>();
                try {
                    String deviceModel = android.os.Build.MODEL;
                    String osVersion = String.valueOf(android.os.Build.VERSION.SDK_INT);
                    registerParams.put(EMAIL, email);
                    registerParams.put(PASSWORD, AppUtils.SHA1(password));
                    registerParams.put(APP_TYPE, "1");
                    registerParams.put(DEVICE_TYPE, RestConstants.DEVICE_ANDROID);
                    registerParams.put(USERTYPE, "1");
                    registerParams.put(UNIQUE_TOKEN, AppUtils.getDeviceId(LoginEmailActivity.this));
                    registerParams.put(DEVICE_TOKEN, gcmToken);
                    registerParams.put(LANGUAGE_CODE, "en");
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                ApiLoader.getInstance().getResponse(LoginEmailActivity.this, registerParams, RestConstants.LOGIN, UserAuth.class, new CallBack() {
                    @Override
                    public <T> void onResponse(Call call, Response response, String msg, Object object) {
                        UserAuth userAuth = new Gson().fromJson((response.body()).toString(), UserAuth.class);
                        userAuth = ((UserAuth) object);
                        if (userAuth != null) {
                            if (userAuth.getStatus()) {
                                sucessLogin(userAuth);
                            } else {
                                AppUtils.showToast(LoginEmailActivity.this, userAuth.getMessage());
                            }
                        } else {
                            AppUtils.showToast(LoginEmailActivity.this, getString(R.string.somethingwrong));
                        }
                        hideProgressBar();
                    }

                    @Override
                    public void onFail(Call call, Throwable e) {
                        AppUtils.showToast(LoginEmailActivity.this, getString(R.string.err_network));
                        hideProgressBar();
                    }

                    @Override
                    public void onSocketTimeout(Call call, Throwable e) {
                        AppUtils.showToast(LoginEmailActivity.this, getString(R.string.err_timeout));
                        hideProgressBar();
                    }
                });
            }

        } else
            AppUtils.showToast(LoginEmailActivity.this, getString(R.string.err_network));
    }

    private void sucessLogin(UserAuth userAuth) {
        storeUserInfo(userAuth);
        Intent sonar = new Intent(Actions.SONAR_LIVE);
        sonar.addCategory(Actions.CATEGORY_INITIAL_DEMO);
        startActivity(sonar);
        finish();
    }

    public void onBack(View view) {
        onBackPressed();
    }

    private void onEditTextError(DividerEditText edtUserName, String errMessage) {
        edtUserName.setError(errMessage);
        edtUserName.requestFocus();
    }
}
