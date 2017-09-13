// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.drawer;


import android.animation.Animator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.reelsonar.ibobber.BLEScanActivity;
import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.bluetooth.BTConstants;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.sonar.SonarLiveActivity;
import com.reelsonar.ibobber.sonar.SonarLiveRawActivity;
import com.reelsonar.ibobber.util.Actions;
import com.reelsonar.ibobber.util.DoubleClickWrapper;
import com.reelsonar.ibobber.util.MathUtil;
import com.reelsonar.ibobber.util.Sound;
import de.greenrobot.event.EventBus;


public class DeviceDrawerFragment extends Fragment implements View.OnClickListener, DoubleClickWrapper.DoubleClickListener {

    private final static String TAG = DeviceDrawerFragment.class.getSimpleName();

    private static final int[] BUTTONS = {
            R.id.sonarLiveButton,
            R.id.bobberDrawerButton,
            R.id.bobberDrawerButtonOverlay,
            R.id.sonarExploreButton,
            R.id.battButton,
            R.id.tempButton,
            R.id.alarmButton,
            R.id.strikeButton,
            R.id.lightButton,
            R.id.buzzerButton,
            R.id.accelerometerButton
    };

    private static final int[] TRANSLATE_VIEWS = {
            R.id.drawerBackground,
            R.id.deviceDrawerButtons,
            R.id.bobberDrawerButton,
            R.id.bobberDrawerButtonOverlay,
            R.id.accelerometerView
    };

    public static class PauseRequested{};
    public static class ResumeRequested{};

    private static int BATTERY_LEVEL_0 = 10;
    private static int BATTERY_LEVEL_1 = 40;
    private static int BATTERY_LEVEL_2 = 75;

    private static int BOBBER_PULSE_FREQ = 500;
    private static float BOBBER_PULSE_LOW_ALPHA = 0.2f;
    private static float BOBBER_PULSE_HIGH_ALPHA = 0.5f;

    private boolean _open;
    private boolean _bobberPulsing = false;

