// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.dsp;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.model.EchoPeak;
import com.reelsonar.ibobber.model.FishSonarData;
import com.reelsonar.ibobber.model.PingDataProcessor;
import com.reelsonar.ibobber.model.SonarData;
import com.reelsonar.ibobber.service.DemoSonarService;
import com.reelsonar.ibobber.service.LocationService;
import com.reelsonar.ibobber.util.GrowableIntArray;
import com.reelsonar.ibobber.weather.WeatherData;
import com.reelsonar.ibobber.weather.WeatherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public class SonarDataService implements Handler.Callback {

    private static final String TAG = SonarDataService.class.getSimpleName();

    private final static int BOBBER_MAX_AMPLITUDE_PING_COUNT = 10;

    private static final int MAX_FISH_PER_FRAME = 3;
    private static final int MAX_DATA_BUFFER_SIZE = 5;
    private static final int MEDIAN_DEPTH_WINDOW_SIZE = 3;
    private static final int MEDIAN_DEPTH_INDEX = 1;

    private static final int SEAWEED_DETECTIONS = 2;
    private static final double SEAWEED_DEPTH_DIFFERENCE_METERS = 1.2192;  // 4 feet.

    private static final int HOTSPOT_MIN_FISH = 3;
    private static final long HOTSPOT_WINDOW_SIZE_MS = TimeUnit.SECONDS.toMillis(50);  // TimeUnit.MINUTES.toMillis(3);
    // Time Interval for hotspot data upload.
    private static final long REMOTE_DB_HOTSPOT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30); // TimeUnit.MINUTES.toMillis(2);

    private static final int MESSAGE_ADD_SONAR_DATA = 0;
    private static final int MESSAGE_GET_SONAR_DATA = 1;
    private static final int MESSAGE_ADD_SONAR_DEMO_DATA = 2;
    private static final int MESSAGE_CLEAR = 3;

    private static SonarDataService INSTANCE;
    public static synchronized SonarDataService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SonarDataService(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private Context _context;
    private LinkedList<SonarData> _sonarBuffer;
    private LinkedList<FishSonarData> _fishHistory;
    private Handler _handler;
    private int _dataIndex;

    private double _previousDepth;
    private long _lastFishHistoryDBUpdateTime;
    private int _bobberMaxAmplitude;

    public SonarDataService(final Context context) {
        _context = context;
        _sonarBuffer = new LinkedList<>();
        _fishHistory = new LinkedList<>();

        HandlerThread thread = new HandlerThread(TAG + "-Processor");
        thread.start();
        _handler = new Handler(thread.getLooper(), this);

        EventBus.getDefault().register(this);
    }

    @Override
    public boolean handleMessage(final Message message) {
        switch (message.what) {
            case MESSAGE_ADD_SONAR_DATA:
            {
                PingDataProcessor processor = (PingDataProcessor)message.obj;
                addSonarDataFromPingDataProcessor(processor);
                return true;
            }
            case MESSAGE_GET_SONAR_DATA:
            {
                Handler returnHandler = (Handler)message.obj;
                getSonarDataHelper(returnHandler);
                return true;
            }
            case MESSAGE_ADD_SONAR_DEMO_DATA:
            {
                addSonarDataFromDemoService();
                return true;
            }

            case MESSAGE_CLEAR:
                clear();
                return true;
            default:
                return false;
        }
    }

    public double getDepth() {
        return _previousDepth;
    }

    public void getNextSonarData(final Handler returnHandler) {
        Message message = _handler.obtainMessage(MESSAGE_GET_SONAR_DATA, returnHandler);
        _handler.sendMessage(message);
    }

    public void onEvent(final PingDataProcessor processor) {
        Message message = _handler.obtainMessage(MESSAGE_ADD_SONAR_DATA, processor);
        _handler.sendMessage(message);
    }

    public void onEvent(final DemoSonarService testDataSonarService) {
        Message message = _handler.obtainMessage(MESSAGE_ADD_SONAR_DEMO_DATA, testDataSonarService);
        _handler.sendMessage(message);
    }

    public void onEvent(final BTService.DeviceDidConnect notification) {
        _handler.sendEmptyMessage(MESSAGE_CLEAR);
    }

    private void clear() {
        _sonarBuffer.clear();
        _fishHistory.clear();
        _dataIndex = 0;
        _previousDepth = 0;
        _bobberMaxAmplitude = 0;
    }

    private void addSonarDataFromPingDataProcessor(PingDataProcessor processor) {
        if (processor.getBottom() != null) {
            if (_dataIndex < BOBBER_MAX_AMPLITUDE_PING_COUNT) {
                for (int i = 0; i < processor.getPingAmplitudes().size(); i++) {
                    _bobberMaxAmplitude = Math.max(processor.getPingAmplitudes().get(i), _bobberMaxAmplitude);
                }
            }

            int temp = BTService.getSingleInstance().getTempCelsius();
            double bottomDepth = processor.getBottom().depth(_previousDepth, temp);
            _previousDepth = bottomDepth;

            List<EchoPeak> fishEchoPeaks = processor.getFishEchoPeaks();
            if (fishEchoPeaks.size() > MAX_FISH_PER_FRAME) {
                fishEchoPeaks = fishEchoPeaks.subList(0, MAX_FISH_PER_FRAME);
            }

            long now = SystemClock.uptimeMillis();
            List<FishSonarData> fishData = new ArrayList<>(fishEchoPeaks.size());
            for (EchoPeak echoPeak : fishEchoPeaks) {
                double fishDepth = echoPeak.depth(_previousDepth, temp);
                fishData.add(new FishSonarData(echoPeak.getFishSize(), echoPeak.getAmplitude(), fishDepth, now));
            }

            SonarData sonarData = new SonarData(fishData, processor, bottomDepth, _dataIndex, _bobberMaxAmplitude);
            ++_dataIndex;

            _sonarBuffer.addFirst(sonarData);
            if (_sonarBuffer.size() > MAX_DATA_BUFFER_SIZE) {
                _sonarBuffer.removeLast();
            }
        }
    }

    private void addSonarDataFromDemoService() {
        DemoSonarService demoSonarService = DemoSonarService.getSingleInstance(_context);

        SonarData sonarData = new SonarData(
                demoSonarService.getFishData(),
                demoSonarService.getPingDataProcessor(),
                demoSonarService.getDepthInMeters(),
                _dataIndex,
                DspConstants.MAX_AMPLITUDE
        );

        ++_dataIndex;

        _sonarBuffer.addFirst(sonarData);
        if (_sonarBuffer.size() > MAX_DATA_BUFFER_SIZE) {
            _sonarBuffer.removeLast();
        }
    }

    private void getSonarDataHelper(final Handler returnHandler) {
        SonarData sonarData;

        if (_sonarBuffer.size() == 0) {
            sonarData = null;
        } else if (_sonarBuffer.size() < MEDIAN_DEPTH_WINDOW_SIZE) {
            sonarData = _sonarBuffer.getFirst();
        } else {
            List<SonarData> sortedDepthWindow = new ArrayList<>(_sonarBuffer.subList(0, MEDIAN_DEPTH_WINDOW_SIZE));
            Collections.sort(sortedDepthWindow, new Comparator<SonarData>() {
                @Override
                public int compare(SonarData sonarData, SonarData sonarData2) {
                    return Double.compare(sonarData.getDepthMeters(), sonarData2.getDepthMeters());
                }
            });

            SonarData currentData = _sonarBuffer.getFirst();
            SonarData medianData = sortedDepthWindow.get(MEDIAN_DEPTH_INDEX);

            GrowableIntArray meanPingAmplitudes = new GrowableIntArray(DspConstants.PING_SIZE);

            for (int i = 0; i < DspConstants.PING_SIZE; i++) {
                int sumOfAmplitudes = 0;
                int amplitudeCount = 0;

                for (int w = 0; w < MEDIAN_DEPTH_WINDOW_SIZE; w++) {
                    SonarData windowSonarData = sortedDepthWindow.get(w);
                    GrowableIntArray medianPingAmplitudes = windowSonarData.getRawSonarPingDataProcessor().getMedianPingAmplitudes();

                    if (medianPingAmplitudes != null && i < medianPingAmplitudes.size()) {
                        sumOfAmplitudes += medianPingAmplitudes.get(i);
                        amplitudeCount++;
                    }
                }

                int meanAmplitude = sumOfAmplitudes / amplitudeCount;
                meanPingAmplitudes.add(meanAmplitude);
            }

            PingDataProcessor meanPingDataProcessor = new PingDataProcessor(meanPingAmplitudes);

            sonarData = new SonarData(currentData.getFish(), meanPingDataProcessor, medianData.getDepthMeters(),
                    currentData.getIndex(), currentData.getBobberMaxAmplitude());
        }

        boolean addToFishHistory = false;
        if (sonarData != null) {
            if (sonarData.getIndex() + 1 >= MAX_DATA_BUFFER_SIZE) {
                addSeaweedData(sonarData);
                addToFishHistory = true;
            } else {
                sonarData.setFish(Collections.<FishSonarData>emptyList());
            }
        }

        Message message = returnHandler.obtainMessage();
        message.obj = sonarData;
        returnHandler.sendMessage(message);


        if (addToFishHistory && BTService.getSingleInstance().getConnectedToDevice()
                && !DemoSonarService.getSingleInstance(_context).getDemoRunning()) {
            analyzeFishHistoryWithSonarData(sonarData);
        }
    }

    private void addSeaweedData(SonarData sonarData) {
        int seaweedCount = 0;
        double seaweedHeight = 0.0;

        List<FishSonarData> filteredFishData = new ArrayList<>(sonarData.getFish().size());

        boolean first = true;
        for (SonarData data : _sonarBuffer) {
            boolean seaweedDetection = false;

            for (FishSonarData fish : data.getFish()) {
                if (fish.getDepthMeters() + SEAWEED_DEPTH_DIFFERENCE_METERS >= data.getDepthMeters()) {
                    seaweedDetection = true;
                    if (first) {
                        seaweedHeight = Math.max(seaweedHeight, data.getDepthMeters() - fish.getDepthMeters());
                    }
                } else if (first) {
                    filteredFishData.add(fish);
                }
            }

            if (seaweedDetection) {
                seaweedCount++;
            }

            first = false;
        }

        if (seaweedCount >= SEAWEED_DETECTIONS) {
            sonarData.setFish(filteredFishData);
            sonarData.setSeaweedHeightMeters(seaweedHeight);
        }
    }

    private void analyzeFishHistoryWithSonarData(final SonarData sonarData) {

        boolean hasFish = false;

        _fishHistory.addAll(sonarData.getFish());

        long now = SystemClock.uptimeMillis();
        long fishWindowStart = now - HOTSPOT_WINDOW_SIZE_MS;
        while (_fishHistory.size() > 0 && _fishHistory.getFirst().getTimestamp() < fishWindowStart) {
            _fishHistory.removeFirst();
        }

        if (_lastFishHistoryDBUpdateTime > 0 && now - _lastFishHistoryDBUpdateTime < REMOTE_DB_HOTSPOT_INTERVAL_MS) {
            return;
        }

        Set<BigDecimal> uniqueDepths = new HashSet<>();
        for (FishSonarData fish : _fishHistory) {
            uniqueDepths.add(BigDecimal.valueOf(fish.getDepthMeters()).setScale(1, RoundingMode.HALF_UP));
        }

        if (uniqueDepths.size() >= HOTSPOT_MIN_FISH) {
            hasFish = true;
        }

        if( BTService.getSingleInstance().getWaterDetectionStatus() == 0 ) { // Ignore case where water is not detected
            Log.e(TAG, "Skipping hotspot data upload.  iBobber not in water");
            return;
        }

        String bobberAddress = BTService.getSingleInstance().getDeviceAddress();

        ParseObject hotspot = new ParseObject("Hotspot");

        hotspot.put("bobberId", bobberAddress );

        hotspot.put("hasFish", hasFish );

        boolean hasVegetation = (sonarData.getSeaweedHeightMeters() > 0) ? true : false;

        hotspot.put("hasVegetation", hasVegetation );

        hotspot.put("depth", sonarData.getDepthMeters());
        hotspot.put("waterTemp", BTService.getSingleInstance().getTempCelsius());

        Location location = LocationService.getInstance(_context).getLastLocation();
        if (location != null && location != LocationService.UNKNOWN_LOCATION) {
            hotspot.put("latitude", location.getLatitude());
            hotspot.put("longitude", location.getLongitude());

            ParseGeoPoint geoPoint = new ParseGeoPoint( location.getLatitude(), location.getLongitude() );
            hotspot.put("location", geoPoint);
        }

        WeatherData weatherData = WeatherService.getInstance(_context).getWeatherData();
        if (weatherData != null) {

            Number tempC = weatherData.getTempC();
            if( tempC != null)
                hotspot.put("airTemp", tempC);

            Number pressure = weatherData.getPressureMB();
            if( pressure != null )
                hotspot.put("pressure", pressure);

            String weatherCode = weatherData.getWeatherCode();
            if( weatherCode != null )
                hotspot.put("weatherCode", weatherCode);

            String cloudCode = weatherData.getCloudCode();
            if( cloudCode != null)
                hotspot.put("cloudCode", cloudCode);

            String windDirection = weatherData.getCardinalWindDirection();
            if( windDirection != null )
            hotspot.put("windDirection",windDirection);

            Number windSpeed = weatherData.getWindSpeedMPH();
            if( windSpeed != null)
                hotspot.put("windSpeed", windSpeed );
        }

        Calendar today = Calendar.getInstance();
        double moonPhase = WeatherService.moonPhaseForDate(today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.MONTH), today.get(Calendar.YEAR));
        hotspot.put("moonPhase", moonPhase);

        hotspot.saveEventually();
        _lastFishHistoryDBUpdateTime = now;
        Log.d(TAG, "Hotspot sent to remote DB");
    }
}
