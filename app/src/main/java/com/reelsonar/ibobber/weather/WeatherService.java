// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.weather;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import com.hamweather.aeris.communication.*;
import com.hamweather.aeris.communication.loaders.*;
import com.hamweather.aeris.communication.parameter.ParameterBuilder;
import com.hamweather.aeris.communication.parameter.PlaceParameter;
import com.hamweather.aeris.model.*;
import com.hamweather.aeris.response.ForecastsResponse;
import com.hamweather.aeris.response.ObservationResponse;
import com.hamweather.aeris.response.TidesResponse;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.service.LocationService;
import de.greenrobot.event.EventBus;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WeatherService {
    
    private static final String TAG = WeatherService.class.getSimpleName();

    private static final long UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);
    private static final float UPDATE_MIN_DISTANCE_METERS = 1609.34f; // 1 mile

    private static WeatherService INSTANCE;

    public static synchronized WeatherService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WeatherService(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private Context _context;
    private EventBus _eventBus;
    private long _lastUpdateTime;
    private Location _lastLocation;
    private Boolean _locationAvailable = false;

    private WeatherService(final Context context) {
        _context = context;
        _eventBus = new EventBus();

        String id = context.getString(R.string.aeris_client_id);
        String secret = context.getString(R.string.aeris_client_secret);

        AerisEngine.initWithKeys(id, secret, context);
        // AerisEngine.getInstance().setDebugLogcatEnabled(true);

        LocationService.getInstance(context).registerForLocationUpdates(this);
    }

    public void registerForWeatherUpdates(final Object subscriber) {
        _eventBus.registerSticky(subscriber);
    }

    public void unregisterForWeatherUpdates(final Object subscriber) {
        _eventBus.unregister(subscriber);
    }

    public WeatherData getWeatherData() {
        return _eventBus.getStickyEvent(WeatherData.class);
    }

    public SunMoonData getSunMoonData() {
        return _eventBus.getStickyEvent(SunMoonData.class);
    }

    public Boolean getIsLocationAvailable() { return _locationAvailable;}

    public void onEventMainThread(final Location location) {
        long now = SystemClock.uptimeMillis();
        if (location != LocationService.UNKNOWN_LOCATION) {
            if ((_lastUpdateTime == 0 || now - _lastUpdateTime > UPDATE_INTERVAL_MS)
                    && (_lastLocation == null || _lastLocation.distanceTo(location) > UPDATE_MIN_DISTANCE_METERS)) {
                _lastUpdateTime = now;
                _lastLocation = location;
                _locationAvailable = true;

                AerisLocation aerisLocation = new AerisLocation();
                aerisLocation.lat = location.getLatitude();
                aerisLocation.lon = location.getLongitude();
                PlaceParameter place = new PlaceParameter(aerisLocation);

                WeatherData weatherData = new WeatherData();
                AtomicInteger countDown = new AtomicInteger(2);
                loadObservations(place, weatherData, countDown);
                loadForecast(place, weatherData, countDown);
                loadSunMoon(aerisLocation);
                loadTides(place);
            }
        }
        else {
            _locationAvailable = false;

        }
    }

    private void loadObservations(final PlaceParameter place,
                                  final WeatherData weatherData,
                                  final AtomicInteger countDown)
    {
        ObservationsTask task = new ObservationsTask(_context, new ObservationsTaskCallback() {

            @Override
            public void onObservationsFailed(AerisError error) {
                Log.e(TAG, "onObservationsFailed error: " + error.description);
            }

            @Override
            public void onObservationsLoaded(List<ObservationResponse> responses) {
                ObservationResponse obResponse = responses.get(0);
                Observation ob = obResponse.getObservation();

                if ( ob != null ) {
                    weatherData.setTempF(ob.tempF);
                    weatherData.setTempC(ob.tempC);
                    weatherData.setWindSpeedMPH(ob.windSpeedMPH);
                    weatherData.setWindSpeedKPH(ob.windSpeedKPH);
                    weatherData.setWindDirection(ob.windDirDEG);
                    weatherData.setPressureIN(ob.pressureIN);
                    weatherData.setPressureMB(ob.pressureMB);
                    weatherData.setWeatherCode(ob.weatherCoded);
                    weatherData.setCloudCode(ob.cloudsCoded);
                    weatherData.setCardinalWindDirection( ob.windDir );
                    if( ob.icon != null) {
                        int pos = ob.icon.indexOf(".");
                        weatherData.setIcon(ob.icon.substring(0, pos));
                    }
                }

                if (countDown.decrementAndGet() == 0) {
                    _eventBus.postSticky(weatherData);
                }
            }

        });

//        task.withDebug(true);
        task.requestClosest(place);
    }



    private void loadForecast(final PlaceParameter place,
                              final WeatherData weatherData,
                              final AtomicInteger countDown)
    {
        ForecastsTask forecastsTask = new ForecastsTask(_context, new ForecastsTaskCallback() {

            @Override
            public void onForecastsFailed(AerisError error) {
                Log.e(TAG, "onForecastsFailed error: " + error.description);
            }

            @Override
            public void onForecastsLoaded(List<ForecastsResponse> fResponse) {
                ForecastsResponse fRes = fResponse.get(0);

                if ( fRes != null ) {
                    ForecastPeriod fp = fRes.getPeriod(0);

                    if (fp != null) {
                        weatherData.setMaxTempF(fp.maxTempF);
                        weatherData.setMaxTempC(fp.maxTempC);
                        weatherData.setMinTempF(fp.minTempF);
                        weatherData.setMinTempC(fp.minTempC);

                        weatherData.setRainFall(fp.pop);
                    }
                }

                if (countDown.decrementAndGet() == 0) {
                    _eventBus.postSticky(weatherData);
                }
            }
        });

//        forecastsTask.withDebug(true);
        forecastsTask.requestClosest(place);
    }

     private void loadTides(final PlaceParameter place)
    {
        TidesTask tidesTask = new TidesTask(_context, new TidesTaskCallback() {

            @Override
            public void onTidesFailed(AerisError error) {
                Log.e(TAG, "onTidesFailed error: " + error.description);
            }

            @Override
            public void onTidesLoaded(List<TidesResponse> tResponse) {
                TidesResponse tRes = tResponse.get(0);

                if ( tRes != null ) {

                    TideData tideData = new TideData();

                    Date highTideDate = null;
                    Date lowTideDate = null;

                    Float highTide = Float.MIN_VALUE;
                    Float lowTide  = Float.MAX_VALUE;

                    for( TidesPeriod tp : tRes.getPeriods()) {
                        if(tp.heightM.floatValue() > highTide ) {
                            highTide = tp.heightM.floatValue();
                            highTideDate = new Date( tp.timestamp.longValue() * 1000 );
                        }
                        if(tp.heightM.floatValue() < lowTide ) {
                            lowTide = tp.heightM.floatValue();
                            lowTideDate = new Date( tp.timestamp.longValue() * 1000 );
                        }
                    }
                    tideData.setmHighTideDate( highTideDate );
                    tideData.setmHighTide( highTide );
                    tideData.setmLowTideDate( lowTideDate );
                    tideData.setmLowTide( lowTide );
                    _eventBus.postSticky(tideData);
                }
            }
        });

        // tidesTask.withDebug(true);
        tidesTask.requestClosest(place);
    }

    private void loadSunMoon(final AerisLocation place) {
        Endpoint ep = new Endpoint("sunmoon");
        String id = ""; //set to ":auto" to enable location-by-IP (sometimes unreliable)
        ParameterBuilder builder = new ParameterBuilder()
                .withLimit(1)
                .withFrom("now")
                .withTo("+" + 1 + "days")
                .withPlace(place);

        AerisRequest request = new AerisRequest(ep, id, builder.build());

        //request.withDebugOutput(true);

        AerisCommunicationTask commTask = new AerisCommunicationTask(_context, new AerisCallback() {
            @Override
            public void onResult(EndpointType endpointType, AerisResponse response) {

                if (response != null) {

                    AerisDataJSON smResponse = response.getFirstResponse();

                    if (smResponse != null) {
                        SunMoonData sunMoonData = new SunMoonData();

                        //have seen issues where moon data exists - but set is null
                        if (smResponse.sun != null) {
                            if (smResponse.sun.rise != null) sunMoonData.setSunrise(smResponse.sun.rise.longValue());
                            sunMoonData.setSunriseISO(smResponse.sun.riseISO);
                            if (smResponse.sun.set != null) sunMoonData.setSunset(smResponse.sun.set.longValue());
                            sunMoonData.setSunsetISO(smResponse.sun.setISO);
                        }

                        if (smResponse.moon != null) {
                            if (smResponse.moon.rise != null) sunMoonData.setMoonRise(smResponse.moon.rise.longValue());
                            sunMoonData.setMoonRiseISO(smResponse.moon.riseISO);
                            if (smResponse.moon.set != null) sunMoonData.setMoonSet(smResponse.moon.set.longValue());
                            sunMoonData.setMoonSetISO(smResponse.moon.setISO);
                        }

                        _eventBus.postSticky(sunMoonData);
                    }
                }
            }
        }, request);

        commTask.execute();
    }

    private static double MOON_PHASE_LENGTH = 29.530588853;
    public static double moonPhaseForDate(int day, int month, int year) {

        double transformedYear = year - Math.floor((12.0 - (double) month) / 10.0);

        int transformedMonth = month + 9;

        if (transformedMonth >= 12) {
            transformedMonth = transformedMonth - 12;
        }

        double term1 = Math.floor(365.25 * (transformedYear + 4712));
        double term2 = Math.floor(30.6 * transformedMonth + 0.5);
        double term3 = Math.floor(Math.floor((transformedYear / 100) + 49) * 0.75) - 38;

        double intermediate = term1 + term2 + day + 59;

        if (intermediate > 2299160) {
            intermediate = intermediate - term3;
        }

        double normalizedPhase = (intermediate - 2451550.1) / MOON_PHASE_LENGTH;
        normalizedPhase = normalizedPhase - Math.floor(normalizedPhase);

        if (normalizedPhase < 0) {
            normalizedPhase = normalizedPhase + 1;
        }

        return normalizedPhase * MOON_PHASE_LENGTH;
    }

}
