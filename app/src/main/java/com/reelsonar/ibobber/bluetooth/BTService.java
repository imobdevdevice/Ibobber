// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.bluetooth;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.PingDataProcessor;
import com.reelsonar.ibobber.model.Temperature;
import com.reelsonar.ibobber.service.DemoSonarService;
import com.reelsonar.ibobber.service.UserService;

import de.greenrobot.event.EventBus;

import java.util.*;

public class BTService extends Service implements BluetoothAdapter.LeScanCallback {

    static private BTService singleInstance;

    public static final String DEVICE_NAME = "iBobber";
    public static final String KEY_USER_DEVICE_HASH = "userDeviceHash";

    // Data refresh rates based on firmware versions.
    public static final int DATA_REFRESH_RATE_MS_1_0_OR_OLDER = 1000;
    public static final int DATA_REFRESH_RATE_MS_1_1_OR_NEWER = 450;

    private static final int DEVICE_LIST_REFRESH_INTERVAL_MS = 500;
    private static final int SCANNED_DEVICE_STALE_MS = 3000;

    private BTFirmwareUpdateProfile mUpdateProfile;

    private Handler mMainThreadHandler = new Handler();

    private Handler mDeviceListUpdateHandler = new Handler();

    private boolean mAutoReconnectEnabled = false;
    private BluetoothDevice mAutoReconnectDevice= null;

    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> characteristicReadQueue = new LinkedList<BluetoothGattCharacteristic>();
    private Queue<BluetoothGattCharacteristic> characteristicWriteQueue = new LinkedList<BluetoothGattCharacteristic>();

    private boolean mDescriptorWritePending = false;
    private boolean mCharacteristicWritePending = false;
    private boolean mCharacteristicReadPending = false;

    private Date datePurchased = null;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    //Eventbus events
    public static class DeviceDidConnect{}
    public static class DeviceStartedConnecting{}
    public static class DeviceDidDisconnect{}
    public static class BTScanStarted{}
    public static class BTScanStopped{}
    public static class DeviceListUpdated{}
    public static class DevicePropertiesUpdated{}
    public static class StrikeAlarmOccurred{}
    public static class HardwareRevUpdated{}
    public static class DeviceAddressUpdated{}
    public static class SlowModeUpdated{}

    public static class FirmwareVersionUpdated{}
    public static class WaterDetectionUpdated{}

    // Internal data
    private final String TAG = "BTService";

    private BluetoothGattCharacteristic mSonarEnableCharacteristic = null;
    private BluetoothGattCharacteristic mAccelEnableCharacteristic = null;
    private BluetoothGattCharacteristic mStrikeAlarmCharacteristic = null;
    private BluetoothGattCharacteristic mAccelThresholdCharacteristic = null;
    private BluetoothGattCharacteristic mWaterDetectCharacteristic = null;
    private BluetoothGattCharacteristic mLightCharacteristic = null;
    private BluetoothGattCharacteristic mBuzzerCharacteristic = null;
    private BluetoothGattCharacteristic mHardwareRevCharacteristic = null;
    private BluetoothGattCharacteristic mDeviceAddressCharacteristic = null;
    private BluetoothGattCharacteristic mSlowModeCharacteristic = null;

    private BluetoothGattCharacteristic mFirmwareVersionCharacteristic = null;

    private BluetoothGattCharacteristic mEchoDataCharacteristic = null;
    private BluetoothGattCharacteristic mTempDataCharacteristic = null;

    private BluetoothGattCharacteristic mBatteryDataCharacteristic  = null;

    private Integer mLightValue = BTConstants.LIGHT_OFF;
    private Integer mBuzzerValue = BTConstants.BUZZER_OFF;
    private Integer mStrikeAlarmEnableStatus = BTConstants.STRIKE_ALARM_DISABLE;
    private Integer mAccelThreshold = BTConstants.STRIKE_ALARM_MAX_VALUE;
    private Integer mSlowMode = BTConstants.SLOW_MODE_DISABLE;

    private String mDeviceAddress;
    private String mFirmwareVersion;
    private int mHardwareRev;

    private boolean mWaterDetectAvailable = false;
    private Timer mWaterDetectTimer;
    private Integer mWaterDetected = 0;

    private Integer mTempCelsius = 0;
    private Integer mBatteryLevelPercent = 0;

    private Timer mBatteryTimer;

    private Integer mRSSI = 0;

    public enum ConnectionStatus{
        DEVICE_CONNECTING,
        DEVICE_CONNECTED,
        DEVICE_DISCONNECTED
    }

    private enum SonarPacketState{
        PACKET_COMPILING,
        PACKET_COMPLETE,
        PACKET_ERROR
    }

    private SonarPacketState currentSonarPacketState = SonarPacketState.PACKET_COMPILING;

    private final int packetLength = 20;
    private final int wordsDataPerPacket = 9;
    private final int packetsPerPing = 47;
    private final int pingTerminator = 0xffff;

    private byte[][] packetArray = new byte[packetsPerPing][packetLength];
    private int packetsCollectedForPing = 0;

    public static BTService getSingleInstance() {

        if (singleInstance == null) {
            singleInstance = new BTService();
        }
        return singleInstance;
    }


   /*-----------------------------------------------------------------
    * Setters
    *-----------------------------------------------------------------*/

    public void setBatteryLevelPercent (Integer value) { mBatteryLevelPercent = value; }
    public void setTempCelsius (int tempValue ) { mTempCelsius = tempValue; }

    public void setLight(int lightValue) {
        mLightValue = lightValue;
        writeCharacteristic(mLightCharacteristic, new byte[]{mLightValue.byteValue()});
    }

    public void setSlowMode( int slowModeValue ) {
        if (mSlowModeCharacteristic != null) {
            mSlowMode = slowModeValue;
            writeCharacteristic(mSlowModeCharacteristic, new byte[]{mSlowMode.byteValue()});
        }
    }

    public void setBuzzer(int buzzerValue) {
        if (mBuzzerCharacteristic != null) {
            mBuzzerValue = buzzerValue;
            writeCharacteristic(mBuzzerCharacteristic, new byte[]{mBuzzerValue.byteValue()});
        }
    }

