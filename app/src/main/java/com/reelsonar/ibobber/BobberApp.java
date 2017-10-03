// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.dsp.SonarDataService;
import com.reelsonar.ibobber.dsp.TestSonarDataService;
import com.reelsonar.ibobber.service.DemoSonarService;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.service.WearService;
import com.reelsonar.ibobber.util.RestConstants;
import com.reelsonar.ibobber.weather.WeatherService;

import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class BobberApp extends Application {

    private Tracker _gaTracker;

    private static Context _context;
    private static long _lastUserInteractionTimeStamp;

    private static final String KEY_BOBBER_SYNCHED = "bobberSynched";
    private static boolean _bobberHasSynched;
    private static boolean _advertisementEnabled = false;
    private static boolean _advertisementDismissed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        MultiDex.install(this);
//        Fabric.with(this, new Crashlytics());

        Stetho.InitializerBuilder initializerBuilder =
                Stetho.newInitializerBuilder(this);

// Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(this)
        );

        // Enable command line interface
        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(this)
        );

// Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();

// Initialize Stetho with the Initializer
        Stetho.initialize(initializer);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.enableAutoActivityReports(this);
        _gaTracker = analytics.newTracker(R.xml.ga_tracker);
        _gaTracker.enableAdvertisingIdCollection(true);
        _gaTracker.enableAutoActivityTracking(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        _bobberHasSynched = prefs.getBoolean(KEY_BOBBER_SYNCHED, false);

        Intent btServiceIntent = new Intent(this, BTService.class);
        startService(btServiceIntent);

        UserService.getInstance(this);
        DemoSonarService.getSingleInstance(this);
        TestSonarDataService.getInstance(this);
        SonarDataService.getInstance(this);
        WeatherService.getInstance(this);

        checkforUpgradeCase();

        updateLastUserInteractionTimeStamp();

        _context = this.getApplicationContext();

        WearService.getSingleInstance();

        //AppsFlyer
        //AppsFlyer
        AppsFlyerLib.getInstance().startTracking(this, RestConstants.APPS_FLYER_KEY);

        AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {
            @Override
            public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Log.d(AppsFlyerLib.LOG_TAG, "attribute: " + attrName + " = " +
                            conversionData.get(attrName));
                }

            }

            @Override
            public void onInstallConversionFailure(String errorMessage) {
                Log.d(AppsFlyerLib.LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> conversionData) {
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(AppsFlyerLib.LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }
        });


    }

    public static boolean bobberHasSynched() {

        return BobberApp._bobberHasSynched;
    }

    public static void setBobberHasSynched(boolean bobberHasSynched) {
        BobberApp._bobberHasSynched = bobberHasSynched;

        SharedPreferences userPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putBoolean(KEY_BOBBER_SYNCHED, true);
        editor.apply();
    }

    public static boolean getAdvertisementDismissed() {
        return _advertisementDismissed;
    }

    public static void setAdvertisementDismissed(boolean _advertisementDismissed) {
        BobberApp._advertisementDismissed = _advertisementDismissed;
    }

    public static boolean getAdvertisementEnabled() {
        return _advertisementEnabled;
    }

    public static void setAdvertisementEnabled(boolean _advertisementEnabled) {
        BobberApp._advertisementEnabled = _advertisementEnabled;
    }


    private static void checkforUpgradeCase() {

        if (UserService.getInstance(_context).userDidRegisteredWithThisVersion()) {

            if (UserService.getInstance(_context).getUserId() == null)
                UserService.getInstance(_context).fetchUserId();
        }
    }

    public static Context getContext() {
        return _context;
    }

    public static Long getLastUserInteraction() {
        return _lastUserInteractionTimeStamp;
    }

    public static void updateLastUserInteractionTimeStamp() {
        _lastUserInteractionTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        UserService.getInstance(this).updateDefaultLocale();
    }

    public Tracker getGaTracker() {
        return _gaTracker;
    }
}
