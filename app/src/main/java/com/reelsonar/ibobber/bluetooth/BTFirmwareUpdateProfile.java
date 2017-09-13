package com.reelsonar.ibobber.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.service.DemoSonarService;
import de.greenrobot.event.EventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by rich on 12/3/15.
 */
public class BTFirmwareUpdateProfile {

    //Event bus
    public static class FirmWareInfoChanged{};

    private static final String TAG = "BTFirmwareUpdateProfile";
    private static final long FIRMWARE_QUERY_MS = TimeUnit.MINUTES.toMillis(60);
    private static final long DISABLE_NOTIFICATIONS_WAIT_MS = TimeUnit.SECONDS.toMillis(5);

    private String mFirmwareVersionAvailable;
    private long mLastFirmwareQueryTime;

    private boolean useTestFirmwareLocation = false;

    private BluetoothGattCharacteristic mImageNotifyCharacteristic = null;
    private BluetoothGattCharacteristic mImageBlockCharacteristic = null;

    private int mImageBlockNum;
    private int mCurrentImageVersion;
    private int mCurrentFirmwareImageType = FIRMWARE_IMAGE_UNKNOWN;

    private FirmwareUpdateCallBack mFirmwareUpdateCallBack;

    private byte[] mFirmwareImage;

    int mFirmwareSizeInFlashWords;
    int mTotalBlocks;

    boolean mUpdateErrorOccurred = false;
    boolean mWriting = false;

//image header format (for reference)
//    typedef struct {
//        uint16 crc1;       // CRC-shadow must be 0xFFFF.
//        // User-defined Image Version Number - default logic uses simple a '<' comparison to start an OAD.
//        uint16 ver;
//        uint16 len;        // Image length in 4-byte blocks (i.e. HAL_FLASH_WORD_SIZE blocks).
//        uint8  uid[4];     // User-defined Image Identification bytes.
//        uint8  res[4];     // Reserved space for future use.
//} img_hdr_t;


    private static final int FIRMWARE_VERSION_LSB_HEADER_OFFSET = 4;

    private static final int OAD_BLOCK_SIZE = 16;

    private static final int OAD_IMG_HDR_OSET = 2;
    private static final int OAD_IMG_VERSION_OFFSET = OAD_IMG_HDR_OSET + 2;
    private static final int OAD_IMG_SIZE_OFFSET = OAD_IMG_HDR_OSET + 4;
    private static final int OAD_IMG_UID_OFFSET = OAD_IMG_HDR_OSET + 6;

    private static final int OAD_IMG_ID_SIZE = 4;
    private static final int OAD_IMG_HDR_SIZE = 2 + 2 + OAD_IMG_ID_SIZE;

    private static final int HAL_FLASH_WORD_SIZE = 4;

    private static final int FIRMWARE_UPDATE_LOOP_COUNT = 1;

    public static final int FIRMWARE_IMAGE_A = 0;
    public static final int FIRMWARE_IMAGE_B = 1;
    public static final int FIRMWARE_IMAGE_UNKNOWN = 2;


    private BTService mBTService = BTService.getSingleInstance();

    public void setImageNotifyCharacteristic(BluetoothGattCharacteristic characteristic) {
        mImageNotifyCharacteristic = characteristic;
        mImageNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        enableNotificationsOnNotifyChar();
    }

    public void setImageBlockCharacteristic(BluetoothGattCharacteristic characteristic) {
        mImageBlockCharacteristic = characteristic;
        mImageBlockCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        enableNotificationsOnImageBlockChar();
    }

    private void enableNotificationsOnNotifyChar() {
        mBTService.setNotifyForCharacteristic(mBTService.getConnectedGatt(), mImageNotifyCharacteristic);
    }

    private void disableNotificationsOnNotifyChar() {
        mBTService.disableNotifyForCharacteristic(mBTService.getConnectedGatt(), mImageNotifyCharacteristic);
    }

    private void enableNotificationsOnImageBlockChar() {
        mBTService.setNotifyForCharacteristic(mBTService.getConnectedGatt(), mImageBlockCharacteristic);
    }

    private void disableNotificationsOnImageBlockChar() {
        mBTService.disableNotifyForCharacteristic(mBTService.getConnectedGatt(), mImageBlockCharacteristic);
    }

