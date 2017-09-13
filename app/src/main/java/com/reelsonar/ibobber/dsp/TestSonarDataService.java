// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.dsp;

import android.content.Context;
import android.util.Log;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.model.PingDataProcessor;
import com.reelsonar.ibobber.util.GrowableIntArray;
import de.greenrobot.event.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestSonarDataService {

    private static final String TAG = TestSonarDataService.class.getSimpleName();

    private static TestSonarDataService INSTANCE;
    public static synchronized TestSonarDataService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TestSonarDataService(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private Context _context;
    private ScheduledExecutorService _executorService;
    private AtomicReference<Future<?>> _runningTestFile;

    private TestSonarDataService(final Context context) {
        _context = context;
        _executorService = Executors.newSingleThreadScheduledExecutor();
        _runningTestFile = new AtomicReference<>();
    }

    public void runTestFile(final int identifier)
    {
        Future<?> runningTestFile = _runningTestFile.get();
        if (runningTestFile != null) {
            runningTestFile.cancel(true);
        }

        runningTestFile = _executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<GrowableIntArray> allPings = new ArrayList<>();

                InputStream is = _context.getResources().openRawResource(identifier);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                try {
                    GrowableIntArray pings = null;
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty()) {
                            continue;
                        }

                        if (line.startsWith("Ping") || pings == null) {
                            pings = new GrowableIntArray();
                            allPings.add(pings);
                        } else {
                            pings.add(Integer.parseInt(line));
                        }
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Error reading test file", ex);
                } finally {
                    try { reader.close(); } catch (IOException ignored) { }
                }

                if (allPings.size() > 0) {
                    int delay = BTService.DATA_REFRESH_RATE_MS_1_1_OR_NEWER;
                    Future<?> runningTestFile = _executorService.scheduleAtFixedRate(new SendTestDataRunnable(allPings),
                            0, delay, TimeUnit.MILLISECONDS);
                    _runningTestFile.set(runningTestFile);
                }
            }
        });
        _runningTestFile.set(runningTestFile);
    }

    private static class SendTestDataRunnable implements Runnable {

        private List<GrowableIntArray> _pings;
        private Iterator<GrowableIntArray> _iterator;

        private SendTestDataRunnable(List<GrowableIntArray> pings) {
            _pings = pings;
        }

        @Override
        public void run() {
            if (_iterator == null || !_iterator.hasNext()) {
                _iterator = _pings.iterator();
            }

            GrowableIntArray pings = _iterator.next();
            EventBus.getDefault().post(new PingDataProcessor(pings));
        }
    }

}
