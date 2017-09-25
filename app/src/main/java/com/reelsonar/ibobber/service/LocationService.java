// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.reelsonar.ibobber.BobberApp;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public class LocationService {
    public static final Location UNKNOWN_LOCATION = new Location((String) null);

    private static final String TAG = LocationService.class.getSimpleName();
    private static final float MIN_DISTANCE_METERS = 5.f;
    private static final int UPDATE_PERIOD_MINUTES = 3;
    private static final int PAUSE_LOCATION_AFTER_MINUTES_USER_INACTIVITY = 15;

    private Context _context;
    private EventBus _eventBus;
    private LocationManager _locationManager;
    private LocationListener _locationListener;
    private boolean _locationUpdatesPaused = false;

    private static LocationService INSTANCE;

    public static synchronized LocationService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LocationService(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private LocationService(final Context context) {
        _context = context;
        _eventBus = new EventBus();

        _locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);

        setupLocationListener();

        setupLocationRequests();
    }

    public Location getLastLocation() {
        return _eventBus.getStickyEvent(Location.class);
    }

    public void registerForLocationUpdates(final Object subscriber) {
        _eventBus.registerSticky(subscriber);
    }

    public void unregisterForLocationUpdates(final Object subscriber) {
        _eventBus.unregister(subscriber);
    }

    public void setupLocationListener() {
        _locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                if (location == null) {
                    return;
                }

                Location lastLocation = getLastLocation();

                if ((System.currentTimeMillis() - BobberApp.getLastUserInteraction()) > TimeUnit.MINUTES.toMillis(PAUSE_LOCATION_AFTER_MINUTES_USER_INACTIVITY)) {
                    Log.d(TAG, "User inactive - pausing GPS");
                    pauseLocationRequests();
                }

                Log.i(TAG, "Time since last user interaction: " + (System.currentTimeMillis() - BobberApp.getLastUserInteraction()));

                if (lastLocation != null && lastLocation != UNKNOWN_LOCATION) {

                    long timeDelta = location.getTime() - lastLocation.getTime();

                    if (timeDelta <= TimeUnit.MINUTES.toMillis(UPDATE_PERIOD_MINUTES)) {

                        if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider()) &&
                                !LocationManager.NETWORK_PROVIDER.equals(lastLocation.getProvider())) {

                            Log.i(TAG, "Got new network location - but still have fresh GPS / Fused location - ignoring");
                            return;
                        }
                    }
                }

                Log.i(TAG, "Using new location");

                _eventBus.postSticky(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    public void setupLocationRequests() {

        if (_locationManager == null || _locationListener == null) return;

        if (!_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            _eventBus.postSticky(UNKNOWN_LOCATION);
            return;
        }

        //try to provide cached location immediately
        checkForCachedLocation();

        if (_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            } else {
                _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TimeUnit.MINUTES.toMillis(UPDATE_PERIOD_MINUTES), MIN_DISTANCE_METERS, _locationListener);
            }

        }
        if (_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            } else {
                _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TimeUnit.MINUTES.toMillis(UPDATE_PERIOD_MINUTES), MIN_DISTANCE_METERS, _locationListener);
            }

    }

    public void pauseLocationRequests() {

        Log.d(TAG, "Pausing location requests ...");

        if (_locationManager == null) return;
        _locationManager.removeUpdates(_locationListener);
        _locationUpdatesPaused = true;
    }

    public void restartLocationRequestsIfPaused() {
        Log.d(TAG, "Restarting location requests ...");
        if (_locationUpdatesPaused) {
            setupLocationRequests();
            _locationUpdatesPaused = false;
        }
    }

    public void setupLocationRequestsIfNeeded() {
        if (_locationManager == null) return;

        Location location = getLastLocation();
        if (location == null || location == LocationService.UNKNOWN_LOCATION || _locationUpdatesPaused) {
            setupLocationRequests();
            _locationUpdatesPaused = false;
        }
    }


    private void checkForCachedLocation() {

        if (_locationManager == null) return;

        if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastLocation == null)
            lastLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (lastLocation != null) {
            Log.i(TAG, "Got cached location: " + lastLocation);
            _eventBus.postSticky(lastLocation);
        }
    }


}
