// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.service;

import android.content.Context;
import android.os.Handler;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.dsp.DspConstants;
import com.reelsonar.ibobber.dsp.TestSonarDataService;
import com.reelsonar.ibobber.model.FishSize;
import com.reelsonar.ibobber.model.FishSonarData;
import com.reelsonar.ibobber.model.PingDataProcessor;
import com.reelsonar.ibobber.util.GrowableIntArray;
import de.greenrobot.event.EventBus;

import java.util.*;

public class DemoSonarService {
    private static DemoSonarService singleInstance = null;

    public static DemoSonarService getSingleInstance(final Context context) {

        if (singleInstance == null) {
            singleInstance = new DemoSonarService(context);
        }
        return singleInstance;
    }

    private final static Comparator<Integer> DESC_COMPARATOR = new Comparator<Integer>() {
        @Override
        public int compare(final Integer lhs, final Integer rhs) {
            return rhs.compareTo(lhs);
        }
    };

    private final static int DEMO_TEMP_C = 14;
    private final static int DEMO_BATTERY_PERCENT = 92;
    private final static double MIN_DEPTH = 3.048; // 10 feet.
    private final static int MIN_FISH_AMPLITUDE = DspConstants.MAX_AMPLITUDE / 2;
    private final static int RAW_BOTTOM_INDEX = DspConstants.PING_SIZE / 2;

    private Context _context;

    private boolean _seaWeedMode = false;
    private int _seaWeedCount = 0;
    private FishSonarData[] _fishSonarEchoData;
    private boolean _demoRunning = false;

    private Random _random = new Random();
    private Handler _dataSendHandler = new Handler();

    private int _amplitudeSeed;
    private int _randomDataCount;

    private int _depthCount;
    private double _depthInMeters = 0;

    private DemoSonarService(final Context context) {
        _context = context;
        _amplitudeSeed = 5;
        _randomDataCount = 0;
    }

    public double getDepthInMeters() {
        return _depthInMeters;
    }

    public List<FishSonarData> getFishData() {
        if (_fishSonarEchoData != null) return Arrays.asList(_fishSonarEchoData);
        return (new ArrayList<>());
    }

    public void startSendingData() {
        _demoRunning = true;

        BTService.getSingleInstance().disconnectBobber();
        _dataSendHandler.postDelayed(runnable, BTService.DATA_REFRESH_RATE_MS_1_1_OR_NEWER);
        EventBus.getDefault().post(new BTService.DeviceDidConnect());

        // Send test file data.
        //sendTestFileData();
    }

    public void stopSendingData() {
        _demoRunning = false;
        _dataSendHandler.removeCallbacksAndMessages(null);
        EventBus.getDefault().post(new BTService.DeviceDidDisconnect());
    }

    public boolean getDemoRunning() {
        return (_demoRunning);
    }

