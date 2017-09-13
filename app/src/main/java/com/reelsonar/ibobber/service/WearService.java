package com.reelsonar.ibobber.service;

import android.content.Context;
import android.os.*;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;
import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.dsp.SonarDataService;
import com.reelsonar.ibobber.model.FishSize;
import com.reelsonar.ibobber.model.FishSonarData;
import com.reelsonar.ibobber.model.PingDataProcessor;
import com.reelsonar.ibobber.model.SonarData;
import com.reelsonar.ibobber.triplog.TripLogService;
import com.reelsonar.ibobber.util.MathUtil;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class WearService implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks{

    private GoogleApiClient mGoogleApiClient;

    private Handler _sonarHandler;

    private String TAG = "WearService";

    private static WearService singleInstance = null;

    public static WearService getSingleInstance() {

        if (singleInstance == null) {
            singleInstance = new WearService();
        }
        return singleInstance;
    }

    private WearService() {

        try {
            mGoogleApiClient = new GoogleApiClient.Builder(BobberApp.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                        }
                    })
                    .addApi(Wearable.API)
                    .build();

            mGoogleApiClient.connect();

        } catch (Exception e) {}

        EventBus.getDefault().register(this);

        setupSonarHandler();

    }

    private void setupSonarHandler () {
        _sonarHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                handleNewSonarData((SonarData)msg.obj);

            }
        };

    }


    private void handleNewSonarData(final SonarData sonarData) {
        Context context = BobberApp.getContext();
        if (BTService.getSingleInstance().getConnectedToDevice() || DemoSonarService.getSingleInstance(context).getDemoRunning()) {

            if (sonarData != null) {

                int numFish = 0;
                for (FishSonarData fish : sonarData.getFish()) {
                    if (fish.getDepthMeters() > 0 && fish.getDepthMeters() < sonarData.getDepthMeters()) {
                        numFish++;
                    }
                }
                if (numFish > 0) {
                    WearService.getSingleInstance().sendFishFound(sonarData.getFish().get(0));
                }

                WearService.getSingleInstance().sendDepth(sonarData.getDepthMeters());
            }
        }
    }


    public void onEventMainThread(final BTService.DeviceDidConnect notification) { sendConnectionStatus(); }

    public void onEventMainThread(final BTService.DeviceDidDisconnect notification) {
        sendConnectionStatus();
    }

    public void onEventMainThread(final PingDataProcessor.BobberOutOfWater notification){
        sendOutOfWater();
    }

    private void sendMessageToWear(final String messageString, final String messageData){

        if (!mGoogleApiClient.isConnected()) return;

        new AsyncTask<Void, Void, List<Node>>(){

            @Override
            protected List<Node> doInBackground(Void... params) {
                List<Node> nodes = new ArrayList<Node>();
                NodeApi.GetConnectedNodesResult rawNodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : rawNodes.getNodes()) {
                    nodes.add(node);
                }
                return nodes;
            }

            @Override
            protected void onPostExecute(List<Node> nodeList) {
                for(Node node : nodeList) {
                    Log.d(TAG, "Sending message to " + node.getId());

                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient,
                            node.getId(),
                            "/listener/" + messageString,
                            messageData.getBytes()
                    );

                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.d(TAG, "Result sent to phone: " + sendMessageResult.getStatus().getStatusMessage());
                        }
                    });
                }
            }
        }.execute();

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

        String messagePath =  messageEvent.getPath();

        if (messagePath != null) {
            if (messageEvent.getPath().endsWith("createGPSTag")) {
                TripLogService.getInstance(BobberApp.getContext()).saveTripLogAtCurrentLocation();
            }

            if (messageEvent.getPath().endsWith("requestSonarData")) {
                SonarDataService.getInstance(BobberApp.getContext()).getNextSonarData(_sonarHandler);
            }

        }
    }

    private void sendConnectionStatus() {

        boolean connected = BTService.getSingleInstance() != null && BTService.getSingleInstance().getConnectedToDevice();

        sendMessageToWear("connectionStatus", Boolean.toString(connected));
    }

    private void sendFishFound(FishSonarData fish) {

        String depthInLocalUnits = MathUtil.getFishDepthText(MathUtil.metersToUnitOfMeasure(fish.getDepthMeters(), BobberApp.getContext()));

        if (fish.getSize() == FishSize.XLARGE) {
            sendMessageToWear("bigFishFound",depthInLocalUnits);
        } else {
            sendMessageToWear("smallFishFound",depthInLocalUnits);
        }
    }


    private void sendOutOfWater() {

        if( BTService.getSingleInstance().hasWaterDetection() )  // Disregard BobberOutOfWaterSoft values if Bobber has hardware-based out-of-water detection
            return;

        sendMessageToWear("outOfWater", "");
    }


    private void sendDepth(double depth) {

        String messageString;

        UserService userService = UserService.getInstance(BobberApp.getContext());

        if (userService.isMetric()) {
            messageString = Integer.toString((int)(depth)) + " m";
        } else {
            messageString = Integer.toString((int)(MathUtil.metersToUnitOfMeasure(depth, BobberApp.getContext()))) + " ft";
        }

        sendMessageToWear("depth", messageString);

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "connected to Google Play Services on Wear!");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

}
