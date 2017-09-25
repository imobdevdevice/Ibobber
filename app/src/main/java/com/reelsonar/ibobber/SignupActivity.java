package com.reelsonar.ibobber;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.GsonBuilder;
import com.reelsonar.ibobber.databinding.ActivitySingupBinding;
import com.reelsonar.ibobber.dialogs.SimpleCustomDialog;
import com.reelsonar.ibobber.model.UserAuth.UserAuth;
import com.reelsonar.ibobber.onboarding.AppDemoActivity;
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
import static com.reelsonar.ibobber.onboarding.AppDemoActivity.INITIAL_DEMO_AFTER_REGISTER_KEY;
import static com.reelsonar.ibobber.onboarding.AppDemoActivity.INITIAL_DEMO_IS_TRUE;
import static com.reelsonar.ibobber.util.RestConstants.APP_TYPE;
import static com.reelsonar.ibobber.util.RestConstants.DEVICE_TOKEN;
import static com.reelsonar.ibobber.util.RestConstants.DEVICE_TYPE;
import static com.reelsonar.ibobber.util.RestConstants.EMAIL;
import static com.reelsonar.ibobber.util.RestConstants.FIRST_NAME;
import static com.reelsonar.ibobber.util.RestConstants.LANGUAGE_CODE;
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

            // Error Messages String
            String errEmail = Validation.checkEmail(email);
            String errPassword = Validation.checkPassword(password);
            String errUserName = Validation.checkUserName(userName);
            String errFname = Validation.checkName(fname);
            String errLname = Validation.checkName(lname);

            if (!isEmpty(errFname)) {
                onEditTextError(binding.edtFirstName, errFname);
                return;
            }
            if (binding.edtFirstName.length() < 2) {
                onEditTextError(binding.edtFirstName, getString(R.string.error_fname));
                return;
            }
            if (!isEmpty(errLname)) {
                onEditTextError(binding.edtLastName, errLname);
                return;
            }
            if (!isEmpty(errEmail)) {
                onEditTextError(binding.edtEmail, errEmail);
                return;
            }
            if (!isEmpty(errUserName)) {
                onEditTextError(binding.edtUserName, errUserName);
                return;
            }
            if (!isEmpty(errPassword)) {
                onEditTextError(binding.edtPassword, errPassword);
                return;
            }

            if (AppUtils.isNetworkAvailable(SignupActivity.this)) {
                showProgressBar();
                HashMap<String, String> registerParams = new HashMap<>();
                try {
                    String deviceModel = android.os.Build.MODEL;
                    String osVersion = String.valueOf(android.os.Build.VERSION.SDK_INT);
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
                    registerParams.put(LANGUAGE_CODE, "en");
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                ApiLoader.getInstance().getResponse(SignupActivity.this, registerParams, RestConstants.REGISTER, UserAuth.class, new CallBack() {
                    @Override
                    public <T> void onResponse(Call call, Response response, String msg, Object object) {
                        UserAuth userAuth = (new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()).fromJson((response.body()).toString(), UserAuth.class);
                        userAuth = ((UserAuth) object);
                        if (userAuth != null) {
                            if (userAuth.getStatus()) {
                                sucessLogin(userAuth);
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
//        edtUserName.setError(errMessage);
//        edtUserName.requestFocus();
        Toast.makeText(this, errMessage, Toast.LENGTH_SHORT).show();
    }

    public void onBack(View view) {
        onBackPressed();
    }

    private void sucessLogin(UserAuth userAuth) {
        storeUserInfo(userAuth);
        Intent intent = new Intent(SignupActivity.this, AppDemoActivity.class);
        intent.putExtra(INITIAL_DEMO_AFTER_REGISTER_KEY, INITIAL_DEMO_IS_TRUE);
        startActivity(intent);
        finish();
    }

}
