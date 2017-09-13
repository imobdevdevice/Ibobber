// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.app.Activity;
import android.widget.ImageView;
import com.reelsonar.ibobber.weather.TideData;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.reelsonar.ibobber.service.LocationService;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.weather.SunMoonData;
import com.reelsonar.ibobber.weather.WeatherData;
import com.reelsonar.ibobber.weather.WeatherService;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WeatherActivity extends Activity {

    private final static String TAG = "WeatherActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        String dateString = (String) android.text.format.DateFormat.format("EEEE, MMM dd", new Date() );
        dateTextView.setText( dateString );

        configureMoonPhase();
    }

    private void configureMoonPhase() {

        Calendar todayCal = new GregorianCalendar();
        int day = todayCal.get(Calendar.DAY_OF_MONTH);
        int month = todayCal.get(Calendar.MONTH) + 1;
        int year = todayCal.get(Calendar.YEAR);

        double phase = WeatherService.moonPhaseForDate(day, month, year);
        Integer phaseValue = ((int) Math.floor(phase)) % 30;

        String moonStr = "moon" + phaseValue +"";
        ImageView moonPhaseImageView = (ImageView) findViewById(R.id.moonPhaseImageView);

        int resID = getResources().getIdentifier(moonStr, "drawable", getPackageName());

        if ( resID != 0 ) {
            moonPhaseImageView.setImageResource(resID);
        }
    }
    
    private void updateFromWeatherData(final WeatherData weatherData) {

        if( weatherData == null )
            return;

        String windDir = weatherData.getCardinalWindDirection();
        if( windDir == null )
            windDir = "-";

        TextView currentTempView = (TextView) findViewById(R.id.currentTempTextView);
        TextView maxTempView = (TextView) findViewById(R.id.maxTempTextView);
        TextView minTempView = (TextView) findViewById(R.id.minTempTextView);
        TextView percentRainView = (TextView) findViewById(R.id.percentRainTextView);
        TextView windView = (TextView) findViewById(R.id.windTextView);
        TextView curTemoDegView = (TextView) findViewById(R.id.currentTempDegTextView);
        TextView maxTemoDegView = (TextView) findViewById(R.id.maxTempDegTextView);
        TextView minTemoDegView = (TextView) findViewById(R.id.minTempDegTextView);
        TextView pressureView = (TextView) findViewById(R.id.pressureTextView);

        boolean metric = UserService.getInstance(this).isMetric();

        if (metric)  {
            currentTempView.setText(weatherData.getTempC().toString());
            curTemoDegView.setText("C");
            maxTemoDegView.setText("C");
            minTemoDegView.setText("C");
            maxTempView.setText(weatherData.getMaxTempC().toString());
            minTempView.setText(weatherData.getMinTempC().toString());
            percentRainView.setText("-- mm");
            if( weatherData.getWindSpeedKPH() != null )
                windView.setText(windDir + weatherData.getWindSpeedKPH().toString() + " KPH");
            windView.setText("- KPH");

            pressureView.setText(weatherData.getPressureMB().toString() + " MB");

        } else {
            currentTempView.setText(weatherData.getTempF().toString());
            curTemoDegView.setText("F");
            maxTemoDegView.setText("F");
            minTemoDegView.setText("F");
            maxTempView.setText(weatherData.getMaxTempF().toString());
            minTempView.setText(weatherData.getMinTempF().toString());
            percentRainView.setText("-- IN");
            if( weatherData.getWindSpeedMPH() != null )
                windView.setText(windDir + weatherData.getWindSpeedMPH().toString() + " MPH");
            else
                windView.setText("- MPH");

            pressureView.setText(weatherData.getPressureIN().toString() + " IN");
        }

        TextView rainFallView = (TextView) findViewById(R.id.percentRainTextView);
        rainFallView.setText(weatherData.getRainFall().toString() + " %");
    }

    private void updateFromSunMoonData(SunMoonData sunMoonData) {
        DateFormat sdf = android.text.format.DateFormat.getTimeFormat(this);

        String sunrise = sdf.format(sunMoonData.getSunrise() * (long) 1000);
        String sunset = sdf.format(sunMoonData.getSunset()  * (long) 1000);
        String moonrise = sdf.format(sunMoonData.getMoonRise() * (long) 1000);
        String moonset = sdf.format(sunMoonData.getMoonSet() * (long) 1000);
        Log.d(TAG, "Moonrise: " + moonrise + " Moonset: " + moonset + " Sunrise: " + sunrise + " Sunset: " + sunset);

        TextView sunsetTextView = (TextView) findViewById(R.id.sunsetTimeTextView);
        sunsetTextView.setText(sunset);
        TextView sunriseTextView = (TextView) findViewById(R.id.sunriseTimeTextView);
        sunriseTextView.setText(sunrise);

        TextView moonSetTextView = (TextView) findViewById(R.id.moonSetTimeTextView);
        moonSetTextView.setText(moonset);
        TextView moonRiseTextView = (TextView) findViewById(R.id.moonRiseTimeTextView);
        moonRiseTextView.setText(moonrise);
    }

    private void updateFromTideData(TideData tideData) {
        DateFormat sdf = android.text.format.DateFormat.getTimeFormat(this);

        String highTideDate = sdf.format(tideData.getmHighTideDate());
        String lowTideDate = sdf.format(tideData.getmLowTideDate());

        TextView highTideTextView = (TextView) findViewById(R.id.highTideTimeTextView);
        highTideTextView.setText(highTideDate);

        TextView lowTideTextView = (TextView) findViewById(R.id.lowTideTimeTextView);
        lowTideTextView.setText(lowTideDate);
    }

    public void onEventMainThread(final WeatherData weatherData) {
        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
        findViewById(R.id.weatherLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.locationError).setVisibility(View.GONE);

        updateFromWeatherData(weatherData);
    }

    public void onEventMainThread(final SunMoonData sunMoonData) {

        updateFromSunMoonData(sunMoonData);
    }

    public void onEventMainThread(final TideData tideData) {

        updateFromTideData(tideData);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (hasFocus) {
            LocationService.getInstance(this).setupLocationRequestsIfNeeded();
        }
    }

    public void closeWeatherActivity(View v) {

        finish();
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
            findViewById(R.id.weatherLayout).setVisibility(View.GONE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        WeatherService.getInstance(this).unregisterForWeatherUpdates(this);
    }

}
