package com.reelsonar.ibobber;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.reelsonar.ibobber.databinding.ActivityNetfishAdsBinding;
import com.reelsonar.ibobber.util.AppUtils;

import static com.reelsonar.ibobber.util.RestConstants.NETFISH_ADS_FLAG;
import static com.reelsonar.ibobber.util.RestConstants.NETFISH_PACKAGE;

/**
 * Created by Manoj Singh
 */

public class NetFishAdsActivity extends BaseActivity {
    ActivityNetfishAdsBinding binding;
    int netFishAdsCount;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_netfish_ads);
        netFishAdsCount = AppUtils.getIntegerSharedpreference(this, NETFISH_ADS_FLAG);
        AppUtils.storeSharedPreference(this, NETFISH_ADS_FLAG, (netFishAdsCount + 1));
    }

    public void onDownloadBtn(View view) {
        AppUtils.openAppOnPlayStore(this, NETFISH_PACKAGE);
    }


    public void onSkipBtn(View view) {
        finish();
    }
}
