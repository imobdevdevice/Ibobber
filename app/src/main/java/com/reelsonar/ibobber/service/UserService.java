// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.service;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;
import com.parse.*;

import com.reelsonar.ibobber.BuildConfig;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.model.SonarData;
import com.reelsonar.ibobber.bluetooth.BTService;
import de.greenrobot.event.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class UserService {

    public static class BobberPurchaseDateStatus{};

    private final String TAG = "UserService";

    public static final class LocalizationChangedNotification {}

    private static final boolean OVERRIDE_LANGUAGE_DETECTION = BuildConfig.OVERRIDE_LANGUAGE_DETECTION;
    private static final String OVERRIDE_LANGUAGE_CODE = BuildConfig.OVERRIDE_LANGUAGE_CODE;

    public static final String KEY_NICKNAME = "nickname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FAV_FISH = "favFish";

    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_OPERATING_SYSTEM = "operatingSystem";
    private static final String KEY_APP_VERSION = "appVersion";

    private static final String KEY_REGISTRATION_DATE = "regDate";
    private static final String KEY_VENDOR_ID = "idForVendor";

    private static final String KEY_BOBBER_USER_ID = "userId";
    private static final String KEY_BOBBER_ID = "bobberId";
    private static final String KEY_USER_ID = "objectId";
    private static final String KEY_BOBBER_ADDRESS = "macAddress";
    private static final String KEY_BOBBER_HW_VERSION = "hwVersion";
    private static final String KEY_BOBBER_FW_VERSION = "fwVersion";
    private static final String KEY_BOBBER_PURCHASE_DATE = "datePurchased";
    private static final String KEY_BOBBER_ACTIVATION_DATE = "activationDate";
    private static final String KEY_ANTIGLARE = "antiglare";

    private static final String KEY_METRIC = "metric";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_LANGUAGE = "language";

    private static final String KEY_REGISTERED_WITH_VERSION = "registeredWithVersion";
    private static final String KEY_VERSION_OF_APP_WHEN_LAST_RUN = "versionOfAppWhenLastRun";

    private static final String KEY_PROMO_NAME = "promoName";
    private static final String KEY_AMAZON_SALE_PROMO_SHOWN = "amazonScalePromoShown";
    private static final String AMAZON_SCALE_PROMO_VALUE = "scalePromo";

    private static final String PARSE_ID = "liCJnr5jUWaU5VjfylcLT4h8QzaaUrkPjNgHrQ1O";
    private static final String PARSE_SECRET = "qtTwZukPDhulj6cidxlveB5h1HAHGbqzU68h3mCc";

    private static final int DEFAULT_SPEED = 2;

    private static UserService INSTANCE;

    public static synchronized UserService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserService(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private Context _context;
    private String _nickname;
    private String _email;
    private String _userId;
    private List<FavoriteFish> _fish;
    private Date _registrationDate;
    private boolean _metric;
    private int _speedFeetPerSecond;
    private boolean _motionAlarm;
    private String _languageCode;
    private Boolean _antiGlareOn;
    private String _bobberAddress;

    private List<SonarData> _lastCapturedSonarData;

    private UserService(Context context) {
        _context = context;

        boolean useHostedParse = false;

        if( useHostedParse ) {

            Parse.initialize(context, PARSE_ID, PARSE_SECRET);

        } else {

            Parse.initialize(new Parse.Configuration.Builder(context)
                    .applicationId("reelsonar-parse")
                    .server("https://reelsonar-services.com/parse")
                    .build()
            );
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        _nickname = prefs.getString(KEY_NICKNAME, null);
        _email = prefs.getString(KEY_EMAIL, null);
        _userId = prefs.getString(KEY_USER_ID, null);

        long registrationTime = prefs.getLong(KEY_REGISTRATION_DATE, -1);
        if (registrationTime != -1) {
            _registrationDate = new Date(registrationTime);
        }

        _antiGlareOn = prefs.getBoolean(KEY_ANTIGLARE, false);

        _speedFeetPerSecond = prefs.getInt(KEY_SPEED, DEFAULT_SPEED);

        _lastCapturedSonarData = new ArrayList<>();

        String detectedLanguage = Locale.getDefault().getLanguage();

        if (OVERRIDE_LANGUAGE_DETECTION) {
            detectedLanguage = OVERRIDE_LANGUAGE_CODE;
        }

        _languageCode = prefs.getString(KEY_LANGUAGE, detectedLanguage);

        _metric = prefs.getBoolean(KEY_METRIC, shouldUseMetricForCurrentLanguage());

        updateDefaultLocale();
    }

    public boolean shouldUseMetricForCurrentLanguage() {
        if (_languageCode.equals("en") || _languageCode.equals("en_GB")) return(false);
        return true;
    }


    public void recordVersionFirstRegisteredWith() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_REGISTERED_WITH_VERSION, getVersionName());
        editor.apply();
    }

    public boolean checkIfIsFirstRunForThisVersion() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);

        String versionOfAppLastRun = prefs.getString(KEY_VERSION_OF_APP_WHEN_LAST_RUN, "1.0");
        String currentVersionName = getVersionName();
        boolean firstRunForThisversion = !versionOfAppLastRun.equals(currentVersionName);

        //record version of app run last time
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_VERSION_OF_APP_WHEN_LAST_RUN, getVersionName());
        editor.apply();

        return firstRunForThisversion;
    }


    public boolean userDidRegisteredWithThisVersion() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);

        String versionOriginallyRegistered = prefs.getString(KEY_REGISTERED_WITH_VERSION, "1.0");
        String currentVersionName = getVersionName();
        boolean userRegisteredWithThisVersion = versionOriginallyRegistered.equals(currentVersionName);

        return userRegisteredWithThisVersion;
    }



    public void recordAmazonPromoShown() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_AMAZON_SALE_PROMO_SHOWN, true);
        editor.apply();
    }

    public boolean amazonPromoWasShown() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean promoWasShown = prefs.getBoolean(KEY_AMAZON_SALE_PROMO_SHOWN, false);
        return promoWasShown;
    }


    public void recordAmazonPromoUserOnParse() {
        ParseObject promoInfo = new ParseObject("Promo");
        promoInfo.put(KEY_NICKNAME, _nickname);
        promoInfo.put(KEY_PROMO_NAME, AMAZON_SCALE_PROMO_VALUE);
        promoInfo.put(KEY_EMAIL, _email);
        promoInfo.saveEventually();
    }


    public void persistUserInfo(final String nickname,
                                final String email,
                                final List<FavoriteFish> fish)
    {
        _nickname = nickname;
        _email = email;
        _fish = fish;
        _registrationDate = new Date();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NICKNAME, nickname);
        editor.putString(KEY_EMAIL, email);
        editor.putLong(KEY_REGISTRATION_DATE, _registrationDate.getTime());
        editor.apply();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserDetail");
        query.whereEqualTo(KEY_EMAIL, prefs.getString( KEY_EMAIL, "") );

        query.findInBackground(new FindCallback<ParseObject>() {

            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    final ParseObject userDetail;

                    if( list == null || list.size() == 0 ) {

                        userDetail = new ParseObject("UserDetail");

                    } else {

                        userDetail = list.get(0);
                        String userId = userDetail.getObjectId();

                        if (userId != null) {
                            _userId = userId;
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(KEY_USER_ID, userId);
                            editor.apply();
                        }
                    }
                    userDetail.put(KEY_NICKNAME, nickname);
                    userDetail.put(KEY_EMAIL, email);
                    userDetail.put(KEY_VENDOR_ID, getUUID());

                    String osVersion = "Android " + Build.VERSION.RELEASE;
                    userDetail.put(KEY_OPERATING_SYSTEM, osVersion);

                    String device =  android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";

                    Display display = ((WindowManager) _context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    int height = size.y;

                    DisplayMetrics metrics = new DisplayMetrics();
                   ((WindowManager) _context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
                    final String displayDpiInfo = getDpiInfo( metrics.densityDpi );
                    String metricsInfo =  metrics.densityDpi + " dpi (" + displayDpiInfo + ")";

                    userDetail.put(KEY_DEVICE_TYPE, device + ", display: " + width + "x" + height + ", " + metricsInfo);

                    userDetail.put(KEY_APP_VERSION, BuildConfig.VERSION_NAME + ", Build: " + BuildConfig.VERSION_CODE);

                    if (fish != null) {
                        List<Integer> fishIds = new ArrayList<>(fish.size());
                        for (FavoriteFish f : fish) {
                            fishIds.add(f.getId());
                        }
                        userDetail.put(KEY_FAV_FISH, fishIds);
                    } else {
                        userDetail.put(KEY_FAV_FISH, Collections.emptyList());
                    }

                    userDetail.saveInBackground( new SaveCallback() {

                        public void done(ParseException e) {

                             if (e == null) {

                                 String userId = userDetail.getObjectId();

                                 if( userId != null ) {
                                     _userId = userId;
                                     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                                     SharedPreferences.Editor editor = prefs.edit();
                                     editor.putString(KEY_USER_ID, userId);
                                     editor.apply();
                                 }
                             } else {
                               Log.e(TAG, "Failure to get response from Parse when persisting user info");
                             }
                           }
                         });



                } else {
                    Log.d(TAG, "persistUserInfo() Go Error: " + e.getMessage());
                }
            }
        });
    }

    public void persistBobberInfo() {

        final String bobberAddress = BTService.getSingleInstance().getDeviceAddress();
        final String fwVersion = BTService.getSingleInstance().getFirmwareRev();
        final String hwVersion = "" + BTService.getSingleInstance().getHardwareRev();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);

        final String email = prefs.getString( KEY_EMAIL, "");
        final String userId = prefs.getString( KEY_USER_ID, "");

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BOBBER_ADDRESS, bobberAddress);
        editor.apply();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ibobbers");
        query.whereEqualTo(KEY_BOBBER_ID, bobberAddress );

        query.findInBackground(new FindCallback<ParseObject>() {

            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {

                    if( list == null )
                        return;

                    ParseObject bobber;

                    if( list.size() == 0 ) {
                        // New bobber for this user
                        bobber = new ParseObject("ibobbers");
                        bobber.put( KEY_BOBBER_ID, bobberAddress ); // On Android, mac address is also the Parse bobberId value
                        bobber.put( KEY_BOBBER_ADDRESS, bobberAddress );
                        bobber.put( KEY_BOBBER_HW_VERSION, hwVersion );
                        bobber.put( KEY_BOBBER_ACTIVATION_DATE, new Date() );

                        EventBus.getDefault().post(new BobberPurchaseDateStatus());

                    } else {
                        bobber = list.get(0);

                        Date datePurchased = bobber.getDate(KEY_BOBBER_PURCHASE_DATE);
                        if( datePurchased != null ) {

                            BTService.getSingleInstance().setDatePurchased(datePurchased);
                            // Todo: Persist locally if not already stored

                            EventBus.getDefault().post(new BobberPurchaseDateStatus());

                        } else {

                            datePurchased = BTService.getSingleInstance().getDatePurchased();
                            if( datePurchased != null ) {
                                bobber.put( KEY_BOBBER_PURCHASE_DATE, datePurchased );

                                // Todo: Persist locally if not already stored

                            } else {
                                EventBus.getDefault().post(new BobberPurchaseDateStatus());
                            }
                        }
                    }

                    if( userId != null )
                        bobber.put( KEY_BOBBER_USER_ID, userId );       // Firmware version and possibly associated userId are the only properties of an iBobber that will change over time
                    if( fwVersion != null )
                        bobber.put( KEY_BOBBER_FW_VERSION, fwVersion );

                    bobber.saveInBackground();
                } else {
                    Log.d(TAG, "persistBobberInfo() Go Error: " + e.getMessage());
                }
            }
        });
    }

