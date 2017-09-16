// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.sonar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.reelsonar.ibobber.BaseActivity;
import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.LoginActivity;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.drawer.DeviceDrawerFragment;
import com.reelsonar.ibobber.drawer.HomeDrawerFragment;
import com.reelsonar.ibobber.dsp.SonarDataService;
import com.reelsonar.ibobber.model.FishSonarData;
import com.reelsonar.ibobber.model.PingDataProcessor;
import com.reelsonar.ibobber.model.SonarData;
import com.reelsonar.ibobber.model.UserAuth.UserAuth;
import com.reelsonar.ibobber.model.triplog.TripLog;
import com.reelsonar.ibobber.model.triplog.TripLogImages;
import com.reelsonar.ibobber.model.triplog.TripLogMain;
import com.reelsonar.ibobber.service.DemoSonarService;
import com.reelsonar.ibobber.service.LocationService;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.triplog.TripLogService;
import com.reelsonar.ibobber.util.Actions;
import com.reelsonar.ibobber.util.ApiLoader;
import com.reelsonar.ibobber.util.AppUtils;
import com.reelsonar.ibobber.util.CallBack;
import com.reelsonar.ibobber.util.MathUtil;
import com.reelsonar.ibobber.util.Sound;
import com.reelsonar.ibobber.view.CommandFragment;
import com.reelsonar.ibobber.view.CountdownFragment;
import com.reelsonar.ibobber.view.SonarView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Response;

import static com.reelsonar.ibobber.util.RestConstants.ACCESSS_TOKEN;
import static com.reelsonar.ibobber.util.RestConstants.CACHE_TITLE;
import static com.reelsonar.ibobber.util.RestConstants.CATCH_DEPTH;
import static com.reelsonar.ibobber.util.RestConstants.CATCH_H2O;
import static com.reelsonar.ibobber.util.RestConstants.CATCH_TEMPERATURE;
import static com.reelsonar.ibobber.util.RestConstants.IBOBBER_APP;
import static com.reelsonar.ibobber.util.RestConstants.LATITUDE;
import static com.reelsonar.ibobber.util.RestConstants.LONGITUDE;
import static com.reelsonar.ibobber.util.RestConstants.NETFISH_MODE;
import static com.reelsonar.ibobber.util.RestConstants.USERID;


public abstract class SonarActivity extends BaseActivity implements CommandFragment.CommandFragmentListener, CountdownFragment.CountdownListener {

    private final static int MAX_SONAR_LIVE_DATA_SIZE = 4;
    private final static int MAX_SONAR_LIVE_RAW_DATA_SIZE = 64;
    private final static int MAX_WATERBED_SONAR_DATA_SIZE = 8;
    private final static int MAX_SONAR_CAPTURE_SIZE = 120; // Two minutes worth.
    private final static int MAX_STATIC_SONAR_DATA = 60;
    private final static int BOBBER_OUT_OF_WATER_THRESHOLD = 3;
    private final static float RAW_SONAR_MIN_DEPTH = 6.096f; // 20 feet.
    private static final int PERMISSIONS_LOCATION_WRITE_STORAGE = 1;

    protected enum Mode {
        LIVE(new int[]{R.id.sonarControls, R.id.sonarStatusImage}),
        RAW(new int[]{R.id.sonarControls, R.id.sonarStatusImage}),
        EXPLORE(new int[]{R.id.homeDrawer, R.id.deviceDrawer});

        int[] _hiddenViews;

        Mode(int[] hiddenViews) {
            _hiddenViews = hiddenViews;
        }
    }

    private enum State {
        READY,
        CASTING,
        REELING,
        IMAGE
    }

    private final static String TAG = "SonarActivity";
    private final static String TAG_COMMAND = "SonarActivity.Command";
    private final static String TAG_COUNTDOWN = "SonarActivity.Countdown";

    private final boolean AMAZON_PROMO_ENABLED = true;
    private final int AMAZON_PROMO_DISPLAY_TIME = 15000;
    private final String AMAZON_PROMO_URL = "http://www.amazon.com/iBobber-Castable-Bluetooth-Smart-Fishfinder/dp/B00LEA2FS0";
    private final String BUY_BOBBER_URL = "http://reelsonar.com";

    private Mode _mode;
    private State _state;
    private SonarView _sonarView;
    private LinkedList<SonarData> _sonarData;