    public void setStrikeAlarmEnabled(Integer value) {
        mStrikeAlarmEnableStatus = value;
        writeCharacteristic(mAccelEnableCharacteristic, new byte[]{mStrikeAlarmEnableStatus.byteValue()});
        setNotifyForCharacteristic(mConnectedGatt, mStrikeAlarmCharacteristic);
    }

    BluetoothGatt getConnectedGatt() {
        return mConnectedGatt;
    }

    public void setAccelThreshold(Integer value) {
        mAccelThreshold = value;
        Integer valueToSend = BTConstants.STRIKE_ALARM_MAX_VALUE - mAccelThreshold;
        writeCharacteristic(mAccelThresholdCharacteristic, new byte[] {valueToSend.byteValue()});
    }

    private void setRSSI(int value) { mRSSI = value; }

    public void setWaterDetected( Integer value ) { mWaterDetected = value; }

    /*-------------------------------------------------------------------------------------------
     * Getters
     *-----------------------------------------------------------------------------------------*/

    public Integer getBatteryPercent() { return mBatteryLevelPercent; }
    public int getTempCelsius () { return mTempCelsius;}
    public int getLight () { return mLightValue; }
    public int getBuzzer() { return mBuzzerValue; }
    public int getHardwareRev() { return mHardwareRev; }
    public String getFirmwareRev () { return mFirmwareVersion; }
    public Integer getAccelThreshold() { return mAccelThreshold; }
    public Integer getStrikeAlarmEnableStatus() { return mStrikeAlarmEnableStatus; }
    public int getRSSI() { return mRSSI; }
    public String getDeviceAddress() { return mDeviceAddress; }
    // public String getDeviceId() { return mDeviceAddress; }

    public int getWaterDetectionStatus() { return mWaterDetected; }
    public int getSlowModeStatus() { return mSlowMode; }

    public Boolean getIsBluetoothInitialized(){
        return checkIfBluetoothInitialized();
    }


    public boolean getConnectedToDevice() {

        Context context = BobberApp.getContext();
        if (DemoSonarService.getSingleInstance(context).getDemoRunning()) return true;

        //device must be both connected and have all characteristics discovered
        if ( mConnectedGatt == null ) {
            return false;
        }

        for (BluetoothDevice device : mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT)) {
            if (device.hashCode() == mConnectedGatt.getDevice().hashCode()) return (allCharacteristicsDiscovered());
        }

