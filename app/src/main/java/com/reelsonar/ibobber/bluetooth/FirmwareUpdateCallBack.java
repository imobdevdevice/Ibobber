package com.reelsonar.ibobber.bluetooth;

/**
 * Created by rich on 12/3/15.
 */
public interface FirmwareUpdateCallBack {
    public void gotFirmwareImageType();
    public void flashProgressUpdate(int percent);
    public void flashErrorOccurred();
    public void flashComplete();
}
