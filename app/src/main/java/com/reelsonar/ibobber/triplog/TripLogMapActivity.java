// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.reelsonar.ibobber.R;

public class TripLogMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TripLogMapActivity";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    double lat, lng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Bundle extras = getIntent().getExtras();
        lat = extras.getDouble("latitude");
        lng = extras.getDouble("longitude");
    }

    @Override
    protected void onResume() {
        super.onResume();
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