        return false;
    }


    /*-------------------------------------------------------------------------------------------
    * Bluetooth devices, connection etc.
    *-------------------------------------------------------------------------------------------*/

    private BluetoothAdapter mBluetoothAdapter;
    public SparseArray<DiscoveredDevice> mDevices;

    public ArrayList<DiscoveredDevice> mDeviceDiscoveryHistory;

    public class DiscoveredDevice {
        BluetoothDevice btDevice;
        long timeDiscovered;
        ConnectionStatus connectionStatus;

        public DiscoveredDevice(final BluetoothDevice btDevice, final long timeDiscovered, final ConnectionStatus connectionStatus) {
            this.btDevice = btDevice;
            this.timeDiscovered = timeDiscovered;
            this.connectionStatus = connectionStatus;
        }

        public BluetoothDevice getBTDevice() {
            return btDevice;
        }

        public long getTimeDiscovered() {
            return timeDiscovered;
        }

        public ConnectionStatus getConnectionStatus() {
            return connectionStatus;
        }

        public void setConnectionStatus(ConnectionStatus newConnectionStatus) {
            connectionStatus = newConnectionStatus;
        }

    }

    private BluetoothGatt mConnectedGatt = null;
    public BluetoothManager mBluetoothManager;

    //=============================================================================
    // BTService Standard Methods
    //=============================================================================

    public BTService() {
        Log.i(TAG, "BTService Constructor");
    }


    public boolean hasWaterDetection() {

        if( mHardwareRev < 4)  // HW 3 had non-boolean water status, which proved problematic
            return false;

        return mWaterDetectAvailable;
    }

    public int getDataRefreshRateMs() {
        int dataRefreshRateMs;

        if (mFirmwareVersion != null && (mFirmwareVersion.startsWith("0.") || mFirmwareVersion.startsWith(("1.0")))) {
            dataRefreshRateMs = DATA_REFRESH_RATE_MS_1_0_OR_OLDER;
        }
        else {
            dataRefreshRateMs = DATA_REFRESH_RATE_MS_1_1_OR_NEWER;
        }

        Log.i(TAG, "getDataRefreshRateMs() " + dataRefreshRateMs);

        return dataRefreshRateMs;
    }

    @Override
    public void onCreate() {

        BTService.singleInstance = this;

        mUpdateProfile = new BTFirmwareUpdateProfile();

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mDevices = new SparseArray<DiscoveredDevice>();

        mDeviceDiscoveryHistory = new ArrayList<DiscoveredDevice>();

        checkIfBluetoothInitialized();
    }

    public BTFirmwareUpdateProfile getFirmwareUpdateProfile() {
        return mUpdateProfile;
    }

    public Boolean checkIfBluetoothInitialized(){

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Context context = getApplicationContext();
            CharSequence text = getResources().getText(R.string.bluetooth_off_alert);
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return false;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, getResources().getText(R.string.bluetooth_unsupported_alert), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "onDestroy");

        mBluetoothAdapter.stopLeScan(this);

        if (mConnectedGatt != null) {

            mWaterDetectTimer.cancel();
            mWaterDetectTimer = null;

            mBatteryTimer.cancel();
            mBatteryTimer = null;

            mConnectedGatt.disconnect();
            mConnectedGatt.close();
            mConnectedGatt = null;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand");

        //assure we stay around
        //return (START_STICKY);  // Specifying START_STICKY caused problem when user attempts to kill app using swipe or Clear ALL (which autostarted the app in the background)
        return (START_NOT_STICKY);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mMessenger.getBinder();
    }

    public void enableSonar() {
        if (mSonarEnableCharacteristic != null) {
            writeCharacteristic(mSonarEnableCharacteristic, new byte[] {BTConstants.SONAR_ENABLE});
        }
    }

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
        }
    }


    //compile incoming packets into array (accounts for packets possibly being out of order)
    private boolean compileSonarPackets (byte[] sonarData) {

        //all packets should be 20 bytes
        if ( sonarData.length != packetLength ) {
            currentSonarPacketState = SonarPacketState.PACKET_ERROR;
            Log.i(TAG, "Sonar packet unexpected size");
            return false;
        }

        int packetIndex = (short)(sonarData[0] & 0xff) + ((short)(sonarData[1] & 0xff) << 8);

        if (packetIndex == 0) {
            packetArray = new byte[packetsPerPing][packetLength];
            packetsCollectedForPing = 0;
            currentSonarPacketState = SonarPacketState.PACKET_COMPILING;
        }

        if (currentSonarPacketState == SonarPacketState.PACKET_ERROR) {
            Log.i(TAG, "Ignoring additional data on bad packet");
            return false;
        }

        int packetWordOffset = packetIndex / wordsDataPerPacket;

        if (packetIndex == pingTerminator) {
            packetsCollectedForPing ++;
            packetArray[packetsPerPing - 1] = Arrays.copyOfRange(sonarData, 2, sonarData.length);
            if (packetsCollectedForPing < packetsPerPing) {
                currentSonarPacketState = SonarPacketState.PACKET_ERROR;
                Log.i(TAG, "Error: Final packet received with prior packets missing");
                return false;
            }
            sonarDataToValues();
            currentSonarPacketState = SonarPacketState.PACKET_COMPLETE;
            return true;
        }

        if (packetWordOffset >= packetsPerPing) {
            currentSonarPacketState = SonarPacketState.PACKET_ERROR;
            Log.i(TAG, "Error: packet index too large");
            return false;
        }

        packetsCollectedForPing ++;
        packetArray[packetWordOffset] = Arrays.copyOfRange(sonarData, 2, sonarData.length);

        return true;
    }


    //converts array of packets into usable data stream
    private void sonarDataToValues() {

        int sonarValues[] = new int[packetsPerPing * wordsDataPerPacket];

        for (int packetLoop = 0; packetLoop < packetsPerPing; packetLoop++) {
            for (int byteLoop = 0; byteLoop < wordsDataPerPacket * 2; byteLoop = byteLoop + 2) {
                int sonarValue = (short)(packetArray[packetLoop][byteLoop] & 0xff) +
                        ((short)(packetArray[packetLoop][byteLoop + 1] & 0xff) << 8);
                sonarValues[(packetLoop * wordsDataPerPacket) + (byteLoop / 2)] = sonarValue;
            }
        }

        EventBus.getDefault().post(new PingDataProcessor(sonarValues));
    }

    private boolean allCharacteristicsDiscovered() {

        if (mSonarEnableCharacteristic != null &&
            mAccelEnableCharacteristic != null &&
            mStrikeAlarmCharacteristic != null &&
            mAccelThresholdCharacteristic != null  &&
            mLightCharacteristic != null &&
            mFirmwareVersionCharacteristic != null &&
            mWaterDetectCharacteristic != null &&
            mHardwareRevCharacteristic != null &&
            mEchoDataCharacteristic != null &&
            mTempDataCharacteristic != null &&
            mBatteryDataCharacteristic != null) return true;

        return false;

    }

    private void resetCharacteristicsAndPurgeQueues() {
        mHardwareRev = BTConstants.HARDWARE_REV_1;

        mSonarEnableCharacteristic = null;
        mAccelEnableCharacteristic = null;
        mStrikeAlarmCharacteristic = null;
        mAccelThresholdCharacteristic = null;
        mLightCharacteristic = null;
        mBuzzerCharacteristic = null;
        mWaterDetectCharacteristic = null;
        mFirmwareVersionCharacteristic = null;
        mHardwareRevCharacteristic = null;
        mEchoDataCharacteristic  = null;
        mTempDataCharacteristic  = null;
        mBatteryDataCharacteristic  = null;

        purgeQueues();
    }

    private void purgeQueues() {

        mMainThreadHandler.post(new Runnable() {
            public void run() {

                if (Looper.myLooper() != Looper.getMainLooper()) {
                    Log.d(TAG, "purgeQueues not on main loop (this should not happen)");
                }

                descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
                characteristicReadQueue = new LinkedList<BluetoothGattCharacteristic>();
                characteristicWriteQueue = new LinkedList<BluetoothGattCharacteristic>();

                mCharacteristicReadPending = false;
                mCharacteristicWritePending = false;
                mDescriptorWritePending = false;
            }
        });

    }

    public void connectToBobber(DiscoveredDevice deviceToConnect) {

        Log.i(TAG, "connectToBobber: " + deviceToConnect);

        //disconnect any previously connected devices
        disconnectBobber();

        //scan must be stopped before initiating connection (bad things happen if not)
        stopScan();

        if ( mDevices == null ) {
            return;
        }

        resetAllDevicesToDisconnected();

        if ( deviceToConnect != null ) {
            mConnectedGatt = deviceToConnect.getBTDevice().connectGatt(this, false, mGattCallback);
            updateListWithConnectionStatusForDevice(deviceToConnect.getBTDevice(), ConnectionStatus.DEVICE_CONNECTING);
            EventBus.getDefault().post(new DeviceStartedConnecting());
            Log.i(TAG, "Connecting to " + deviceToConnect.getBTDevice().getName() + "with gatt" + mConnectedGatt);
        }
    }

    private void resetAllDevicesToDisconnected() {

        if( mDevices == null )
            return;
        //reset all devices to disconnected (clears any "connecting" devices)
        for (int i = 0; i < mDevices.size(); i++) {
            DiscoveredDevice deviceFromList =  mDevices.valueAt(i);
            deviceFromList.setConnectionStatus(ConnectionStatus.DEVICE_DISCONNECTED);
        }

        EventBus.getDefault().post(new DeviceDidDisconnect());

    }

    private void updateListWithConnectionStatusForDevice(BluetoothDevice btDevice, ConnectionStatus connectionStatus) {

        int index = mDevices.indexOfKey(btDevice.hashCode());

        if ( !(index < 0) ) {
            mDevices.get(btDevice.hashCode()).setConnectionStatus(connectionStatus);
        }

    }

    public void disconnectBobber() {

        resetCharacteristicsAndPurgeQueues();
        mAutoReconnectEnabled = false;
        mFirmwareVersion = null;

        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
        }

        if( mWaterDetectTimer != null ) {
            mWaterDetectTimer.cancel();
            mWaterDetectTimer = null;
        }

        if( mBatteryTimer != null ) {
            mBatteryTimer.cancel();
            mBatteryTimer = null;
        }

        resetAllDevicesToDisconnected();
    }

    //enqueue descriptor write
    private void writeGattDescriptor(BluetoothGattDescriptor descriptor) {

        if (descriptor != null ) {
            Log.i(TAG, "Queue descriptor write: " + descriptor);

            if (mBluetoothAdapter == null || mConnectedGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }

            descriptorWriteQueue.add(descriptor);
            processBTQueuesOnMainThread();
        }
    }

    public void requestInWaterStatus() {

        //water status request might be on timer - need to assure runs on main thread to avoid creating thread safety issues
        mMainThreadHandler.post(new Runnable() {
            public void run() {
                readCharacteristic(mWaterDetectCharacteristic);
            }
        });
    }

    //enqueue characteristic read
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (characteristic != null ) {

            // Log.d(TAG, "** ** ** Queue read for Characteristic: [ " + characteristic.getUuid() + " ] ** ** ** **");

            if (mBluetoothAdapter == null || mConnectedGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }

            characteristicReadQueue.add(characteristic);
            processBTQueuesOnMainThread();
        }
    }

    //enqueue characteristic write
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {

        if (characteristic != null ) {
            characteristic.setValue(value);
            Log.d(TAG, "Queue write for Characteristic: " + characteristic);

            if (mBluetoothAdapter == null || mConnectedGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }

            characteristicWriteQueue.add(characteristic);
            processBTQueuesOnMainThread();
        }

    }

    public void dequeueOnMainThread(Object queue) {

        final Queue finalQueue = (Queue<Object>)queue;
        mMainThreadHandler.post(new Runnable() {
            public void run() {

                if (finalQueue == characteristicWriteQueue) {
                    mCharacteristicWritePending = false;
                    Log.d(TAG, "dequeue / characteristicWriteQueue size:" + characteristicWriteQueue.size());
                }
                if (finalQueue == characteristicReadQueue) {
                    mCharacteristicReadPending = false;
                    Log.d(TAG, "dequeue / characteristicReadQueue size:" + characteristicReadQueue.size());
                }
                if (finalQueue == descriptorWriteQueue) {
                    mDescriptorWritePending = false;
                    Log.d(TAG, "dequeue / descriptorWriteQueue size:" + descriptorWriteQueue.size());
                }
                if(finalQueue.size() > 0 )
                    finalQueue.remove();

                processBTQueuesOnMainThread();

            }
        });
    }

    //assures processBTQueues only happens on main thread
    private void processBTQueuesOnMainThread() {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            processBTQueues();
        } else {
            mMainThreadHandler.post(new Runnable() {
                public void run() {
                    processBTQueues();
                }
            });
        }
    }

    //process BLE queue
    //queueing assures we don't perform additional requests before prior finished (which causes problems)

    public void processBTQueues() {

        Log.d(TAG, "characteristicWriteQueue size:" + characteristicWriteQueue.size());
        Log.d(TAG, "characteristicReadQueue size:" + characteristicReadQueue.size());
        Log.d(TAG, "descriptorWriteQueue size:" + descriptorWriteQueue.size());

        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.d(TAG, "processBTQueues not on main loop (this should not happen)");
        }

        if (mDescriptorWritePending || mCharacteristicWritePending || mCharacteristicReadPending) {
            Log.d(TAG, "Not processing Queue - waiting for prior request to complete");
            return;
        }

        //handle all descriptor writes first
        if (descriptorWriteQueue.size() > 0) {

            try {
                Log.d(TAG, "Writing descriptor: " + descriptorWriteQueue.element());
                boolean result = mConnectedGatt.writeDescriptor(descriptorWriteQueue.element());
                Log.d(TAG, "Result: " + result);
                mDescriptorWritePending = result;
                if (result == false) {
                    descriptorWriteQueue.remove();
                    processBTQueues();
                }
            } catch( Exception exc) {
                Log.e(TAG, "processBTQueues() got exception processing descriptorWriteQueue: " + exc.toString());

            }
            return;
        }

        //then all characteristic writes
        if (characteristicWriteQueue.size() > 0) {

            try {
                BluetoothGattCharacteristic characteristicToWrite = characteristicWriteQueue.element();
                Log.d(TAG, "Writing characteristic: " + characteristicToWrite);
                boolean result = mConnectedGatt.writeCharacteristic(characteristicToWrite);
                Log.d(TAG, "Result:" + result);
                mCharacteristicWritePending = result;

                if (result == false) {
                    Log.e(TAG, "Error writing characteristic!");

                    if (BTConstants.IMAGE_BLOCK_CHAR_UUID.equals(characteristicToWrite.getUuid())) {
                        mUpdateProfile.imageWriteErrorOccurred();
                    }

                    characteristicWriteQueue.remove();
                    processBTQueues();
                }
            } catch( Exception exc) {
                Log.e(TAG, "processBTQueues() got exception processing characteristicWriteQueue: " + exc.toString());

            }
            return;
        }

        //then characteristic reads last
        else if (characteristicReadQueue.size() > 0) {
            // Log.d(TAG, "Made read request for:" + characteristicReadQueue.element());
            try {
                boolean result = mConnectedGatt.readCharacteristic(characteristicReadQueue.element());
                Log.d(TAG, "Result:" + result);
                mCharacteristicReadPending = result;
                if (result == false) {
                    characteristicReadQueue.remove();
                    processBTQueues();
                }
            }
            catch ( Exception exc ) {
                Log.e(TAG, "processBTQueues() got exception processing characteristicReadQueue: " + exc.toString());
            }
            return;
        }
    }

    void setNotifyForCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        //Enable local notifications
        if (characteristic != null ) {
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(BTConstants.CONFIG_DESCRIPTOR);
            if( desc != null ) {
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            else {
                Log.e(TAG, "setNotifyForCharacteristic() got null BluetoothGattDescriptor");
            }

            writeGattDescriptor(desc);
        }
    }

    void disableNotifyForCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        //Disable local notifications
        if (characteristic != null ) {
            gatt.setCharacteristicNotification(characteristic, false);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(BTConstants.CONFIG_DESCRIPTOR);
             if( desc != null && BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE != null ) {
                 desc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
             } else {
               Log.e(TAG, "disableNotifyForCharacteristic() got null BluetoothGattDescriptor");
             }
            writeGattDescriptor(desc);
        }
    }

    public void disableNotifyCharacteristics() {

        disableNotifyForCharacteristic(mConnectedGatt, mEchoDataCharacteristic);
        disableNotifyForCharacteristic(mConnectedGatt, mTempDataCharacteristic);
        disableNotifyForCharacteristic(mConnectedGatt, mBatteryDataCharacteristic);
        disableNotifyForCharacteristic(mConnectedGatt, mWaterDetectCharacteristic);

        if( mWaterDetectTimer != null )
            mWaterDetectTimer.cancel();
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        private void initializeServices(BluetoothGatt gatt) {

            //sonar
            setNotifyForCharacteristic(gatt, mEchoDataCharacteristic);

            //temp
            setNotifyForCharacteristic(gatt, mTempDataCharacteristic);

            //light
            setLight(BTConstants.LIGHT_OFF);
            setLight(BTConstants.LIGHT_ON);
            setLight(BTConstants.LIGHT_OFF);

            setBuzzer(BTConstants.BUZZER_OFF);

            // Slow Mode
            readCharacteristic(mSlowModeCharacteristic);
            // setSlowMode( BTConstants.SLOW_MODE_DISABLE );

            // battery
            // setNotifyForCharacteristic(gatt, mBatteryDataCharacteristic);
            // readCharacteristic(mBatteryDataCharacteristic);
            pollForBattery();

            //request firmware version
            readCharacteristic(mFirmwareVersionCharacteristic);

            // Hardware Rev
            readCharacteristic(mHardwareRevCharacteristic);

            // MAC Address (exposed in devices having firmware 1.6 and above)
            readCharacteristic(mDeviceAddressCharacteristic);

            // Water ADC
            // setNotifyForCharacteristic(gatt, mWaterDetectCharacteristic);
            // readCharacteristic(mWaterDetectCharacteristic);

            //accelerometer / strike alarm
            setAccelThreshold(mAccelThreshold);
            setStrikeAlarmEnabled(mStrikeAlarmEnableStatus);

            //stop sonar demo service if running
            DemoSonarService.getSingleInstance(getApplicationContext()).stopSendingData();

            //connection event only fired after fully discovering / initializing device
            updateListWithConnectionStatusForDevice(gatt.getDevice(), ConnectionStatus.DEVICE_CONNECTED);
            mAutoReconnectEnabled = true; //autoreconnect is enabled on full initialization
            EventBus.getDefault().post(new DeviceDidConnect());

            BobberApp.setBobberHasSynched( true );
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.i(TAG, "Connection State Change: "+status+" -> "+connectionState(newState) + "gatt instance:" + gatt);

            //if connection state changed during FW update - trigger error
            BTService.getSingleInstance().getFirmwareUpdateProfile().imageWriteErrorOccurred();

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {

                //write out hash for connected device
                SharedPreferences userPreferences = PreferenceManager.getDefaultSharedPreferences(BTService.singleInstance);
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putInt (BTService.KEY_USER_DEVICE_HASH, gatt.getDevice().hashCode());
                editor.apply();

                mAutoReconnectDevice = gatt.getDevice();

                gatt.discoverServices();
                return;
            }

            //not verifying success in case of disconnect
            //Android 5 returns "GATT_INSUF_AUTHORIZATION" on client-initiated connection drop (Android bug?)
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                updateListWithConnectionStatusForDevice(gatt.getDevice(), ConnectionStatus.DEVICE_DISCONNECTED);

                //auto-reconnect if we lost connection
                if (mAutoReconnectDevice == gatt.getDevice() && mAutoReconnectEnabled) {

                    Log.i(TAG, "Autoreconnecting to device");
                    resetCharacteristicsAndPurgeQueues();
                    gatt.close();
                    mConnectedGatt = gatt.getDevice().connectGatt(BTService.getSingleInstance(), false, this);

                } else {
                    resetCharacteristicsAndPurgeQueues();
                    gatt.close();  //disconnects gatt callbacks - important for stability
                    gatt = null;
                }

                datePurchased = null;

                EventBus.getDefault().post(new DeviceDidDisconnect());
                return;
            }

            //if any other condition which isn't listed as success - drop connection / reset everything
            if (status != BluetoothGatt.GATT_SUCCESS) {
                //if connection state change indicates error - disconnect / close gatt
                Log.i(TAG, "Non-success connections status change - dropping connection / resetting");
                mAutoReconnectEnabled = false;
                resetCharacteristicsAndPurgeQueues();
                gatt.disconnect();
                updateListWithConnectionStatusForDevice(gatt.getDevice(), ConnectionStatus.DEVICE_DISCONNECTED);
                gatt.close();
                gatt = null;
                EventBus.getDefault().post(new DeviceDidDisconnect());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Log.d(TAG, "Services Discovered: "+ status);

            if ( status == BluetoothGatt.GATT_SUCCESS) {

                List<BluetoothGattService> deviceServices = gatt.getServices();

                if ( deviceServices != null ) {
                    Log.d(TAG, "onServiceDiscovered: " + deviceServices.size() + " services");
                    for ( int i = 0; i < deviceServices.size(); i++ ) {
                        BluetoothGattService deviceService = deviceServices.get(i);

                        if ( deviceService != null ) {
                            Log.d(TAG, "onServiceDiscovered service uuid: " + deviceService.getUuid());

                            List<BluetoothGattCharacteristic> serviceCharacteristics = deviceService.getCharacteristics();
                            Log.d(TAG, "onServiceDiscovered: " + serviceCharacteristics.size() + " characteristics");

                            //assign characteristics to variables
                            for ( int j = 0; j < serviceCharacteristics.size(); j++ ) {
                                BluetoothGattCharacteristic characteristic  = serviceCharacteristics.get(j);
                                UUID uuid = characteristic.getUuid();

                                Log.d(TAG, "onServiceDiscovered characteristic: " + uuid);

                                // characteristics for custom service
                                if (BTConstants.CUSTOM_SERVICE_UUID.equals(deviceService.getUuid())) {

                                    if (BTConstants.TEMP_DATA_CHAR_UUID.equals(characteristic.getUuid()))
                                        mTempDataCharacteristic = characteristic;

                                    if (BTConstants.ECHO_DATA_UUID.equals(characteristic.getUuid()))
                                        mEchoDataCharacteristic = characteristic;

                                    if (BTConstants.SONAR_ENABLE_CHAR_UUID.equals(characteristic.getUuid()))
                                        mSonarEnableCharacteristic = characteristic;

                                    if (BTConstants.ECHO_DATA_UUID.equals(characteristic.getUuid()))
                                        mEchoDataCharacteristic = characteristic;

                                    if (BTConstants.LIGHT_DATA_CHAR_UUID.equals(characteristic.getUuid()))
                                        mLightCharacteristic = characteristic;

                                    if (BTConstants.BUZZER_DATA_CHAR_UUID.equals(characteristic.getUuid())) {
                                        mBuzzerCharacteristic = characteristic;
                                    }

                                    if (BTConstants.DEVICE_ADDRESS_DATA_UUID.equals(characteristic.getUuid())) {
                                        mDeviceAddressCharacteristic = characteristic;
                                    }

                                    if (BTConstants.WATER_DETECT_CHAR_UUID.equals(characteristic.getUuid())) {
                                        mWaterDetectCharacteristic = characteristic;
                                        mWaterDetectAvailable = true;
                                        pollForWaterDetection();
                                    }
                                    if (BTConstants.SLOW_MODE_DATA_UUID.equals(characteristic.getUuid()))
                                        mSlowModeCharacteristic = characteristic;
                                }

                                //characteristics for accel service
                                if (BTConstants.ACCEL_SERVICE_UUID.equals(deviceService.getUuid())) {

                                    if (BTConstants.ACCEL_ENABLE_UUID.equals(characteristic.getUuid()))
                                        mAccelEnableCharacteristic = characteristic;

                                    if (BTConstants.ACCEL_THRESHOLD_UUID.equals(characteristic.getUuid()))
                                        mAccelThresholdCharacteristic = characteristic;

                                    if (BTConstants.STRIKE_ALARM_UUID.equals(characteristic.getUuid()))
                                        mStrikeAlarmCharacteristic = characteristic;


                                }

                                //battery service / characteristic
                                //OAD service / characteristic
                                if (BTConstants.BATT_SERVICE_UUID.equals(deviceService.getUuid())) {
                                    if (BTConstants.BATT_DATA_CHAR_UUID.equals(characteristic.getUuid()))
                                        mBatteryDataCharacteristic = characteristic;
                                }

                                //OAD service / characteristic
                                if (BTConstants.OAD_SERVICE_UUID.equals(deviceService.getUuid())) {
                                    if (BTConstants.IMAGE_NOTIFY_CHAR_UUID.equals(characteristic.getUuid()))
                                        mUpdateProfile.setImageNotifyCharacteristic(characteristic);

                                    if (BTConstants.IMAGE_BLOCK_CHAR_UUID.equals(characteristic.getUuid()))
                                        mUpdateProfile.setImageBlockCharacteristic(characteristic);
                                }

                                //device version / firmware version characteristic
                                if (BTConstants.DEVICE_SERVICE_UUID.equals(deviceService.getUuid())) {
                                    if (BTConstants.FIRMWARE_VERSION_CHAR_UUID.equals(characteristic.getUuid()))
                                        mFirmwareVersionCharacteristic = characteristic;

                                    if (BTConstants.HARDWARE_REV_DATA_UUID.equals(characteristic.getUuid())) {
                                        mHardwareRevCharacteristic = characteristic;
                                    }

                                }
                            }
                        }
                    }
                }

                //if all services discovered - initialize - otherwise disconnect
                if (allCharacteristicsDiscovered())
                {
                    initializeServices(gatt);

                    if( mDeviceAddress == null ) { // This will not have been set for firmware versions previous to 1.6
                        String macAddress = mAutoReconnectDevice.getAddress();
                        if( macAddress != null )
                            mDeviceAddress = macAddress;
                    }
                    requestBobberInfoUpload();
                } else {
                    gatt.disconnect();
                }

                gatt.readRemoteRssi();
            } else {
                Log.d(TAG, "onServiceDiscovered received: " + status);
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

//            Log.d(TAG, "pre-dequeue / characteristicWriteQueue size:" + characteristicWriteQueue.size());

            dequeueOnMainThread(characteristicWriteQueue);
            //iOS re-reads characteristic value after write - doesn't seem needed / seems to cause write reliability issues
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            dequeueOnMainThread(characteristicReadQueue);

            //handling logic same as if characteristic updated
            onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            if (BTConstants.STRIKE_ALARM_UUID.equals(characteristic.getUuid())) {
                Log.i(TAG, "Update for Strike Alarm value:" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                EventBus.getDefault().post(new BTService.StrikeAlarmOccurred());
            }

            if (BTConstants.BATT_DATA_CHAR_UUID.equals(characteristic.getUuid())) {

                byte[] value = characteristic.getValue();
                if( value == null || value.length == 0 ) {

                    Log.e(TAG, "* * * * * Error getting BATT_DATA_CHAR_UUID * * * * *");
                    return;
                }

                int newBatteryPercent = batteryDataToPercent(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                if (newBatteryPercent != mBatteryLevelPercent) {
                    setBatteryLevelPercent(newBatteryPercent);
                    Log.i(TAG, "Battery percent changed:" + getBatteryPercent());
                    EventBus.getDefault().post(new BTService.DevicePropertiesUpdated());
                }
            }

            if (BTConstants.WATER_DETECT_CHAR_UUID.equals(characteristic.getUuid())) {

                byte[] value = characteristic.getValue();

                if( value == null || value.length == 0 ) {

                    Log.e(TAG, "* * * * * Error getting WATER_DETECT_CHAR_UUID * * * * *");
                    return;
                }

                int waterDetected = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

                if( waterDetected != mWaterDetected ) {
                    Log.e(TAG, "New water detection:" + waterDetected);
                    mWaterDetected = waterDetected;
                    EventBus.getDefault().post(new BTService.WaterDetectionUpdated());
                }
            }

            if (BTConstants.IMAGE_NOTIFY_CHAR_UUID.equals(characteristic.getUuid())) {
                mUpdateProfile.gotImageNotifyUpdate(characteristic);
            }

            if (BTConstants.IMAGE_BLOCK_CHAR_UUID.equals(characteristic.getUuid())) {
                mUpdateProfile.gotImageBlockUpdate(characteristic);
            }

            if (BTConstants.FIRMWARE_VERSION_CHAR_UUID.equals(characteristic.getUuid())) {
                byte[] firmwareVersionData = characteristic.getValue();

                if( firmwareVersionData == null || firmwareVersionData.length == 0 ) {
                    Log.e(TAG, "* * * * * Error getting FIRMWARE_VERSION_CHAR_UUID * * * * *");
                    return;
                }

                String firmwareVersion = new String(firmwareVersionData, 0, firmwareVersionData.length - 1);
                String[] components = firmwareVersion.split("\\.");

                if (components.length >= 2) {
                    String major = components[0].replaceAll("[^\\d]", "");
                    String minor = components[1].replaceAll("[^\\d]", "");

                    mFirmwareVersion = Integer.parseInt(major) + "." + Integer.parseInt(minor);
                    EventBus.getDefault().post(new BTFirmwareUpdateProfile.FirmWareInfoChanged());
                    mUpdateProfile.requestLatestFirmwareVersion();
                    Log.i(TAG, "Firmware version: " + mFirmwareVersion);
                    EventBus.getDefault().post(new BTService.DevicePropertiesUpdated());
                    EventBus.getDefault().post(new BTService.FirmwareVersionUpdated());
                }
            }

            if (BTConstants.TEMP_DATA_CHAR_UUID.equals(characteristic.getUuid())) {

                byte[] value = characteristic.getValue();

                if( value == null || value.length == 0 ) {
                    Log.e(TAG, "* * * * * Error getting TEMP_DATA_CHAR_UUID * * * * *");
                    return;
                }

                int newTempCelsius = Temperature.findCelsius(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
                if (newTempCelsius != getTempCelsius()) {
                    setTempCelsius(newTempCelsius);
                    Log.i(TAG, "Temp C changed:" + getTempCelsius());
                    EventBus.getDefault().post(new BTService.DevicePropertiesUpdated());
                }
            }

            if (BTConstants.ECHO_DATA_UUID.equals(characteristic.getUuid())) {
                //Log.d(TAG, "Got Sonar Data");

                byte[] value = characteristic.getValue();

                if( value == null || value.length == 0 ) {
                    Log.e(TAG, "* * * * * Error getting ECHO_DATA_UUID * * * * *");
                    return;
                }

                byte[] sonarData = characteristic.getValue();
                compileSonarPackets(sonarData);
            }

            if (BTConstants.SLOW_MODE_DATA_UUID.equals(characteristic.getUuid())) {
               byte[] value = characteristic.getValue();
                if( value != null ) {
                    int slowModeStatus = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.d(TAG, "Got Slow Mode Status: " + slowModeStatus);
                } else {
                    Log.d(TAG, "Got NULL Slow Mode Status");
                }
            }

            if (BTConstants.HARDWARE_REV_DATA_UUID.equals(characteristic.getUuid())) {

                byte[] value = characteristic.getValue();
                if( value == null || value.length == 0 ) {

                    Log.e(TAG, "* * * * * Error getting HARDWARE_REV_DATA_UUID * * * * *");
                    return;
                }

                int hwRevValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

                mHardwareRev = (int) hwRevValue - 48;  // Value is a character.  Example: '5' (53 base 10)

                EventBus.getDefault().post(new BTService.HardwareRevUpdated());
            }
            if (BTConstants.DEVICE_ADDRESS_DATA_UUID.equals(characteristic.getUuid())) {

                byte[] addressData = characteristic.getValue();

                if( addressData != null && addressData.length >= 12 && addressData[0] != 0 ) {

                    StringBuilder addressStringFormatted = new StringBuilder();
                    String addressString = new String(addressData, 0, addressData.length - 1);

                    int position = 0;
                    while( position < addressString.length() ) {  // Reverse chars and format as MAC address
                        addressStringFormatted.insert(0, ":" + addressString.substring(position, position + 2));
                        position += 2;
                    }
                    addressStringFormatted.deleteCharAt(0);  // Delete leading colon
                    mDeviceAddress = addressStringFormatted.toString();
                    Log.d(TAG, "iBobber Address: " + addressStringFormatted.toString());
                } else {

                    Log.d(TAG, "HW address not available from characteristic.  Using address from bluetooth device.");
                   mDeviceAddress = gatt.getDevice().getAddress();
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            if (BTConstants.ECHO_DATA_UUID.equals(descriptor.getCharacteristic().getUuid())) {
                Log.d(TAG, "Descriptor for Sonar Data characteristic changed");
            }

            if (BTConstants.TEMP_DATA_CHAR_UUID.equals(descriptor.getCharacteristic().getUuid())) {
                Log.d(TAG, "Descriptor for Temp Data characteristic changed");
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Callback: Wrote GATT Descriptor successfully.");
            }
            else{
                Log.d(TAG, "Callback: Error writing GATT Descriptor: " + status);
            }

            dequeueOnMainThread(descriptorWriteQueue);
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: "+rssi);
            setRSSI(rssi);
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

    private void clearDeviceList() {
        mDevices.clear();
        mDeviceDiscoveryHistory.clear();
        EventBus.getDefault().post(new DeviceListUpdated());
    }

    public void resetListToConnectedDeviceOnly() {
        final List<BluetoothDevice> connectedDevices = mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT);

        clearDeviceList();

        if (allCharacteristicsDiscovered()) {
            for (BluetoothDevice device : connectedDevices) {
                //only add iBobbers
                if (DEVICE_NAME.equals(device.getName())) {
                    Log.i(TAG, "connected device: " + device.hashCode());
                    DiscoveredDevice theDevice = new DiscoveredDevice(device, 0, ConnectionStatus.DEVICE_CONNECTED);
                    mDevices.put(device.hashCode(), theDevice);
                }
            }
        }

        EventBus.getDefault().post(new DeviceListUpdated());
    }

    public void startScan() {

        //disconnect bobber if connected before scan
        disconnectBobber();

        clearDeviceList();

        mBluetoothAdapter.startLeScan(this);

        mDeviceListUpdateHandler.postDelayed(deviceRefreshRunnable, DEVICE_LIST_REFRESH_INTERVAL_MS);

        EventBus.getDefault().post(new BTService.BTScanStarted());

    }

    public void startReconnectScan() {

        mBluetoothAdapter.startLeScan(this);

    }



    public void stopScan() {

        mBluetoothAdapter.stopLeScan(this);

        mDeviceListUpdateHandler.removeCallbacksAndMessages(null);

        EventBus.getDefault().post(new BTService.BTScanStopped());

    }

    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        mDeviceListUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                //find only ibobbers
                if (DEVICE_NAME.equals(device.getName())) {

                    Log.i(TAG, "onLeScan found new iBobber device:" + device.hashCode());

                    DiscoveredDevice discoveredDevice = new DiscoveredDevice(device, System.currentTimeMillis(), ConnectionStatus.DEVICE_DISCONNECTED);

                    mDeviceDiscoveryHistory.add(discoveredDevice);

                }
            }
        });
    }

    //runnable for calling device list refresh
    private Runnable deviceRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshDeviceList();
            mDeviceListUpdateHandler.postDelayed(this, DEVICE_LIST_REFRESH_INTERVAL_MS);
        }
    };



    private void refreshDeviceList() {

        mDevices.clear();

        //list may be updated async - so using copy
        ArrayList<DiscoveredDevice> discoveryHistoryCopy = new ArrayList<DiscoveredDevice>(mDeviceDiscoveryHistory);

        //purge any old devices from history
        for (Iterator<DiscoveredDevice> discoveredDevices = discoveryHistoryCopy.iterator(); discoveredDevices.hasNext(); ) {
            DiscoveredDevice discoveredDevice = discoveredDevices.next();
            if (System.currentTimeMillis() - discoveredDevice.getTimeDiscovered() > SCANNED_DEVICE_STALE_MS) {
                //some devices don't continually report discovered devices - removing for now
                //discoveredDevices.remove();
            }
        }

        mDeviceDiscoveryHistory = discoveryHistoryCopy;

        //repopulate device list
        for (int i = 0; i < mDeviceDiscoveryHistory.size(); i++) {
            int index = mDevices.indexOfKey(mDeviceDiscoveryHistory.get(i).getBTDevice().hashCode());
            if ( index < 0 ) {
                DiscoveredDevice newDevice = new DiscoveredDevice(mDeviceDiscoveryHistory.get(i).getBTDevice(), 0, ConnectionStatus.DEVICE_DISCONNECTED);
                mDevices.put(newDevice.getBTDevice().hashCode(), newDevice);
            }
        }

        EventBus.getDefault().post(new DeviceListUpdated());
    }


    private void requestBobberInfoUpload() {

        int delay = 4000; // delay for 4 sec, enough to permit the collection all information from iBobber
        Timer timer = new Timer();
        timer.schedule( new TimerTask()
            {
                public void run()
                {
                    UserService.getInstance( BobberApp.getContext() ).persistBobberInfo();
                }
            }, delay);
    }

    private void pollForWaterDetection() {

        int delay = 8000;
        int interval = 3000;

        mWaterDetectTimer = null;
        mWaterDetectTimer = new Timer();

        mWaterDetectTimer.schedule ( new TimerTask()
            {
                public void run()
                {
                    if( BTService.this.getHardwareRev() >= 4 )
                        requestInWaterStatus();
                }
            }, delay, interval);
    }

    private void pollForBattery() {

        int delay = 5000;
        int interval = 30000;

        mBatteryTimer = null;
        mBatteryTimer = new Timer();

        mBatteryTimer.schedule ( new TimerTask()
            {
                public void run()
                {
                    readCharacteristic(mBatteryDataCharacteristic);

                }
            }, delay, interval);
    }

    private int batteryDataToPercent(int rawBatteryValue) {

        final int batteryTableSize = 5;
        int[] firmwarePercentArray = {30, 40, 40, 60, 90};
        float[] batteryPercentArray = {0.0f, 60.0f, 77.78f, 88.89f, 100.0f};

        if (rawBatteryValue >= 90) {
            return 100;
        }

        int i;

        for( i = 0; i < batteryTableSize - 1; i++ ) {
            if (firmwarePercentArray[i] <= rawBatteryValue
                    && firmwarePercentArray[i+1] >= rawBatteryValue) {
                double diffx = rawBatteryValue - firmwarePercentArray[i];
                double diffn = firmwarePercentArray[i+1] - firmwarePercentArray[i];

                return (int)(batteryPercentArray[i]
                        + (batteryPercentArray[i+1] - batteryPercentArray[i] ) * diffx / diffn);
            }
        }

        return 0;

    }

    public Date getDatePurchased() {
        return datePurchased;
    }

    public void setDatePurchased(Date datePurchased) {
        this.datePurchased = datePurchased;
    }

}
