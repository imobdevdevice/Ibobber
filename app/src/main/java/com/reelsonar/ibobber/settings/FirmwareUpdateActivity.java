package com.reelsonar.ibobber.settings;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.parse.*;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.bluetooth.FirmwareUpdateCallBack;

import java.util.List;

/**
 * Created by rich on 12/3/15.
 */
public class FirmwareUpdateActivity extends Activity {

    enum FirmwareUpdateViewState{
        Prepare,
        GetReady,
        Download,
        Write,
        Reset,
        Done,
        PrepareError,
        DownloadError,
        WriteError
    }

    FirmwareUpdateViewState firmwareUpdateViewState;

    int firmwareType;
    TextView firmwareBodyText;
    Button okButton;
    Button cancelButton;
    ProgressBar spinProgress;
    ProgressBar flashProgress;

    int getBobberInfoTimeoutMS = 30000;
    int writeCompleteRebootInterval = 6000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);

        firmwareBodyText = (TextView)findViewById(R.id.firmwareBodyText);
        okButton = (Button)findViewById(R.id.commandOK);
        cancelButton = (Button)findViewById(R.id.commandCancel);
        flashProgress = (ProgressBar)findViewById(R.id.flashProgressBar);
        spinProgress = (ProgressBar)findViewById(R.id.waitIndicator);

        updateToState(FirmwareUpdateViewState.Prepare);

        if( firmwareUpdateCallBack != null )
            BTService.getSingleInstance().getFirmwareUpdateProfile().setFirmwareDiscoveryCallBack(firmwareUpdateCallBack);

        BTService.getSingleInstance().getFirmwareUpdateProfile().beginImageTypeDetermination();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getBobberInfoTimeout();
            }
        }, getBobberInfoTimeoutMS);
    }

    private void getBobberInfoTimeout() {
        if (firmwareUpdateViewState == FirmwareUpdateViewState.Prepare) updateToState(FirmwareUpdateViewState.PrepareError);
    }

    private void updateToState(FirmwareUpdateViewState newState){
        firmwareUpdateViewState = newState;

        switch (firmwareUpdateViewState) {
            case Prepare:
                firmwareBodyText.setVisibility(View.GONE);
                spinProgress.setVisibility(View.VISIBLE);
                flashProgress.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                break;
            case GetReady:
                firmwareBodyText.setVisibility(View.VISIBLE);
                firmwareBodyText.setText(R.string.firmware_prepare_detail);
                spinProgress.setVisibility(View.GONE);
                okButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                break;
            case Download:
                firmwareBodyText.setText(R.string.firmware_downloading);
                firmwareBodyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                spinProgress.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                break;
            case Write:
                firmwareBodyText.setText(R.string.firmware_writing);
                spinProgress.setVisibility(View.GONE);
                flashProgress.setVisibility(View.VISIBLE);
                break;
            case Reset:
                firmwareBodyText.setVisibility(View.GONE);
                spinProgress.setVisibility(View.VISIBLE);
                flashProgress.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                break;
            case Done:
                firmwareBodyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                firmwareBodyText.setVisibility(View.VISIBLE);
                firmwareBodyText.setText(R.string.firmware_done_detail);
                spinProgress.setVisibility(View.GONE);
                flashProgress.setVisibility(View.GONE);
                okButton.setVisibility(View.VISIBLE);
                okButton.setText(R.string.button_done_uppercase);
                break;
            case PrepareError:
                configViewForErrorDisplay();
                firmwareBodyText.setText(R.string.firmware_prepare_error_detail);
                break;
            case DownloadError:
                configViewForErrorDisplay();
                firmwareBodyText.setText(R.string.firmware_download_error_detail);
                break;
            case WriteError:
                configViewForErrorDisplay();
                firmwareBodyText.setText(R.string.firmware_write_error_detail);
        }
    }

    private void configViewForErrorDisplay() {
        firmwareBodyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        firmwareBodyText.setVisibility(View.VISIBLE);
        spinProgress.setVisibility(View.GONE);
        flashProgress.setVisibility(View.GONE);
        okButton.setVisibility(View.VISIBLE);
        okButton.setText(R.string.button_done_uppercase);
    }


    private FirmwareUpdateCallBack firmwareUpdateCallBack = new FirmwareUpdateCallBack() {
        public void gotFirmwareImageType() {
            //triggered by ble callback - need to assure on main thread
            FirmwareUpdateActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    firmwareType = BTService.getSingleInstance().getFirmwareUpdateProfile().getCurrentFirmwareImageType();
                    if (firmwareUpdateViewState != FirmwareUpdateViewState.PrepareError)
                        updateToState(FirmwareUpdateViewState.GetReady);
                }
            });
        }

        public void flashProgressUpdate(int percent) {
            flashProgress.setProgress(percent);
        }

        public void flashErrorOccurred() {
            //triggered by ble callback - need to assure on main thread
            FirmwareUpdateActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //ignore errors generated when not in write mode (ie - generated by device reboot)
                    if (firmwareUpdateViewState == FirmwareUpdateViewState.Write)
                        updateToState(FirmwareUpdateViewState.WriteError);
                }
            });
        }

        public void flashComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rebootComplete();
                        }
                    }, writeCompleteRebootInterval);

                    updateToState(FirmwareUpdateViewState.Reset);
                }
            });
        }
    };

    private void rebootComplete() {
        updateToState(FirmwareUpdateViewState.Done);
    }

    public void downloadLatestFirmware(String imageType) {

        ParseQuery<ParseObject> query = new ParseQuery<>(BTService.getSingleInstance().getFirmwareUpdateProfile().getParseClassForUpdate());
        query.orderByDescending("build");
        query.whereMatches("area", imageType);

        query.setLimit(1);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> list, final ParseException e) {
                if (e != null) {
                    //error querying parse
                    updateToState(FirmwareUpdateViewState.DownloadError);
                    return;
                }

                if (list != null && list.size() > 0) {
                    ParseObject result = list.get(0);

                    ParseFile imageFile = (ParseFile) result.get("image");
                    imageFile.getDataInBackground(new GetDataCallback() {
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {

                                if (BTService.getSingleInstance().getFirmwareUpdateProfile().validateImage(data)) {

                                    updateToState(FirmwareUpdateViewState.Write);

                                    BTService.getSingleInstance().getFirmwareUpdateProfile().initiateFirmwareUpdate(data);

                                } else {
                                    //error validating image (wrong image area?)
                                    updateToState(FirmwareUpdateViewState.DownloadError);
                                }
                            } else {
                                //error downloading image
                                updateToState(FirmwareUpdateViewState.DownloadError);
                            }
                        }
                    });

                }
            }
        });
    }

    public void okClick(View view) {

        if (firmwareUpdateViewState == FirmwareUpdateViewState.GetReady) {
            //get firmware A if running B and vice-versa
            updateToState(FirmwareUpdateViewState.Download);
            downloadLatestFirmware((firmwareType == 0) ? "B" : "A");
        } else {
            //includes "done" and all errors
            exitActivityAndVoidFlash();
        }

    }

    public void cancelClick(View view) {
        exitActivityAndVoidFlash();
    }

    private void exitActivityAndVoidFlash() {
        BTService.getSingleInstance().getFirmwareUpdateProfile().imageWriteErrorOccurred();
        finish();
    }

}

