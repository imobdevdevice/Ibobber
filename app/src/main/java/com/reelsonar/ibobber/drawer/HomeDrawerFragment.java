// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.drawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.reelsonar.ibobber.*;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.sonar.SonarActivity;
import com.reelsonar.ibobber.sonar.SonarLiveActivity;
import com.reelsonar.ibobber.util.Actions;
import de.greenrobot.event.EventBus;

import java.util.Calendar;

public class HomeDrawerFragment extends Fragment implements View.OnClickListener {

    public static class HomeDrawerClosed{};

    private final static String TAG = HomeDrawerFragment.class.getSimpleName();
    private static final int[] BUTTONS = {
            R.id.homeDrawerToggleButton,
            R.id.sonarButton,
            R.id.waterbedButton,
            R.id.weatherButton,
            R.id.calendarButton,
            R.id.settingsButton,
            R.id.triplogButton,
    };

    private boolean _open;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_home_drawer, container, false);

        for (int id : BUTTONS) {
            Button button = (Button)rootView.findViewById(id);
            button.setOnClickListener(this);
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {

        BobberApp.updateLastUserInteractionTimeStamp();

        if (view.getId() == R.id.homeDrawerToggleButton) {
            toggleOpen(null);
        } else {

            String action = getActivity().getIntent().getAction();

            switch (view.getId()) {
                case R.id.sonarButton:
                    action = Actions.SONAR_LIVE;
                    break;
                case R.id.waterbedButton:
                    action = Actions.SONAR_EXPLORE;
                    break;
                case R.id.weatherButton:
                    action = Actions.WEATHER;
                    break;
                case R.id.calendarButton:
                    action = Actions.ACTIVE_USERS_MAP;
                    break;
                case R.id.settingsButton:
                    action = Actions.SETTINGS;
                    break;
                case R.id.triplogButton:
                    action = Actions.TRIPLOG;
                    break;
            }

            if (getActivity().getIntent().getAction() == null || !getActivity().getIntent().getAction().equals(action)) {
                Intent intent = new Intent(action);
                toggleOpen(intent);
            } else {
                toggleOpen(null);
            }
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(final UserService.LocalizationChangedNotification notification) {
        ((TextView)getView().findViewById(R.id.sonarButton)).setText(R.string.bottom_menu_sonar);
        ((TextView)getView().findViewById(R.id.waterbedButton)).setText(R.string.bottom_menu_waterbed);
        ((TextView)getView().findViewById(R.id.weatherButton)).setText(R.string.bottom_menu_weather);
        ((TextView)getView().findViewById(R.id.calendarButton)).setText("Global Plotters");
        ((TextView)getView().findViewById(R.id.settingsButton)).setText(R.string.bottom_menu_settings);
        ((TextView)getView().findViewById(R.id.triplogButton)).setText(R.string.bottom_menu_trip_log);
    }

    public void toggleOpen() {
        toggleOpen(null);
    }

    private void toggleOpen(final Intent intent) {
        _open = !_open;

        if( !_open ) {
           EventBus.getDefault().post(new HomeDrawerClosed());
        }

        getView().bringToFront();

        ViewPropertyAnimator animator = getView().animate().translationY(_open ? 0 : getActivity().getResources().getDimension(R.dimen.home_drawer_translation));

        View overLayView = getView().findViewById(R.id.backgroundOverlay);
        if (overLayView != null) {
            if (!_open) {
                overLayView.setBackgroundColor(BobberApp.getContext().getResources().getColor(R.color.transparent));
            }
        }

        animator.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                View drawerView = getView();
                View overLayView = null;

                if (drawerView != null) overLayView = drawerView.findViewById(R.id.backgroundOverlay);

                if (_open) {
                    if (overLayView != null) overLayView.setBackgroundColor(BobberApp.getContext().getResources().getColor(R.color.transparentGray));
                }

                if (intent != null) {
                    startActivity(intent);

                    Activity currentActivity = getActivity();
                    if (currentActivity != null) {
                        if (!currentActivity.getClass().equals(SonarLiveActivity.class)) {
                            currentActivity.finish();
                        }
                    }

                }
            }
        });
    }

    public boolean isOpen() {
        return _open;
    }

}
