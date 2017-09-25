package com.reelsonar.ibobber;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.reelsonar.ibobber.util.Actions;
import com.reelsonar.ibobber.util.AppUtils;

/**
 * Created by Manoj Singh
 */

public class SplashActivity extends BaseActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String deviceToken = AppUtils.getDeviceId(getApplicationContext());
//        if (FirebaseInstanceId.getInstance().getToken() != null)
//            Log.d("User Token ", deviceToken + " , Device token :" + FirebaseInstanceId.getInstance().getToken());
//        else
//            Log.d("User Token ", deviceToken);
        handler = new Handler();
        handler.postDelayed(runnable, 5000);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (getUserInfo() == null) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent sonar = new Intent(Actions.SONAR_LIVE);
                sonar.addCategory(Actions.CATEGORY_INITIAL_DEMO);
                startActivity(sonar);
                finish();
//                Intent intent = new Intent(SplashActivity.this, AppDemoActivity.class);
//                intent.putExtra(INITIAL_DEMO_AFTER_REGISTER_KEY, INITIAL_DEMO_IS_TRUE);
//                startActivity(intent);
//                finish();
            }
        }
    };

//    public void printHashKey(Context pContext) {
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.reelsonar.ibobber",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }
//    }
}
