package com.reelsonar.ibobber;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.reelsonar.ibobber.databinding.ActivityNetfishAdsBinding;
import com.reelsonar.ibobber.util.AppUtils;

import static com.reelsonar.ibobber.util.RestConstants.NETFISH_ADS_FLAG;
import static com.reelsonar.ibobber.util.RestConstants.NETFISH_URL_CATCH;

/**
 * Created by Rujul Gandhi
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
        Uri uri = Uri.parse(NETFISH_URL_CATCH);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivityForResult(intent, 100);
    }


    public void onSkipBtn(View view) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            finish();
        }
    }
}