    private Sound _strikeAlarm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device_drawer, container, false);

        for (int id : BUTTONS) {
            View button = rootView.findViewById(id);
            if (id == R.id.strikeButton) {
                DoubleClickWrapper.wrap(button, this);
            } else {
                button.setOnClickListener(this);
            }
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshBobberStatus();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (_strikeAlarm != null) {
            _strikeAlarm.release();
        }
    }

    public void onEventMainThread(final BTService.HardwareRevUpdated notification) {
        setButtonsForHardwareRev();
    }

    public void onEventMainThread(final BTService.DeviceDidConnect notification) {
        refreshBobberStatus();
    }

    public void onEventMainThread(final BTService.DeviceDidDisconnect notification) {
        refreshBobberStatus();
    }

    public void onEventMainThread(final BTService.DevicePropertiesUpdated notification) {
        if (_open) {
            refreshBobberInfo();
        }
    }

    public void onEventMainThread(final BTService.StrikeAlarmOccurred notification) {
        if (_strikeAlarm == null) {
            _strikeAlarm = new Sound(getActivity(), R.raw.beeps_up, 1);
        }
        _strikeAlarm.play();
    }

    public void onEventMainThread(final UserService.LocalizationChangedNotification notification) {
        String on = getActivity().getString(R.string.on), off = getActivity().getString(R.string.off);
        for (int id : new int[] { R.id.alarmButton, R.id.strikeButton, R.id.lightButton, R.id.buzzerButton }) {
            ToggleButton toggle = (ToggleButton)getView().findViewById(id);
            toggle.setTextOn(on);
            toggle.setTextOff(off);
        }

        ((TextView) getView().findViewById(R.id.accelerometerButton)).setText(R.string.button_done_uppercase);
    }

    public void setLiveSonar() {
        getView().findViewById(R.id.sonarLiveButton).setBackgroundResource(R.drawable.sonar_live_active);
    }

    public void setLiveRawSonar() {
        getView().findViewById(R.id.sonarLiveButton).setBackgroundResource(R.drawable.sonar_live_orange);
    }

    @Override
    public void onClick(View view) {

        boolean homeDrawerOpen = false;
        HomeDrawerFragment homeDrawerFragment = (HomeDrawerFragment)getFragmentManager().findFragmentById(R.id.homeDrawer);
        if (homeDrawerFragment == null) homeDrawerFragment = (HomeDrawerFragment)getFragmentManager().findFragmentByTag("HomeDrawer");
        if (homeDrawerFragment != null) homeDrawerOpen = homeDrawerFragment.isOpen();

        if (view.getId() == R.id.bobberDrawerButtonOverlay) {
            boolean connected = BTService.getSingleInstance() != null && BTService.getSingleInstance().getConnectedToDevice();
            if (connected) {
                if (!homeDrawerOpen) toggleOpen();
            }
            else {
                if (!(getActivity() instanceof BLEScanActivity)) {
                    Intent intent = new Intent(BobberApp.getContext(), BLEScanActivity.class);
                    startActivity(intent);
                }
            }
        }

        if (homeDrawerOpen) return;

        switch (view.getId()) {
            case R.id.lightButton:
                lightButton();
                break;
            case R.id.buzzerButton:
                buzzerButton();
                break;
            case R.id.alarmButton:
                alarmButton();
                break;
            case R.id.strikeButton:
                strikeButton();
                break;
            case R.id.accelerometerButton:
                accelerometerButton();
                break;
            case R.id.sonarLiveButton: {

                Activity currentActivity = getActivity();
                Intent intent;

                if (!(currentActivity instanceof SonarLiveActivity) && !(currentActivity instanceof SonarLiveRawActivity)) {
                    intent = new Intent(Actions.SONAR_LIVE);
                } else if (currentActivity instanceof SonarLiveRawActivity) {
                    intent = new Intent(Actions.SONAR_LIVE);
                    getView().findViewById(R.id.sonarLiveButton).setBackgroundResource(R.drawable.sonar_live_orange);
                } else {
                    intent = new Intent(Actions.SONAR_LIVE_RAW);
                    getView().findViewById(R.id.sonarLiveButton).setBackgroundResource(R.drawable.sonar_live_active);
                }

                startActivity(intent);
                getActivity().finish();

                break;
            }
            case R.id.sonarExploreButton: {
                Intent intent = new Intent(Actions.SONAR_EXPLORE);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onDoubleClick(View view) {
        SeekBar seek = (SeekBar)getView().findViewById(R.id.accelerometerSlider);
        seek.setMax(BTConstants.STRIKE_ALARM_MAX_VALUE);
        seek.setProgress(BTService.getSingleInstance().getAccelThreshold());

        getView().findViewById(R.id.accelerometerView).setVisibility(View.VISIBLE);
    }

    private void refreshBobberStatus() {
        boolean connected = BTService.getSingleInstance() != null && BTService.getSingleInstance().getConnectedToDevice();
        final View drawerButton = getView().findViewById(R.id.bobberDrawerButton);

        if (!connected) {
            _bobberPulsing = true;
            drawerButton.animate().alpha(BOBBER_PULSE_LOW_ALPHA);
            drawerButton.animate().setDuration(BOBBER_PULSE_FREQ);

            //pulse bobber
            drawerButton.animate().setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (_bobberPulsing) {
                        if (drawerButton.getAlpha() == BOBBER_PULSE_LOW_ALPHA) {
                            drawerButton.animate().alpha(BOBBER_PULSE_HIGH_ALPHA);
                        } else {
                            drawerButton.animate().alpha(BOBBER_PULSE_LOW_ALPHA);
                        }
                    }
                }
                public void onAnimationCancel(Animator animation) {}

                public void onAnimationRepeat(Animator animation) {}

                public void onAnimationStart(Animator animation) {}

            });

        }
        else {
            _bobberPulsing = false;
            drawerButton.animate().setDuration(getView().findViewById(R.id.deviceDrawerButtons).animate().getDuration());
            drawerButton.animate().cancel();
            drawerButton.setAlpha(1.0f);

            setButtonsForHardwareRev();
        }

        if (!connected && _open) {
            toggleOpen();
        }
    }

    private void refreshBobberInfo() {
        int battery = BTService.getSingleInstance().getBatteryPercent();
        int celsius = BTService.getSingleInstance().getTempCelsius();
        int temp = MathUtil.celsiusToUnitOfMeasurement(celsius, getActivity());

        Button batteryButton = (Button)getView().findViewById(R.id.battButton);
        Button batteryComboButton = (Button)getView().findViewById(R.id.battComboButton);

        batteryButton.setText(battery + "%");
        batteryComboButton.setText(battery + "%");

        int batteryResource;
        if (battery < BATTERY_LEVEL_0) {
            batteryResource = R.drawable.drawer_icon_battery;
        } else if (battery < BATTERY_LEVEL_1) {
            batteryResource = R.drawable.drawer_icon_battery1;
        } else if (battery < BATTERY_LEVEL_2) {
            batteryResource = R.drawable.drawer_icon_battery2;
        } else {
            batteryResource = R.drawable.drawer_icon_battery3;
        }

        batteryButton.setCompoundDrawablesWithIntrinsicBounds(0, batteryResource, 0, 0);
        batteryComboButton.setCompoundDrawablesWithIntrinsicBounds(batteryResource, 0, 0, 0);

        Button temperatureButton = (Button)getView().findViewById(R.id.tempButton);
        Button temperatureComboButton = (Button)getView().findViewById(R.id.tempComboButton);

        if (UserService.getInstance(getActivity()).isMetric()) {
            temperatureButton.setText(temp + "\u2103");
            temperatureComboButton.setText(temp + "\u2103");
        } else {
            temperatureButton.setText(temp + "\u2109");
            temperatureComboButton.setText(temp + "\u2109");
        }

        boolean motionAlarm = UserService.getInstance(getActivity()).isMotionAlarm();
        boolean strikeAlarm = (BTConstants.STRIKE_ALARM_ENABLE == BTService.getSingleInstance().getStrikeAlarmEnableStatus());
        boolean light = (BTConstants.LIGHT_FLASH == BTService.getSingleInstance().getLight());
        boolean buzzer = (BTConstants.BUZZER_ON == BTService.getSingleInstance().getBuzzer());

        ((ToggleButton)getView().findViewById(R.id.alarmButton)).setChecked(motionAlarm);
        ((ToggleButton)getView().findViewById(R.id.strikeButton)).setChecked(strikeAlarm);
        ((ToggleButton)getView().findViewById(R.id.lightButton)).setChecked(light);
        ((ToggleButton)getView().findViewById(R.id.buzzerButton)).setChecked(buzzer);
    }

    private void toggleOpen() {

        BobberApp.updateLastUserInteractionTimeStamp();

        _open = !_open;

        getView().bringToFront();
        getView().findViewById(R.id.backgroundOverlay).setClickable(_open);

        if (_open) {
            refreshBobberInfo();
            getView().findViewById(R.id.backgroundOverlay).setBackgroundColor(
                    getActivity().getResources().getColor(R.color.transparentGray));


        } else {
            getView().findViewById(R.id.accelerometerView).setVisibility(View.GONE);
            getView().findViewById(R.id.backgroundOverlay).setBackgroundColor(
                    getActivity().getResources().getColor(R.color.transparent));
        }

        float translation = getActivity().getResources().getDimension(R.dimen.device_drawer_translation);
        for (int id : TRANSLATE_VIEWS) {
            getView().findViewById(id).animate().translationY(_open ? 0 : translation);
        }
    }



    private void lightButton () {
        Integer lightValue = BTService.getSingleInstance().getLight();

        if ( lightValue == BTConstants.LIGHT_OFF) {
            lightValue = BTConstants.LIGHT_FLASH;
        } else {
            lightValue = BTConstants.LIGHT_OFF;
        }

        BTService.getSingleInstance().setLight(lightValue);
    }

    private void buzzerButton() {
        Integer buzzerValue = BTService.getSingleInstance().getBuzzer();

        if (buzzerValue == BTConstants.BUZZER_OFF) {
            buzzerValue = BTConstants.BUZZER_ON;
        }
        else {
            buzzerValue = BTConstants.BUZZER_OFF;
        }

        BTService.getSingleInstance().setBuzzer(buzzerValue);
    }

    public void alarmButton () {
        boolean motionAlarm = ((ToggleButton)getView().findViewById(R.id.alarmButton)).isChecked();
        UserService.getInstance(getActivity()).setMotionAlarm(motionAlarm);
    }

    public void strikeButton () {
        int strikeValue = BTService.getSingleInstance().getStrikeAlarmEnableStatus();

        if ( strikeValue == BTConstants.STRIKE_ALARM_ENABLE ) {
            strikeValue = BTConstants.STRIKE_ALARM_DISABLE;
        } else {
            strikeValue = BTConstants.STRIKE_ALARM_ENABLE;
        }

        BTService.getSingleInstance().setStrikeAlarmEnabled(strikeValue);
    }

    public void accelerometerButton() {
        SeekBar seek = (SeekBar)getView().findViewById(R.id.accelerometerSlider);
        int accelThreshold = seek.getProgress();
        BTService.getSingleInstance().setAccelThreshold(accelThreshold);

        getView().findViewById(R.id.accelerometerView).setVisibility(View.GONE);
    }

    private void setButtonsForHardwareRev() {
        if (BTService.getSingleInstance().getHardwareRev() >= BTConstants.HARDWARE_REV_5) {
            getView().findViewById(R.id.battTempComboButton).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.buzzerButton).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.battButton).setVisibility(View.GONE);
            getView().findViewById(R.id.tempButton).setVisibility(View.GONE);
        }
        else {
            getView().findViewById(R.id.battTempComboButton).setVisibility(View.GONE);
            getView().findViewById(R.id.buzzerButton).setVisibility(View.GONE);
            getView().findViewById(R.id.battButton).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.tempButton).setVisibility(View.VISIBLE);
        }
    }

}
