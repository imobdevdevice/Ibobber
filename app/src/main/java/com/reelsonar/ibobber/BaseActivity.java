package com.reelsonar.ibobber;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.reelsonar.ibobber.model.UserAuth.UserAuth;

import static com.reelsonar.ibobber.util.RestConstants.USER_INFO;

/**
 * Created by Manoj Singh
 */

public class BaseActivity extends Activity {


    private Dialog mProgressDialog;
    private ImageView loadingImageView;

    public void showProgress(boolean show) {
        if (show)
            showProgressBar();
        else
            hideProgressBar();
    }

    protected final void showProgressBar() {
        View view = View.inflate(this, R.layout.progressbar, null);
        mProgressDialog = new Dialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(view);
        mProgressDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        loadingImageView = (ImageView) view.findViewById(R.id.ivLoadingIndicator);
        mProgressDialog.show();
        Glide.with(this)
                .load(R.drawable.loading)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(loadingImageView);
    }

    protected final void hideProgressBar() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    protected final void storeUserInfo(UserAuth auth) {
//        {"accessToken":"5c994ef4-93d1-11e7-a2ff-0018510d9bfb","data":{"banner_image_url":"","image_url":"users/707/707_1504690940.","inapp_data":{"activation_date":"","subscription_status":0,"transaction_id":""},"language_code":"en","premium_plan":false,"privacy":{"catches":"1","date_of_birth":"1","gender":"1","hometown":"1","name":"1"},"redeem_info":[],"referral":{"code":"64823206","referred_by":""},"settings":{"email_summary":"1","push_all":"1","push_comment":"1","push_follow":"1","push_message":"1","push_vote":"1","share_post":"1"},"user_about_me":"","user_created_at":"1504690940","user_dob":"","user_email":"amarjadeja12@gmail.com","user_first_name":"Amar","user_gender":"","user_id":"707","user_last_name":"Jadeja","user_latitude":"37.09024","user_location":"","user_longitude":"-95.71289","user_loyalty_points":0,"user_type":"2","user_user_name":"amarjadeja34088"},"message":"Successfully logged in","status":true}
        String userInfoStr = new Gson().toJson(auth, UserAuth.class);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_INFO, userInfoStr);
        editor.apply();
    }

    protected final UserAuth getUserInfo() {
        UserAuth auth = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userInfoStr = prefs.getString(USER_INFO, "");
        auth = new Gson().fromJson(userInfoStr, UserAuth.class);
        return auth;
    }


}
