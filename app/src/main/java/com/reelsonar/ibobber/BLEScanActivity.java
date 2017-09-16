// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.app.Activity;
import android.app.ListFragment;
import android.bluetooth.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.drawer.DeviceDrawerFragment;
import com.reelsonar.ibobber.drawer.HomeDrawerFragment;
import com.reelsonar.ibobber.service.UserService;
import de.greenrobot.event.EventBus;
import java.util.Date;


public class BLEScanActivity extends Activity {

    private DeviceListAdapter mDeviceListAdapter;

    private final boolean mShowBobberAddress = false;

    private static final String TAG = "BLEScanActivity";

    private LayoutInflater mInflator;

    private SparseArray<BTService.DiscoveredDevice> mBTDeviceListCopy;

    private ProgressBar mProgressBar;

    private Button mScanButton;

    private final Number START_SCAN_ON_CLICK = 1;
    private final Number STOP_SCAN_ON_CLICK = 2;
    private final static int MIN_TIME_BETWEEN_DEVICE_CLICKS_MS = 400;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new BLEScanFragment())
                    .add(R.id.container, new DeviceDrawerFragment())
                    .add(R.id.container, new HomeDrawerFragment(), "HomeDrawer")
                    .commit();
        }

        mBTDeviceListCopy = new SparseArray<BTService.DiscoveredDevice>();
        mDeviceListAdapter = new DeviceListAdapter();

        EventBus.getDefault().register(this);

    }

    public DeviceListAdapter getDeviceListAdapter() {
        return mDeviceListAdapter;

    }

    public SparseArray<BTService.DiscoveredDevice> getBTDeviceListCopy() {
        return mBTDeviceListCopy;
    }

    @Override
    public void onResume() {

        mInflator = BLEScanActivity.this.getLayoutInflater();

        mProgressBar = (ProgressBar)this.findViewById(R.id.btScanProgress);
        mScanButton = (Button)this.findViewById(R.id.scanButton);
        mScanButton.setTag(START_SCAN_ON_CLICK);

        //show any connected devices on initial display / resume
        //if no devices found - start scan
        BTService.getSingleInstance().resetListToConnectedDeviceOnly();

        Log.i(TAG, "Number of devices:" + BTService.getSingleInstance().mDevices.size());

        if (BTService.getSingleInstance().getIsBluetoothInitialized()) {

            if (BTService.getSingleInstance().mDevices.size() == 0) {
                BTService.getSingleInstance().startScan();
            }

        }

        super.onResume();

    }

    @Override
    public void onPause() {

        BTService.getSingleInstance().stopScan();

        super.onPause();
        Log.i(TAG, "onPause");
    }

    public void scanClick(View v) {

        if (BTService.getSingleInstance().getIsBluetoothInitialized()) {

            if (v.getTag() == STOP_SCAN_ON_CLICK) {
                BTService.getSingleInstance().stopScan();
            } else {
                BTService.getSingleInstance().startScan();
            }

        }
    }

    static public class BLEScanFragment extends ListFragment {

        private long mTimeOfLastDeviceClick = 0;

        public BLEScanFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            super.onActivityCreated(savedInstanceState);
            setListAdapter(((BLEScanActivity)getActivity()).getDeviceListAdapter());
        }

        @Override
        public void onListItemClick(ListView list, View v, int pos, long id) {

            //prevent excessive connect / reconnect requests by user (
            if (System.currentTimeMillis() - mTimeOfLastDeviceClick > MIN_TIME_BETWEEN_DEVICE_CLICKS_MS) {
                mTimeOfLastDeviceClick = System.currentTimeMillis();

                try {
                    int key = ((BLEScanActivity)getActivity()).getBTDeviceListCopy().keyAt(pos);
                    BTService.DiscoveredDevice device = ((BLEScanActivity)getActivity()).getBTDeviceListCopy().get(key);

                    Message msg;

                    if (device.getConnectionStatus() == BTService.ConnectionStatus.DEVICE_CONNECTED) {
                        Log.i(TAG, "User clicked on connected bobber - disconnecting");
                        BTService.getSingleInstance().disconnectBobber();
                    } else if (device.getConnectionStatus() == BTService.ConnectionStatus.DEVICE_DISCONNECTED) {
                        Log.i(TAG, "User clicked on disconnected bobber - connecting");
                        BTService.getSingleInstance().connectToBobber(device);
                    }

                    //(no user action if device is connecting)

                } catch (Exception e) {

                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_ble_scan, container, false);

            return rootView;
        }

    }

    public void onEvent(final BTService.DeviceDidConnect deviceUpdate) {
        updateDeviceList();
    }

    public void onEvent(final BTService.DeviceStartedConnecting deviceUpdate) {
        updateDeviceList();
    }

    public void onEvent(final BTService.DeviceDidDisconnect deviceUpdate) {
        updateDeviceList();
    }

    public void onEvent(final BTService.DeviceListUpdated deviceUpdate) {
        updateDeviceList();
    }

    public void onEvent(final BTService.BTScanStarted deviceUpdate) {
        mProgressBar.setVisibility(View.VISIBLE);
        mScanButton.setText(getResources().getText(R.string.bluetooth_stop_search));
        mScanButton.setTag(STOP_SCAN_ON_CLICK);
    }

    public void onEvent(final BTService.BTScanStopped deviceUpdate) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mScanButton.setText(getResources().getText(R.string.bluetooth_start_search));
        mScanButton.setTag(START_SCAN_ON_CLICK);
    }


    public void updateDeviceList() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //update local copy of device list
                //copy of device list is maintained to assure all changes to list happen on UI thread
                mBTDeviceListCopy = BTService.getSingleInstance().mDevices.clone();
                mDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    private String getNameForDevice(BluetoothDevice device)
    {
        SharedPreferences userPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int userDeviceHash = userPreferences.getInt(BTService.KEY_USER_DEVICE_HASH, 0);

        if (device.hashCode() == userDeviceHash) {
            return userPreferences.getString(UserService.KEY_NICKNAME, BTService.DEVICE_NAME);
        }

        return device.getName();

    }


    private class DeviceListAdapter extends BaseAdapter {

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            class ListItemContents {
                CheckBox deviceCheckBox;
                TextView deviceStatusText;
                ProgressBar deviceConnectProgress;
            }

            final ListItemContents listItemContents;

            //create cell view if needed
            if (view == null) {
                view = mInflator.inflate(R.layout.list_item_device, null);

                listItemContents = new ListItemContents();
                listItemContents.deviceCheckBox = (CheckBox) view.findViewById(R.id.deviceCheckBox);
                listItemContents.deviceStatusText = (TextView) view.findViewById(R.id.deviceStatusTextView);
                listItemContents.deviceConnectProgress = (ProgressBar) view.findViewById(R.id.deviceConnectProgress);


                view.setTag(listItemContents);
            } else {
                listItemContents = (ListItemContents) view.getTag();
            }

            int key = mBTDeviceListCopy.keyAt(i);
            BTService.DiscoveredDevice device = mBTDeviceListCopy.get(key);

            String deviceName = getNameForDevice(device.getBTDevice());

            if (mShowBobberAddress) {
                deviceName = deviceName + " " + device.getBTDevice().getAddress().hashCode();
            }

            if (deviceName != null && deviceName.length() > 0) {
                listItemContents.deviceCheckBox.setText(deviceName);
            }

            if (device.getConnectionStatus() == BTService.ConnectionStatus.DEVICE_CONNECTED) {
                //report as connected if fully initialized
                listItemContents.deviceCheckBox.setChecked(true);
                listItemContents.deviceStatusText.setText(getResources().getText(R.string.bluetooth_connected));
                listItemContents.deviceConnectProgress.setVisibility(View.INVISIBLE);

            } else if (device.getConnectionStatus() == BTService.ConnectionStatus.DEVICE_CONNECTING) {
                listItemContents.deviceCheckBox.setChecked(false);
                listItemContents.deviceStatusText.setText("");

                listItemContents.deviceConnectProgress.setVisibility(View.VISIBLE);

            } else {
                //not connected at all
                listItemContents.deviceCheckBox.setChecked(false);
                listItemContents.deviceStatusText.setText(getResources().getText(R.string.bluetooth_disconnected));
                listItemContents.deviceConnectProgress.setVisibility(View.INVISIBLE);
            }
            return view;
        }

        @Override
        public int getCount() {
            return mBTDeviceListCopy.size();
        }

        @Override
        public Object getItem(int i) {
            int key = mBTDeviceListCopy.keyAt(i);
            return mBTDeviceListCopy.get(key);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

    }

    public void onEventMainThread(UserService.BobberPurchaseDateStatus notification) {

        Date datePurchased = BTService.getSingleInstance().getDatePurchased();
        if (datePurchased == null) {

            Intent activity = new Intent(this, PurchaseDateActivity.class);
            startActivity(activity);

        }
    }
}

