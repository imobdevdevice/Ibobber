// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.reelsonar.ibobber.drawer.DeviceDrawerFragment;
import com.reelsonar.ibobber.drawer.HomeDrawerFragment;
import com.reelsonar.ibobber.model.triplog.TripLog;
import com.reelsonar.ibobber.service.LocationService;
import com.reelsonar.ibobber.triplog.TripLogDetailActivity;
import com.reelsonar.ibobber.triplog.TripLogService;
import com.reelsonar.ibobber.util.Actions;
import com.reelsonar.ibobber.util.Style;
import com.reelsonar.ibobber.weather.SunMoonData;
import com.reelsonar.ibobber.weather.TideData;
import com.reelsonar.ibobber.weather.WeatherService;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

public class TodayActivity extends Activity implements LoaderManager.LoaderCallbacks<List<TripLog>>{

    private final static String TAG = "TodayActivity";

    private static View mFragmentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new TodayFragment())
                    .add(R.id.container, new DeviceDrawerFragment())
                    .add(R.id.container, new HomeDrawerFragment(), "HomeDrawer")
                    .commit();
        }
    }

    private void updateFromSunMoonData(SunMoonData sunMoonData) {
        DateFormat sdf = android.text.format.DateFormat.getTimeFormat(this);

        String sunrise = sdf.format(sunMoonData.getSunrise() * (long) 1000);
        String sunset = sdf.format(sunMoonData.getSunset()  * (long) 1000);
        String moonrise = sdf.format(sunMoonData.getMoonRise() * (long) 1000);
        String moonset = sdf.format(sunMoonData.getMoonSet() * (long) 1000);
        Log.d(TAG, "Moonrise: " + moonrise + " Moonset: " + moonset + " Sunrise: " + sunrise + " Sunset: " + sunset);

        TextView sunsetTextView = (TextView) mFragmentView.findViewById(R.id.sunsetTimeTextView);
        sunsetTextView.setText(sunset);
        TextView sunriseTextView = (TextView) mFragmentView.findViewById(R.id.sunriseTimeTextView);
        sunriseTextView.setText(sunrise);

        TextView moonSetTextView = (TextView) mFragmentView.findViewById(R.id.moonSetTimeTextView);
        moonSetTextView.setText(moonset);
        TextView moonRiseTextView = (TextView) mFragmentView.findViewById(R.id.moonRiseTimeTextView);
        moonRiseTextView.setText(moonrise);
    }

    private void updateFromTideData(TideData tideData) {
        DateFormat sdf = android.text.format.DateFormat.getTimeFormat(this);

        String highTideDate = sdf.format(tideData.getmHighTideDate());
        String lowTideDate = sdf.format(tideData.getmLowTideDate());

        TextView highTideTextView = (TextView) mFragmentView.findViewById(R.id.highTideTimeTextView);
        highTideTextView.setText(highTideDate);

        TextView lowTideTextView = (TextView) mFragmentView.findViewById(R.id.lowTideTimeTextView);
        lowTideTextView.setText(lowTideDate);
    }

    public void onEventMainThread(final SunMoonData sunMoonData) {
        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
        findViewById(R.id.locationError).setVisibility(View.GONE);
        findViewById(R.id.todayFragment).setVisibility(View.VISIBLE);
        updateFromSunMoonData(sunMoonData);
    }

    public void onEventMainThread(final TideData tideData) {
        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
        findViewById(R.id.locationError).setVisibility(View.GONE);
        findViewById(R.id.todayFragment).setVisibility(View.VISIBLE);
        updateFromTideData(tideData);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (hasFocus) {
            LocationService.getInstance(this).setupLocationRequestsIfNeeded();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        WeatherService weatherService = WeatherService.getInstance(this);

        weatherService.registerForWeatherUpdates(this);

        if (weatherService.getIsLocationAvailable() == false) {
            Log.e(TAG, "weatherService2: " + weatherService);
            findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
            findViewById(R.id.locationError).setVisibility(View.VISIBLE);
            findViewById(R.id.todayFragment).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        WeatherService.getInstance(this).unregisterForWeatherUpdates(this);
    }


    static public class TodayFragment extends PreferenceFragment {

        public TodayFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_today, container, false);
            mFragmentView = rootView;

            int[] textViews = {
                    R.id.sunsetTimeTextView,
                    R.id.sunriseTimeTextView,
                    R.id.moonSetTimeTextView,
                    R.id.moonRiseTimeTextView,
                    R.id.highTideTimeTextView,
                    R.id.lowTideTimeTextView
            };
            Typeface tf = Style.appTypeface(BobberApp.getContext());
            for (int id : textViews) {
                ((TextView)rootView.findViewById(id)).setTypeface(tf);
            }

            return rootView;
        }

     }

    public void onCalendarButton(View view){

        Bundle bundle = new Bundle();
        bundle.putLong("date", Calendar.getInstance().getTime().getTime());
        getLoaderManager().restartLoader(0, bundle, this);

    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        return TripLogService.getInstance(this).tripLogsForDate(args.getLong("date"));
    }

    @Override
    public void onLoadFinished(Loader<List<TripLog>> listLoader, List<TripLog> tripLogs) {

        if (tripLogs.size() > 1) {
            Intent intent = new Intent(Actions.TRIPLOG);
            intent.putExtra("dateToHighlight", Calendar.getInstance().getTime());
            startActivity(intent);
        }

        if (tripLogs.size() == 1) {

            Intent tripDetail = new Intent(this, TripLogDetailActivity.class);
            tripDetail.putExtra("idTrip",tripLogs.get(0).getIdTrip());
            startActivity(tripDetail);
        }

        getLoaderManager().destroyLoader(0);

    }

    @Override
    public void onLoaderReset(Loader<List<TripLog>> listLoader) {

    }



}