public void fetchUserId() {

    final String email = _email;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);

    ParseQuery<ParseObject> query = ParseQuery.getQuery("UserDetail");
    query.whereEqualTo(KEY_EMAIL, email );

    query.findInBackground(new FindCallback<ParseObject>() {

        public void done(List<ParseObject> list, ParseException e) {

            if (e == null) {

                final ParseObject userDetail;

                if( list == null || list.size() == 0 ) {

                    Log.e(TAG, "fetchUserId() failed to find a UserDetail object matching email: " + email );
                    return;
                }

                userDetail = list.get(0);
                String userId = userDetail.getObjectId();

                if (userId != null) {

                    _userId = userId;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USER_ID, userId);
                    editor.apply();
                }

            } else {
                Log.e(TAG, "fetchUserId() Go Error: " + e.getMessage());
            }
        }
        });
    }

    public String getVersionName() {
        String versionName = "";
        try {
            PackageInfo pInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ex) {
            versionName = "1.0";
        }
        return versionName;
    }

    public String getNickname() {
        return _nickname;
    }

    public String getEmail() { return _email; }

    public String getUserId() { return _userId; }

    public List<FavoriteFish> getFish() {
        return _fish;
    }

    public Date getRegistrationDate() {
        return _registrationDate;
    }

    public boolean isMetric() {
        return _metric;
    }


    public void setAntiGlare(boolean antiGlareOn) {
        _antiGlareOn = antiGlareOn;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ANTIGLARE, antiGlareOn);
        editor.apply();
    }

    public boolean getAntiGlare() {
        return _antiGlareOn;
    }

    public void setMetric(boolean metric) {
        _metric = metric;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_METRIC, metric);
        editor.apply();
    }

    public int getSpeedFeetPerSecond() {
        return _speedFeetPerSecond;
    }

    public void setSpeedFeetPerSecond(int speedFeetPerSecond) {
        _speedFeetPerSecond = speedFeetPerSecond;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SPEED, speedFeetPerSecond);
        editor.apply();
    }

    public boolean isMotionAlarm() {
        return _motionAlarm;
    }

    public void setMotionAlarm(boolean motionAlarm) {
        _motionAlarm = motionAlarm;
    }

    public String getLanguageCode() {
        return _languageCode;
    }

    public void setLanguageCode(final String languageCode) {
        boolean postNotification = !languageCode.equals(_languageCode);
        _languageCode = languageCode;

        setMetric(shouldUseMetricForCurrentLanguage());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();

        updateDefaultLocale();

        if (postNotification) {
            EventBus.getDefault().post(new LocalizationChangedNotification());
        }

    }

    public void updateDefaultLocale() {

        StringTokenizer tokens = new StringTokenizer(_languageCode, "_");
        String languagePrefix = tokens.nextToken();
        String localeSuffix = "";
        if (tokens.hasMoreElements()) localeSuffix = tokens.nextToken();

        Locale locale = new Locale(languagePrefix, localeSuffix);

        Configuration config = new Configuration();
        config.locale = locale;
        _context.getResources().updateConfiguration(config, _context.getResources().getDisplayMetrics());
    }

    public List<SonarData> getLastCapturedSonarData() {
        return _lastCapturedSonarData;
    }

    public void setLastCapturedSonarData(List<SonarData> lastCapturedSonarData) {
        _lastCapturedSonarData.clear();
        _lastCapturedSonarData.addAll(lastCapturedSonarData);
    }

    private static final String INSTALLATION = "INSTALLATION";
    private String getUUID() {
        File installation = new File(_context.getFilesDir(), INSTALLATION);
        try {
            if (!installation.exists())
                writeInstallationFile(installation);
            return readInstallationFile(installation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

    private static String getDpiInfo( int dpi ) {

        String dpiInfo = "U/K";
        switch( dpi ) {

            case DisplayMetrics.DENSITY_LOW:  // 120
                dpiInfo = "ldpi";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:  // 160 (aka DENSITY_DEFAULT)
                dpiInfo = "mdpi";
                break;
            case DisplayMetrics.DENSITY_TV:  // 200 (commonly seen for some 7" tablets)
                dpiInfo = "tv / 7in tablet";
                break;
            case DisplayMetrics.DENSITY_HIGH:  // 240
                dpiInfo = "hdpi";
                break;
            case DisplayMetrics.DENSITY_XHIGH: // 320
                dpiInfo = "xhdpi";
                break;
            case DisplayMetrics.DENSITY_XXHIGH: // 480
                dpiInfo = "xxhdpi";
                break;
            case 560: // 560
                dpiInfo = "xxhdpi > dpi < xxxhdpi";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH: // 640
                dpiInfo = "xxxhdpi";
                break;
        }
        return dpiInfo;
    }

}