    private LinkedList<SonarData> _capturedSonarData;
    private long _captureStartTime;
    private long _captureEndTime;
    private long _bobberOutOfWaterCount;

    private ScheduledExecutorService _executorService;
    private Future<?> _updater;
    private Handler _sonarHandler;
    private Sound _sonarSound;

    private LinearLayout _promoLayout, _advertisementLayout, _advertisementExplorerModeLayout;
    private TextView _demoLabel, _demoExplorerModeLabel;

    protected abstract Mode getMode();

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        this);
                alert.setTitle(getString(R.string.location_permission_required));
                alert.setMessage(getString(R.string.are_you_sure));
                alert.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SonarActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSIONS_LOCATION_WRITE_STORAGE);
                        dialog.dismiss();
                    }
                });
                alert.create();
                alert.show();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                AlertDialog.Builder alert = new AlertDialog.Builder(
                        this);
                alert.setTitle(getString(R.string.storage_permission_required));
                alert.setMessage(getString(R.string.are_you_sure));
                alert.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SonarActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_LOCATION_WRITE_STORAGE);
                        dialog.dismiss();
                    }
                });
                alert.create();
                alert.show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_LOCATION_WRITE_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_LOCATION_WRITE_STORAGE) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            checkPermission();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getUserInfo() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_sonar);
        _mode = getMode();

        for (int id : _mode._hiddenViews) {
            findViewById(id).setVisibility(View.GONE);
        }

        _sonarData = new LinkedList<>();

        _promoLayout = (LinearLayout) findViewById(R.id.promoLayout);
        _advertisementLayout = (LinearLayout) findViewById(R.id.advertisementLayout);
        _advertisementExplorerModeLayout = (LinearLayout) findViewById(R.id.advertisementExplorerModeLayout);

        _demoLabel = (TextView) findViewById(R.id.demoLabel);
        _demoExplorerModeLabel = (TextView) findViewById(R.id.demoExplorerModeLabel);

        _sonarView = (SonarView) findViewById(R.id.sonarView);
        _sonarView.setSonarData(_sonarData);

        _executorService = Executors.newSingleThreadScheduledExecutor();
        _sonarHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleNewSonarData((SonarData) msg.obj);
            }
        };

        if (_mode == Mode.EXPLORE) {
            _sonarView.setPlotFish(false);
            _sonarView.setTopMargin(60);
            _sonarView.setBottomMargin(42);
            _sonarView.setShowDepthLabel(false);

            View tripLogButton = findViewById(R.id.imgGPS);
            FrameLayout.LayoutParams tripLogParams = (FrameLayout.LayoutParams) tripLogButton.getLayoutParams();
            tripLogParams.gravity = Gravity.RIGHT | Gravity.TOP;
            tripLogButton.setLayoutParams(tripLogParams);

            _capturedSonarData = new LinkedList<>();
            _capturedSonarData.addAll(UserService.getInstance(this).getLastCapturedSonarData());
            if (_capturedSonarData.size() > 0) {
                _state = State.IMAGE;
                showCapturedSonarData();
            } else {
                resetState();
            }

        } else {
            DeviceDrawerFragment deviceDrawer = (DeviceDrawerFragment) getFragmentManager().findFragmentById(R.id.deviceDrawer);

            if (_mode == Mode.RAW) {
                _sonarView.initRawSonar(MAX_SONAR_LIVE_RAW_DATA_SIZE);
                deviceDrawer.setLiveRawSonar();
            } else {
                _sonarView.setMaxDataFrames(MAX_SONAR_LIVE_DATA_SIZE);
                deviceDrawer.setLiveSonar();
            }

            findViewById(R.id.loadingSpinner).setVisibility(View.INVISIBLE);
            showInitialView();
        }

        if (BobberApp.getAdvertisementEnabled() == true) {
            showAdvertisement();
        }
    }

    private void showAdvertisement() {
        if (_mode == Mode.EXPLORE) {
            _advertisementExplorerModeLayout.bringToFront();
            _advertisementExplorerModeLayout.setVisibility(View.VISIBLE);
        } else {
            _advertisementLayout.bringToFront();
            _advertisementLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideAdvertisement() {
        _advertisementExplorerModeLayout.setVisibility(View.INVISIBLE);
        _advertisementLayout.setVisibility(View.INVISIBLE);
    }

    private void showInitialView() {
        //now show amazon promo on each upgrade (but not initial install)
        if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) || getIntent().hasCategory(Actions.CATEGORY_INITIAL_DEMO)) {
            if (!UserService.getInstance(this).userDidRegisteredWithThisVersion() &&
                    UserService.getInstance(this).checkIfIsFirstRunForThisVersion() && AMAZON_PROMO_ENABLED) {
                UserService.getInstance(this).recordAmazonPromoShown();
                _promoLayout.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(hideAmazonPromo, AMAZON_PROMO_DISPLAY_TIME);
            } else {
                HomeDrawerFragment homeDrawerFragment = (HomeDrawerFragment) getFragmentManager().findFragmentById(R.id.homeDrawer);
                homeDrawerFragment.toggleOpen();
            }
        }
    }

    private Runnable hideAmazonPromo = new Runnable() {
        @Override
        public void run() {
            _promoLayout.setVisibility(View.INVISIBLE);
        }
    };

    public void sendToPromoURL(final View view) {
        UserService.getInstance(this).recordAmazonPromoUserOnParse();
        _promoLayout.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AMAZON_PROMO_URL));
        startActivity(intent);
    }

    public void dismissPromo(final View view) {
        _promoLayout.setVisibility(View.INVISIBLE);
        HomeDrawerFragment homeDrawerFragment = (HomeDrawerFragment) getFragmentManager().findFragmentById(R.id.homeDrawer);
        homeDrawerFragment.toggleOpen();
    }

    public void sendToAdvertisementURL(final View view) {
        _advertisementLayout.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BUY_BOBBER_URL));
        startActivity(intent);
    }

    public void dismissAdvertisement(final View view) {

        _advertisementLayout.setVisibility(View.INVISIBLE);
        _advertisementExplorerModeLayout.setVisibility(View.INVISIBLE);

        BobberApp.setAdvertisementDismissed(true);
        BobberApp.setAdvertisementEnabled(false);

        if (!DemoSonarService.getSingleInstance(this).getDemoRunning())
            DemoSonarService.getSingleInstance(this).startSendingData();

        if (_mode == Mode.EXPLORE) {
            _demoExplorerModeLabel.setVisibility(View.VISIBLE);
            _demoLabel.setVisibility(View.INVISIBLE);

        } else {
            _demoLabel.setVisibility(View.VISIBLE);
            _demoExplorerModeLabel.setVisibility(View.INVISIBLE);
        }
    }

    private void showCastView() {
        String title = getString(R.string.cast_command_title);
        String subtitle = getString(R.string.cast_command_detail);

        getFragmentManager().beginTransaction()
                .add(R.id.container, CommandFragment.newInstance(title, subtitle, false), TAG_COMMAND)
                .commit();
    }

    private void showReelView() {
        String title = getString(R.string.reel_command_title);
        String subtitle = getString(R.string.reel_command_detail);

        getFragmentManager().beginTransaction()
                .add(R.id.container, CommandFragment.newInstance(title, subtitle, true), TAG_COMMAND)
                .commit();
    }

    private void showCountDownView() {
        CountdownFragment countdownFragment = new CountdownFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container, countdownFragment, TAG_COUNTDOWN)
                .commit();
        countdownFragment.start();
    }

    public void restartUpdate() {
        stopUpdate();
        startUpdate();
    }

    public void startUpdate() {
        if (_updater == null) {
            int dataRefreshRateMs = BTService.getSingleInstance().getDataRefreshRateMs();

            _updater = _executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    SonarDataService.getInstance(SonarActivity.this).getNextSonarData(_sonarHandler);
                }
            }, 1, dataRefreshRateMs, TimeUnit.MILLISECONDS);
        }
    }

    public void stopUpdate() {
        if (_updater != null) {
            _updater.cancel(false);
            _updater = null;
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        View container = findViewById(R.id.container);

        if (UserService.getInstance(this).getAntiGlare()) {
            container.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        } else {
            container.setBackgroundResource(R.drawable.background);
        }

        if ((DemoSonarService.getSingleInstance(this).getDemoRunning())) {
            if (_mode == Mode.EXPLORE) {
                _demoExplorerModeLabel.setVisibility(View.VISIBLE);
                _demoLabel.setVisibility(View.INVISIBLE);
            } else {
                _demoLabel.setVisibility(View.VISIBLE);
                _demoExplorerModeLabel.setVisibility(View.INVISIBLE);
            }
        } else {
            _demoLabel.setVisibility(View.INVISIBLE);
            _demoExplorerModeLabel.setVisibility(View.INVISIBLE);
        }

        _sonarView.updateGroundTextures();

        BobberApp.updateLastUserInteractionTimeStamp();
        LocationService.getInstance(this).restartLocationRequestsIfPaused();

        updateSonarStatus();
        EventBus.getDefault().register(this);

        if (_mode != Mode.EXPLORE) {
            _sonarSound = new Sound(this, R.raw.sonar1, 2);

            if (_mode == Mode.LIVE) {
                _sonarView.setMaxDataFrames(MAX_SONAR_LIVE_DATA_SIZE);
            } else {
                _sonarView.setMaxDataFrames(MAX_SONAR_LIVE_RAW_DATA_SIZE);
            }

            startUpdate();
        }

        BTService.getSingleInstance().enableSonar();

        if (BobberApp.getAdvertisementEnabled() == true) {
            showAdvertisement();
        } else {
            hideAdvertisement();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause");

        EventBus.getDefault().unregister(this);

        if (_mode != Mode.EXPLORE) {
            if (_sonarSound != null) {
                _sonarSound.release();
                _sonarSound = null;
            }
        }

        CountdownFragment countdownFragment = (CountdownFragment) getFragmentManager().findFragmentByTag(TAG_COUNTDOWN);
        if (countdownFragment != null) {
            countdownFragment.stop();
        }

        stopUpdate();

        LocationService.getInstance(this).pauseLocationRequests();
    }

    private void updateSonarStatus() {
        View sonarStatus = findViewById(R.id.sonarStatusImage);
        if (BTService.getSingleInstance() != null && BTService.getSingleInstance().getConnectedToDevice()) {
            sonarStatus.setBackgroundResource(R.drawable.drawer_bobber_on);
        } else {
            sonarStatus.setBackgroundResource(R.drawable.drawer_bobber_off);
        }
    }

    public void onEventMainThread(HomeDrawerFragment.HomeDrawerClosed notification) {
        if (!BobberApp.bobberHasSynched() && BobberApp.getAdvertisementDismissed() == false) {
            BobberApp.setAdvertisementEnabled(true);
            showAdvertisement();
        }
    }

    public void onEventMainThread(BTService.DeviceDidConnect notification) {
        BTService.getSingleInstance().enableSonar();
        updateSonarStatus();
        restartUpdate();
    }

    public void onEventMainThread(BTService.DeviceDidDisconnect notification) {
        stopUpdate();
        updateSonarStatus();
    }

    public void onEventMainThread(BTService.FirmwareVersionUpdated notification) {
        restartUpdate();
    }

    public void onEventMainThread(BTService.WaterDetectionUpdated notification) {

        int waterDetected = BTService.getSingleInstance().getWaterDetectionStatus();
        if (waterDetected == 1) {
            Log.d(TAG, "Bobber in water.");
            findViewById(R.id.outOfWaterLabel).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.outOfWaterLabel).setVisibility(View.VISIBLE);
            findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
            Log.d(TAG, "Bobber out of water.");
        }
    }

    public void onEventMainThread(final DeviceDrawerFragment.PauseRequested notification) {
        stopUpdate();
    }

    public void onEventMainThread(final DeviceDrawerFragment.ResumeRequested notification) {
        startUpdate();
    }


    private void startCapture() {
        _captureStartTime = SystemClock.uptimeMillis();

        _state = State.REELING;
        showReelView();
        _sonarView.setMaxDataFrames(MAX_WATERBED_SONAR_DATA_SIZE);
        _sonarView.getDepthAxisView().setVisibility(View.VISIBLE);
        _sonarView.getNewAxisView().setVisibility(View.VISIBLE);
        startUpdate();
    }

    private void stopCapture() {
        _captureEndTime = SystemClock.uptimeMillis();

        _state = State.IMAGE;
        stopUpdate();
        _sonarView.setVisibility(View.INVISIBLE);
        showCountDownView();
    }

    private void resetState() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        CommandFragment commandFragment = (CommandFragment) getFragmentManager().findFragmentByTag(TAG_COMMAND);
        if (commandFragment != null) {
            transaction.remove(commandFragment);
        }
        CountdownFragment countdownFragment = (CountdownFragment) getFragmentManager().findFragmentByTag(TAG_COUNTDOWN);
        if (countdownFragment != null) {
            countdownFragment.stop();
            transaction.remove(countdownFragment);
        }
        transaction.commit();
        findViewById(R.id.sonarExploreButton).setBackgroundResource(R.drawable.sonar_explore_active);

        _state = State.READY;
        showCastView();
        _sonarView.getDepthAxisView().setVisibility(View.INVISIBLE);
        _sonarView.getDistanceAxisView().setVisibility(View.INVISIBLE);

        _sonarView.setLakeFloorDepth(0);
        _bobberOutOfWaterCount = 0;
        _sonarData.clear();
        _capturedSonarData.clear();
        UserService.getInstance(this).setLastCapturedSonarData(_capturedSonarData);

        stopUpdate();
    }

    private double getCaptureDistanceMeters() {
        int speedFPS = UserService.getInstance(this).getSpeedFeetPerSecond();
        double speedMPS = MathUtil.feetToMeters(speedFPS);
        long captureIntervalSeconds = (_captureEndTime - _captureStartTime) / 1000;
        return speedMPS * captureIntervalSeconds;
    }

    private void updateDepth(LinkedList<SonarData> sonarDataList) {

        int depthMaxDepth = 0;

        if (sonarDataList == null) return;

        for (SonarData sonarDataItem : sonarDataList) {

            double sonarDataInNativeUnits = MathUtil.metersToUnitOfMeasure(sonarDataItem.getDepthMeters(), this);

            if (depthMaxDepth < sonarDataInNativeUnits) {
                depthMaxDepth = (int) Math.min(MathUtil.metersToUnitOfMeasure(SonarView.MAX_DEPTH_METERS, this),
                        sonarDataInNativeUnits);
            }

        }

        depthMaxDepth = MathUtil.roundToNearest(depthMaxDepth + 1, 2);

        if (_mode == Mode.RAW) {
            int rawModeMinDepth = (int) MathUtil.metersToUnitOfMeasure(RAW_SONAR_MIN_DEPTH, this);
            depthMaxDepth = Math.max(rawModeMinDepth, depthMaxDepth);
            _sonarView.setLakeFloorDepth(depthMaxDepth);
        } else {
            _sonarView.setLakeFloorDepth(depthMaxDepth);
        }

        _sonarView.getDepthAxisView().setMaxValue(depthMaxDepth);
        _sonarView.getDepthAxisView().setNumOfTicks(Math.min(depthMaxDepth, 10));

        if (_mode == Mode.RAW) {
            _sonarView.rawDataLayout();
        }

        _sonarView.invalidate();
    }

    private void showCapturedSonarData() {
        Fragment commandView = getFragmentManager().findFragmentById(R.id.commandView);
        if (commandView != null) {
            getFragmentManager().beginTransaction().remove(commandView).commit();
        }

        LinkedList<SonarData> captured;
        if (_capturedSonarData.size() > MAX_STATIC_SONAR_DATA) {
            captured = new LinkedList<>();
            int i = 0;
            for (SonarData sonarData : _capturedSonarData) {
                if (i % 2 == 0) {
                    captured.add(sonarData);
                }
                ++i;
            }
        } else {
            captured = _capturedSonarData;
        }

        UserService.getInstance(this).setLastCapturedSonarData(_capturedSonarData);

        updateDepth(_capturedSonarData);

        double captureDistanceMeters = getCaptureDistanceMeters();
        double captureDistance = MathUtil.metersToUnitOfMeasure(captureDistanceMeters, this);
        int distanceRounded = MathUtil.roundToNearest((int) Math.floor(captureDistance), 10);
        float pixelsPerUnitOfMeasurement = (float) _sonarView.getDistanceAxisView().getWidth() / (float) captureDistance;
        float distanceAxisWidth = pixelsPerUnitOfMeasurement * (float) distanceRounded;

        _sonarView.getDistanceAxisView().setWidthOverride(Math.round(distanceAxisWidth));
//        _sonarView.getDistanceAxisView().setVisibility(View.VISIBLE);
//        _sonarView.getDistanceAxisView().setMaxValue(distanceRounded);
        _sonarView.getDistanceAxisView().setVisibility(View.VISIBLE);
        _sonarView.getDistanceAxisView().setMaxValue(10);

//        _sonarView.getNewAxisView().setWidthOverride(Math.round(distanceAxisWidth));
//        _sonarView.getNewAxisView().setMaxValue(distanceRounded);
//        _sonarView.getNewAxisView().setVisibility(View.VISIBLE);

        _sonarView.setMaxDataFrames(captured.size());
        _sonarView.setSonarData(captured);
        _sonarView.setVisibility(View.VISIBLE);

        findViewById(R.id.sonarExploreButton).setBackgroundResource(R.drawable.sonar_explore_orange);
    }

    private void playSonarSoundIfNeeded() {
        if (_mode != Mode.EXPLORE && UserService.getInstance(this).isMotionAlarm()) {
            int numFish = 0;
            for (SonarData sonarData : _sonarData) {
                for (FishSonarData fish : sonarData.getFish()) {
                    if (fish.getDepthMeters() > 0 && fish.getDepthMeters() < sonarData.getDepthMeters()) {
                        ++numFish;
                    }
                }
            }
            if (numFish > 0) {
                if (_sonarSound != null) _sonarSound.play();
            }
        }
    }

    public void onEventMainThread(final PingDataProcessor.BobberOutOfWaterSoft notification) {

        _bobberOutOfWaterCount++;
        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
    }

    private void handleNewSonarData(final SonarData sonarData) {
        if (!BTService.getSingleInstance().getConnectedToDevice()) {
            return;
        }

        //display spinner if we're connected - but haven't gotten any data yet
        if ((sonarData == null || _sonarData.size() == 0) && _mode != Mode.EXPLORE && _bobberOutOfWaterCount == 0) {
            if (BTService.getSingleInstance().getConnectedToDevice()) {
                findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
            }
        }

        if (sonarData != null) {
            if ((_mode != Mode.EXPLORE
                    && _sonarData.size() > 0
                    && _sonarData.getFirst().getIndex() == sonarData.getIndex())) {
                Log.d(TAG, "repeat data");
            } else {
                _bobberOutOfWaterCount = 0;
            }

            if (_mode != Mode.EXPLORE
                    && _bobberOutOfWaterCount >= BOBBER_OUT_OF_WATER_THRESHOLD
                    && BTService.getSingleInstance().getConnectedToDevice()) {

                _sonarView.clear();

                if (BTService.getSingleInstance().hasWaterDetection()) { // If Bobber has hardware-based out-of-water detection, interpret BobberOutOfWaterSoft notifications as weak sonar signal notifications

                    if (BTService.getSingleInstance().getWaterDetectionStatus() == 1)  // Bobber is in water; but has weak signal (based on _bobberOutOfWaterCount having reached BOBBER_OUT_OF_WATER_THRESHOLD)
                        findViewById(R.id.weakSonarSignalLabel).setVisibility(View.VISIBLE);

                } else {
                    findViewById(R.id.outOfWaterLabel).setVisibility(View.VISIBLE);
                }
                findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
            } else {
                _sonarData.addFirst(sonarData);

                if (_sonarData.size() > _sonarView.getMaxDataFrames()) {
                    _sonarData.removeLast();
                }

                _sonarView.setSonarData(_sonarData);
                updateDepth(_sonarData);

                if (!BTService.getSingleInstance().hasWaterDetection())
                    findViewById(R.id.outOfWaterLabel).setVisibility(View.GONE);

                findViewById(R.id.weakSonarSignalLabel).setVisibility(View.GONE);
                findViewById(R.id.loadingSpinner).setVisibility(View.GONE);

                if (_mode == Mode.EXPLORE) {
                    Log.i(TAG, "new explore sonar data");
                    _capturedSonarData.addFirst(sonarData);
                    if (_capturedSonarData.size() == MAX_SONAR_CAPTURE_SIZE) {
                        Log.i(TAG, "stopping capture - max size hit");
                        stopCapture();
                    }
                }

                playSonarSoundIfNeeded();
            }
        }
    }

    @Override
    public void onOK(CommandFragment fragment) {
        getFragmentManager().beginTransaction().remove(fragment).commit();
        if (_state == State.READY) {
            _state = State.CASTING;
            showCountDownView();
            _sonarView.getDistanceAxisView().setVisibility(View.INVISIBLE);
        } else {
            stopCapture();
        }
    }

    @Override
    public void onCancel(CommandFragment fragment) {
        getFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();

        showCastView();
    }

    @Override
    public void onCountdownComplete(CountdownFragment fragment) {
        getFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
        if (_state == State.CASTING) {
            startCapture();
        } else {
            _state = State.IMAGE;
            showCapturedSonarData();
        }
    }

    public void onLiveSonarButtonPressed(View view) {
        Intent intent = new Intent(Actions.SONAR_LIVE);
        startActivity(intent);
        finish();
    }

    public void onExploreSonarButtonPressed(View view) {
        resetState();
    }

    public void onTripLogButtonPressed(final View view) {
        UserAuth auth = getUserInfo();
        TripLog newTripLog = TripLogService.getInstance(this).saveTripLogAtCurrentLocation();

        try {
            View captureView = this._sonarView;
            captureView.setDrawingCacheEnabled(true);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "IBOBBER_" + timeStamp + ".jpg";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageDir = new File(storageDir.toString() + File.separator + "ibobber");
            if (!imageDir.exists()) {
                imageDir.mkdir();
            }
            String fullPath = imageDir.getPath() + File.separator + imageFileName;
            Bitmap b = captureView.getDrawingCache();
            b.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(fullPath));

            TripLogImages tripLogImages = new TripLogImages(newTripLog.getIdTrip());
            tripLogImages.addImage(fullPath);

            if (AppUtils.getIntegerSharedpreference(this, NETFISH_MODE) == 1) {
                //API CALL
                createTripLog(newTripLog);

            } else {

                TripLogService.getInstance(this).saveTripLog(newTripLog, tripLogImages, null);
                AppUtils.showToast(this, getString(R.string.trip_log_trip_log_created));

            }


        } catch (Exception exc) {
            Log.e(TAG, "Error saving screen capture");
        }
    }

    private void createTripLog(TripLog newTripLog) {
        ApiLoader.createTripLog(SonarActivity.this, getTripInfo(newTripLog, getUserInfo()), new CallBack() {
            @Override
            public void onResponse(Call call, Response response, String msg) {
                String responseStr = response.body().toString();
                Log.d("responseString", responseStr);
                TripLogMain tripLog = (new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()).fromJson(responseStr, TripLogMain.class);
                if (tripLog.getStatus()) {
                    AppUtils.showToast(getApplicationContext(), getString(R.string.netfish_catch_created));
                } else {
                    AppUtils.showToast(getApplicationContext(), getString(R.string.error_cancel));
                }
            }

            @Override
            public void onFail(Call call, Throwable e) {
                AppUtils.showToast(getApplicationContext(), getString(R.string.err_network));
            }

            @Override
            public void onSocketTimeout(Call call, Throwable e) {
                AppUtils.showToast(getApplicationContext(), getString(R.string.err_timeout));
            }
        });
    }

    private HashMap<String, String> getTripInfo(TripLog newTripLog, UserAuth auth) {
//      "ibobber_app=1&catch_title=iBobber&accessToken=%@&userId=%@&latitude=%.6f&longitude=%.6f&catch_h2o=%@&catch_temperature=%@&catch_depth=%@", accessToken, userId, [latitude doubleValue], [longitude doubleValue], waterTempParam, airTempParam, depthParam];
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(IBOBBER_APP, "1");
        hashMap.put(CACHE_TITLE, "iBobber");
        hashMap.put(ACCESSS_TOKEN, auth.getAccessToken());
        hashMap.put(USERID, auth.getData().getUserId());
        hashMap.put(LATITUDE, String.valueOf(newTripLog.getLatitude()));
        hashMap.put(LONGITUDE, String.valueOf(newTripLog.getLongitude()));
        hashMap.put(CATCH_H2O, String.valueOf(newTripLog.getWaterTemp()));
        hashMap.put(CATCH_TEMPERATURE, String.valueOf(newTripLog.getAirTemp()));
        hashMap.put(CATCH_DEPTH, String.valueOf(newTripLog.getWaterDepth()));
        return hashMap;
    }


}
