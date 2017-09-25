package com.reelsonar.ibobber;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.GsonBuilder;
import com.reelsonar.ibobber.databinding.ActivityLoginBinding;
import com.reelsonar.ibobber.model.UserAuth.UserAuth;
import com.reelsonar.ibobber.util.Actions;
import com.reelsonar.ibobber.util.ApiLoader;
import com.reelsonar.ibobber.util.AppUtils;
import com.reelsonar.ibobber.util.CallBack;
import com.reelsonar.ibobber.util.RestConstants;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramSession;
import net.londatiga.android.instagram.InstagramUser;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

import static com.reelsonar.ibobber.util.AppUtils.CLIENT_ID;
import static com.reelsonar.ibobber.util.AppUtils.CLIENT_SECRET;
import static com.reelsonar.ibobber.util.AppUtils.DEVICE_TYPE;
import static com.reelsonar.ibobber.util.AppUtils.REDIRECT_URI;
import static com.reelsonar.ibobber.util.AppUtils.USERTYPE_FACEBOOK;
import static com.reelsonar.ibobber.util.AppUtils.USERTYPE_INSTAGRAM;
import static com.reelsonar.ibobber.util.AppUtils.USERTYPE_TWITTER;
import static com.reelsonar.ibobber.util.RestConstants.TWITTER_KEY;
import static com.reelsonar.ibobber.util.RestConstants.TWITTER_SECRET;

/**
 * Created by Manoj Singh
 */

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private CallbackManager callbackManager;
    String email, social_id, name, first_name, last_name;
    private TwitterSession session;
    private TwitterConfig config;
    private Integer login_type;
    private Instagram mInstagram;
    private InstagramSession mInstagramSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConfig();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        binding.btnFBLogin.setReadPermissions(Arrays.asList("public_profile,email,user_birthday"));
        binding.btnFBLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest
                        .newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (object != null) {
                                    email = object.optString("email");
                                    social_id = object.optString("id");
                                    name = object.optString("name");
                                    first_name = object.optString("first_name");
                                    last_name = object.optString("last_name");
                                    login_type = USERTYPE_FACEBOOK;
                                    loginApiCall();
                                } else {
                                    AppUtils.showToast(getApplicationContext(), getString(R.string.somethingwrong));
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "name,email,gender,birthday,first_name,last_name");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        binding.btnTwitterLogin.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                session = result.data;
                name = session.getUserName();
                email = "";
                social_id = String.valueOf(session.getUserId());
                login_type = USERTYPE_TWITTER;

                loginApiCall();

            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("Sucess", "Twitter fail");
            }
        });

    }

    private void initConfig() {
        callbackManager = CallbackManager.Factory.create();
        config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
        mInstagram = new Instagram(this, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
        mInstagramSession = mInstagram.getSession();
    }

    private void loginApiCall() {
        showProgressBar();
        ApiLoader.getInstance().getResponse(getApplicationContext(), getLoginInfo(), RestConstants.LOGIN, UserAuth.class, new CallBack() {
            @Override
            public <T> void onResponse(Call call, Response response, String msg, Object object) {
                String responseStr = response.body().toString();

                UserAuth userAuth = (new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()).fromJson(responseStr, UserAuth.class);
                userAuth = ((UserAuth) object);
                if (userAuth.getNouser() != null) {
                    if (userAuth.getNouser()) {
                        Register();
                    } else {
                        sucessLogin(userAuth);
                    }
                } else {
                    if (userAuth.getStatus())
                        sucessLogin(userAuth);
                }
                hideProgressBar();
            }

            @Override
            public void onFail(Call call, Throwable e) {
                hideProgressBar();
                AppUtils.showToast(LoginActivity.this, getString(R.string.err_network));
            }

            @Override
            public void onSocketTimeout(Call call, Throwable e) {
                hideProgressBar();
                AppUtils.showToast(LoginActivity.this, getString(R.string.err_timeout));
            }
        });
    }

    private void sucessLogin(UserAuth userAuth) {
        storeUserInfo(userAuth);
        Intent sonar = new Intent(Actions.SONAR_LIVE);
        sonar.addCategory(Actions.CATEGORY_INITIAL_DEMO);
        startActivity(sonar);
        finish();
    }

    private void Register() {
        showProgressBar();
        ApiLoader.getInstance().getResponse(getApplicationContext(), getLoginInfo(), RestConstants.REGISTER, UserAuth.class, new CallBack() {
            @Override
            public <T> void onResponse(Call call, Response response, String msg, Object object) {

                String responseStr = response.body().toString();
                UserAuth userAuth = (new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()).fromJson(responseStr, UserAuth.class);
                userAuth = ((UserAuth) object);
                if (userAuth.getStatus()) {
                    sucessLogin(userAuth);
                } else {
                    AppUtils.showToast(LoginActivity.this, userAuth.getMessage());
                }
                hideProgressBar();
            }

            @Override
            public void onFail(Call call, Throwable e) {
                hideProgressBar();
                AppUtils.showToast(LoginActivity.this, getString(R.string.err_network));
            }

            @Override
            public void onSocketTimeout(Call call, Throwable e) {
                hideProgressBar();
                AppUtils.showToast(LoginActivity.this, getString(R.string.err_timeout));
            }
        });

    }

    private HashMap<String, String> getLoginInfo() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("email", email);
        hashMap.put("social_id", social_id);
        hashMap.put("userType", String.valueOf(login_type));
        hashMap.put("deviceType", DEVICE_TYPE);
        hashMap.put("deviceToken", FirebaseInstanceId.getInstance().getToken());
        hashMap.put("uniqueToken", AppUtils.getDeviceId(LoginActivity.this));
        hashMap.put("app_type", "1");
        return hashMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button.
        binding.btnTwitterLogin.onActivityResult(requestCode, resultCode, data);
    }

    public void onFacebookClick(View view) {
        binding.btnFBLogin.performClick();
    }


    public void instaLogin(View view) {
        mInstagram.authorize(mAuthListener);
    }

    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
            email = "";
            social_id = user.id;
            name = user.username;
            login_type = USERTYPE_INSTAGRAM;
            loginApiCall();
        }

        @Override
        public void onError(String error) {
            AppUtils.showToast(LoginActivity.this, getString(R.string.err_network));
        }


        @Override
        public void onCancel() {
            AppUtils.showToast(LoginActivity.this, getString(R.string.error_cancel));
        }
    };

    public void onTwitterLogin(View view) {
        binding.btnTwitterLogin.performClick();
    }

    public void onSignup(View view) {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    public void onLoginByEmail(View view) {
        Intent intent = new Intent(LoginActivity.this, LoginEmailActivity.class);
        startActivity(intent);
    }
}