    public PingDataProcessor getPingDataProcessor() {
        List<Integer> rawPingAmplitudes = new ArrayList<>(DspConstants.PING_SIZE);
        int maxAmplitude = DspConstants.MAX_AMPLITUDE;

        // 1. First fill the ping with a random sample of low amplitudes.
        for (int i = 0; i < DspConstants.PING_SIZE; i++) {
            rawPingAmplitudes.add(_random.nextInt((int)(maxAmplitude * 0.1)));
        }

        // 2. Add some random "below surface" amplitudes, which decay from maxAmplitude just below the surface,
        // down towards lower amplitudes well below the surface.
        int belowSurfaceAmplitudesCount = _random.nextInt(10) + 20;
        List<Integer> belowSurfaceAmplitudes = new ArrayList<>(belowSurfaceAmplitudesCount);

        for (int i = 0; i < belowSurfaceAmplitudesCount; i++) {
            belowSurfaceAmplitudes.add(getNextRandomAmplitudeForMax(maxAmplitude));
        }

        Collections.sort(belowSurfaceAmplitudes, DESC_COMPARATOR);

        // 3. Set max amplitude.
        rawPingAmplitudes.set(RAW_BOTTOM_INDEX, maxAmplitude);

        for (int i = 0; i < belowSurfaceAmplitudes.size(); i++) {
            rawPingAmplitudes.set(RAW_BOTTOM_INDEX + i + 1, belowSurfaceAmplitudes.get(i));
        }

        _randomDataCount++;

        if (_randomDataCount % 5 == 0) {
            _amplitudeSeed++;

            if (_amplitudeSeed > 10) {
                _amplitudeSeed = 1;
            }
        }

        return new PingDataProcessor(new GrowableIntArray(rawPingAmplitudes));
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendRandomData();
            sendTempAndBattery();
            _dataSendHandler.postDelayed(this, BTService.DATA_REFRESH_RATE_MS_1_1_OR_NEWER);
        }
    };

    private void sendTestFileData() {
        TestSonarDataService testSonarDataService = TestSonarDataService.getInstance(_context);
        testSonarDataService.runTestFile(R.raw.test6ft);
    }

    private void sendRandomData(){

        if (!_seaWeedMode) {
            int seaweedRand = _random.nextInt(5);
            if (seaweedRand == 2) {
                _seaWeedMode = true;
                _seaWeedCount = 0;
            }
        }
        else {
            _seaWeedCount++;
        }

        if (_seaWeedCount == 10) {
            _seaWeedMode = false;
        }

        int numOfFish = _random.nextInt(5);
        if (numOfFish > 1) numOfFish = 0;

        _fishSonarEchoData = new FishSonarData[numOfFish + (_seaWeedMode ? 1 : 0)];

        List<FishSonarData> fishSonarEchoDatas = new ArrayList<>();

        double sonarDataDepthMeters = getDepth();
        double fishMaxDepthMeters = sonarDataDepthMeters - 1;

        for (int i = 0; i < numOfFish; i++) {
            if (fishMaxDepthMeters > 1) {
                fishSonarEchoDatas.add((randomFishDataWithMaxDepth(fishMaxDepthMeters)));
            }
        }

        if (_seaWeedMode & fishSonarEchoDatas.size() > 0) {
            fishSonarEchoDatas.set(fishSonarEchoDatas.size() - 1,
                    new FishSonarData(FishSize.LARGE, 500, sonarDataDepthMeters - 0.9144)); // 3 feet
        }

        _fishSonarEchoData = fishSonarEchoDatas.toArray(new FishSonarData[fishSonarEchoDatas.size()]);
        _depthInMeters = sonarDataDepthMeters;

        EventBus.getDefault().post(new DemoSonarService(_context));

    }

    private int getNextRandomAmplitudeForMax(final int maxAmplitude) {
        int percent = _random.nextInt(100);

        // Decay amplitudes from maxAmplitude, just below the surface.
        if (percent >= 90) {
            int amplitudeDelta = (int)((double) maxAmplitude * 0.15);
            return maxAmplitude - _random.nextInt(amplitudeDelta);
        }
        else if (percent >= 60) {
            int amplitudeDelta = (int)((double) maxAmplitude * 0.2);
            return maxAmplitude - _random.nextInt(amplitudeDelta);
        }
        // Random lower amplitudes further below the surface.
        else {
            return _random.nextInt((int)((double)maxAmplitude * 0.2));
        }
    }

    private void sendTempAndBattery() {
        BTService.getSingleInstance().setTempCelsius(DEMO_TEMP_C);
        BTService.getSingleInstance().setBatteryLevelPercent(DEMO_BATTERY_PERCENT);
        EventBus.getDefault().post(new BTService.DevicePropertiesUpdated());
    }

    private FishSonarData randomFishDataWithMaxDepth(final double maxDepth) {
        int fishSizeRnd = _random.nextInt(10);
        FishSize fishSize = fishSizeRnd >= 8 ? FishSize.XLARGE : FishSize.LARGE;
        double depth = ((_random.nextInt(100) / 100.f) * (maxDepth - 1)) + 1.f;
        int amplitude = MIN_FISH_AMPLITUDE;
        return (new FishSonarData(fishSize, amplitude, depth));
    }

    private double getDepth() {
        if (_depthCount == 0) {
            if (_depthInMeters == 0) {
                _depthInMeters = ((_random.nextInt(100) / 100.0f) * 6.f) + 1f;
            }
            else {
                boolean deeperDepth = _random.nextInt(10) % 2 == 0;
                double depthDelta = ((_random.nextInt(100) / 100.0f) * 1.f);

                if (!deeperDepth && _depthInMeters - depthDelta >= MIN_DEPTH) {
                    _depthInMeters -= depthDelta;
                }
                else {
                    _depthInMeters += depthDelta;
                }
            }

            _depthCount = _random.nextInt(16) + 8;
        }

        _depthCount--;

        return _depthInMeters;
    }
}