    public void setFirmwareDiscoveryCallBack(FirmwareUpdateCallBack callback) {
        mFirmwareUpdateCallBack = callback;
    }

    public void beginImageTypeDetermination() {

        mCurrentFirmwareImageType = FIRMWARE_IMAGE_UNKNOWN;
        mUpdateErrorOccurred = false;

        mBTService.writeCharacteristic(mImageNotifyCharacteristic, new byte[] {0x00});

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageTypeDeterminationSecondStep();
            }
        }, 1500);
    }

    private void imageTypeDeterminationSecondStep() {
        mBTService.writeCharacteristic(mImageNotifyCharacteristic, new byte[]{0x01});
    }

    public int getCurrentFirmwareImageType() {
        return mCurrentFirmwareImageType;
    }

    public static int signedByteToUInt(byte b) {
        return (int)b & 0xff;
    }

    void gotImageNotifyUpdate(BluetoothGattCharacteristic characteristic) {

        mCurrentImageVersion = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);

        Log.e(TAG, "gotImageNotifyUpdate - version: " + mCurrentImageVersion);

        Log.e(TAG, "prior mCurrentFirmwareImageType: " + mCurrentFirmwareImageType);

        //this code follows same logic in iOS - seems to allow for updating firmwareimage type on first response - but ignore subsequent response
        if (mCurrentFirmwareImageType == FIRMWARE_IMAGE_UNKNOWN) {
            if ((mCurrentImageVersion & 0x01) == 0) mCurrentFirmwareImageType = FIRMWARE_IMAGE_A;

            if ((mCurrentImageVersion & 0x01) == 1) mCurrentFirmwareImageType = FIRMWARE_IMAGE_B;

            disableNotificationsOnNotifyChar();
        }

        Log.e(TAG, "new mCurrentFirmwareImageType: " + mCurrentFirmwareImageType);

        mFirmwareUpdateCallBack.gotFirmwareImageType();
    }

    public void gotImageBlockUpdate(BluetoothGattCharacteristic characteristic) {
        mImageBlockNum = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
        Log.i(TAG, "gotImageBlockUpdate - imageBlockNum: " + mImageBlockNum);

        if (!mUpdateErrorOccurred) {
            writeImageTick();
        }
    }

    public boolean validateImage(byte[] firmwareImage) {
        if (firmwareImage.length == 0 || mCurrentFirmwareImageType == FIRMWARE_IMAGE_UNKNOWN) return false;

        //just need LSB to determine if A or B firmware
        byte firmwareVersionHeaderByte = firmwareImage[FIRMWARE_VERSION_LSB_HEADER_OFFSET];

        Log.e(TAG, "Downloaded image firmwareVersionHeaderByte: " + firmwareVersionHeaderByte);

        //Assures image types -don't- match (need A for B / B for A)
        if ((firmwareVersionHeaderByte & 0x01) != (mCurrentImageVersion & 0x01)) return true;

        return false;
    }

    public void imageWriteErrorOccurred() {
        mUpdateErrorOccurred = true;
        if (mFirmwareUpdateCallBack != null) mFirmwareUpdateCallBack.flashErrorOccurred();
    }


    public void initiateFirmwareUpdate(final byte[] firmwareImage) {
        mBTService.disableNotifyCharacteristics();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFirmwareImage = firmwareImage;
                mWriting = true;

                byte requestData[] = new byte[OAD_IMG_HDR_SIZE + 2 + 2]; // 12Bytes

                //version from new image
                requestData[0] = mFirmwareImage[OAD_IMG_VERSION_OFFSET];
                requestData[1] = mFirmwareImage[OAD_IMG_VERSION_OFFSET + 1];

                //size from new image
                requestData[2] = mFirmwareImage[OAD_IMG_SIZE_OFFSET];
                requestData[3] = mFirmwareImage[OAD_IMG_SIZE_OFFSET + 1];

                mFirmwareSizeInFlashWords = signedByteToUInt(mFirmwareImage[OAD_IMG_SIZE_OFFSET]) +
                        (mFirmwareImage[OAD_IMG_SIZE_OFFSET + 1] << 8);

                //copy firmware uid
                System.arraycopy(mFirmwareImage, OAD_IMG_UID_OFFSET + 1, requestData, 4, OAD_IMG_ID_SIZE);

                //these values are included in the iOS firmware update code / other CC2540 FW updates online
                //the purpose of them is not obvious
                requestData[OAD_IMG_HDR_SIZE + 0] = 12;
                requestData[OAD_IMG_HDR_SIZE + 1] = 0;

                requestData[OAD_IMG_HDR_SIZE + 2] = 15;
                requestData[OAD_IMG_HDR_SIZE + 3] = 0;

                mBTService.writeCharacteristic(mImageNotifyCharacteristic, requestData);

                mTotalBlocks = mFirmwareSizeInFlashWords / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE);
                mImageBlockNum = 0;

            }
        }, DISABLE_NOTIFICATIONS_WAIT_MS);
    }

    void writeImageTick()
    {
        if (!mWriting) {
            return;
        }

        //Prepare Block
        byte requestData[] = new byte[2 + OAD_BLOCK_SIZE];

        //ios code sends 4 packets at a time to
        //"consecutive packets in the same connection interval"
        //for lack of other info - mimicking that behavior here

        int iBlocks = mImageBlockNum;

        for (int ii = 0; ii < FIRMWARE_UPDATE_LOOP_COUNT; ii++) {
            requestData[0] = (byte)iBlocks;
            requestData[1] = (byte)(iBlocks >> 8);

            int bytesOffset = OAD_BLOCK_SIZE * iBlocks;

            if (iBlocks % 10 == 0) {
                Log.e(TAG, "bytes xfered:" + bytesOffset + " blocks:" + iBlocks + "/" + mTotalBlocks);
                mFirmwareUpdateCallBack.flashProgressUpdate((iBlocks * 100) / mTotalBlocks);
            }

            System.arraycopy(mFirmwareImage, bytesOffset, requestData, 2, OAD_BLOCK_SIZE);
            mBTService.writeCharacteristic(mImageBlockCharacteristic, requestData);

            iBlocks++;

            if (iBlocks == mTotalBlocks) {
                Log.e(TAG, "bytes xfered:" + bytesOffset +" blocks:" + iBlocks + "/" + mTotalBlocks);
                disableNotificationsOnImageBlockChar();
                mWriting = false;


                mFirmwareUpdateCallBack.flashComplete();

                return;
            }
        }
    }

    public void toggleUseTestFirmwareLocation() {
        useTestFirmwareLocation = !useTestFirmwareLocation;
        Context context = BobberApp.getContext();

        Toast toast = Toast.makeText(context, "Use test firmware: " + useTestFirmwareLocation, Toast.LENGTH_SHORT);
        toast.show();
    }

    public String getParseClassForUpdate() {
        return !useTestFirmwareLocation ? BTConstants.PARSE_FW_UPDATE_CLASS : BTConstants.PARSE_FW_TEST_UPDATE_CLASS;
    }

    public void resetLastFirmwareQueryTime() {
        mLastFirmwareQueryTime = 0;
    }

    public void requestLatestFirmwareVersion() {
        long now = SystemClock.uptimeMillis();
        if (mLastFirmwareQueryTime == 0 || now - mLastFirmwareQueryTime > FIRMWARE_QUERY_MS) {


            ParseQuery<ParseObject> query = new ParseQuery<>(getParseClassForUpdate());
            query.orderByDescending("build");
            query.setLimit(1);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> list, final ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Firmware query error", e);
                        return;
                    }

                    if (list != null && list.size() > 0) {
                        mLastFirmwareQueryTime = SystemClock.uptimeMillis();

                        ParseObject result = list.get(0);
                        mFirmwareVersionAvailable = (String)result.get("version");
                        if (isFirmwareUpdateAvailable()) {
                            EventBus.getDefault().post(new FirmWareInfoChanged());
                        }
                    }
                }
            });
        }
    }

    public boolean isFirmwareUpdateAvailable() {
        Context context = BobberApp.getContext();
        return mBTService.getFirmwareRev() != null && mFirmwareVersionAvailable != null
                && !mBTService.getFirmwareRev().equals(mFirmwareVersionAvailable)
                && !DemoSonarService.getSingleInstance(context).getDemoRunning();
    }

    public String getAvailableFirmwareRev() {
        return mFirmwareVersionAvailable;
    }

}
