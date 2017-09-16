// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.settings;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.bluetooth.BTFirmwareUpdateProfile;
import com.reelsonar.ibobber.bluetooth.BTService;

import de.greenrobot.event.EventBus;


public class SettingsActivity extends Activity  {

    private final static String TAG = SettingsActivity.class.getSimpleName();

    private Handler _handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ListView formView = (ListView)findViewById(R.id.settingsForm);
        SettingsAdapter adapter = new SettingsAdapter(SettingsActivity.this);
        formView.setAdapter(adapter);
        formView.setOnItemClickListener(adapter);
    }


    private void refreshFormView() {
        ListView formView = (ListView)findViewById(R.id.settingsForm);
        SettingsAdapter adapter = (SettingsAdapter)formView.getAdapter();
        adapter.notifyDataSetChanged();
    }

    public void onEventMainThread(final BTService.DeviceDidConnect notification) {
        refreshFormView();
    }

    public void onEventMainThread(final BTService.DeviceDidDisconnect notification) {
        refreshFormView();
    }

    public void onEventMainThread(final BTFirmwareUpdateProfile.FirmWareInfoChanged notification) {
        refreshFormView();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFormView();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }



}
