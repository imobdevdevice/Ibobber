// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.reelsonar.ibobber.R;

public class TripLogMapActivity extends Activity implements OnMapReadyCallback {

    private static final String TAG = "TripLogMapActivity";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog_map);
        final Bundle extras = getIntent().getExtras();
        lat = extras.getDouble("latitude");
        lng = extras.getDouble("longitude");

        try {
            setUpMapIfNeeded();
        } catch (Exception e) {
            Log.i(TAG, "Map init failed - probably non-google play enabled phone");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
            // Check if we were successful in obtaining the map.
//            if (mMap != null) {
//                setUpMap();
//            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18.0f));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            setUpMap();
        }
    }
}
