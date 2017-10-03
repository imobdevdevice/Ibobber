package com.reelsonar.ibobber;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.reelsonar.ibobber.databinding.ActivityNetfishAdsSplashBinding;
import com.reelsonar.ibobber.util.AppUtils;

import static com.reelsonar.ibobber.util.RestConstants.NETFISH_ADS_FLAG;
import static com.reelsonar.ibobber.util.RestConstants.NETFISH_PACKAGE;

/**
 * Created by Rujul Gandhi
 */

public class NetFishAdsSplashActivity extends BaseActivity {
    ActivityNetfishAdsSplashBinding binding;
    int netFishAdsCount;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_netfish_ads_splash);
        netFishAdsCount = AppUtils.getIntegerSharedpreference(this, NETFISH_ADS_FLAG);
        AppUtils.storeSharedPreference(this, NETFISH_ADS_FLAG, (netFishAdsCount + 1));
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                AppUtils.openAppOnPlayStore(NetFishAdsSplashActivity.this, NETFISH_PACKAGE);
                finish();
            }

        };
        handler.postDelayed(runnable, 3000);
    }

}
