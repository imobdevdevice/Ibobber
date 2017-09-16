// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.bluetooth;

import java.util.UUID;


public class BTConstants {
    public static final String DEVICE_NAME = "iBobber";

    public static final boolean ALLOW_A_TO_B_FW_SWAP_ON_SAME_FW_VERSION = false;

    public static final String PARSE_FW_UPDATE_CLASS = "Firmware";
    public static final String PARSE_FW_TEST_UPDATE_CLASS = "FWTest";

    /* Client Configuration Descriptor */
    public static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /* iBobber Services */
    public static final UUID ACCEL_SERVICE_UUID = UUID.fromString("1791FFA0-3853-11E3-AA6E-0800200C9A66");
    public static final UUID ACCEL_ENABLE_UUID = UUID.fromString("1791FFA1-3853-11E3-AA6E-0800200C9A66");
    public static final UUID ACCEL_THRESHOLD_UUID = UUID.fromString("1791FFA6-3853-11E3-AA6E-0800200C9A66");
    public static final UUID STRIKE_ALARM_UUID = UUID.fromString("1791FFA7-3853-11E3-AA6E-0800200C9A66");

    public static final UUID CUSTOM_SERVICE_UUID = UUID.fromString("1791FF90-3853-11E3-AA6E-0800200C9A66");
    public static final UUID SONAR_ENABLE_CHAR_UUID = UUID.fromString("1791FF91-3853-11E3-AA6E-0800200C9A66");
    public static final UUID TEMP_DATA_CHAR_UUID = UUID.fromString("1791FF92-3853-11E3-AA6E-0800200C9A66");
    public static final UUID ECHO_DATA_UUID = UUID.fromString("1791FF93-3853-11E3-AA6E-0800200C9A66");

    public static final UUID DEVICE_ADDRESS_DATA_UUID = UUID.fromString("1791FF9E-3853-11E3-AA6E-0800200C9A66");
    public static final UUID SLOW_MODE_DATA_UUID      = UUID.fromString("1791FF9F-3853-11E3-AA6E-0800200C9A66");

    public static final UUID LIGHT_DATA_CHAR_UUID = UUID.fromString("1791FF94-3853-11E3-AA6E-0800200C9A66");
    public static final UUID BUZZER_DATA_CHAR_UUID = UUID.fromString("1791FF9D-3853-11E3-AA6E-0800200C9A66");

    public static final UUID WATER_DETECT_CHAR_UUID = UUID.fromString("1791FF9C-3853-11E3-AA6E-0800200C9A66");


    public static final UUID BATT_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    public static final UUID BATT_DATA_CHAR_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    public static final UUID DEVICE_SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
    public static final UUID FIRMWARE_VERSION_CHAR_UUID = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public static final UUID HARDWARE_REV_DATA_UUID = UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB");
//                                                                     00002a27-0000-1000-8000-00805f9b34fb
    /* OAD Service */
    public static final UUID OAD_SERVICE_UUID = UUID.fromString("F000FFC0-0451-4000-B000-000000000000");
    public static final UUID IMAGE_NOTIFY_CHAR_UUID = UUID.fromString("F000FFC1-0451-4000-B000-000000000000");
    public static final UUID IMAGE_BLOCK_CHAR_UUID = UUID.fromString("F000FFC2-0451-4000-B000-000000000000");

    public static final int LIGHT_OFF = 0;
    public static final int LIGHT_ON = 1;
    public static final int LIGHT_FLASH = 2;

    public static final int BUZZER_OFF = 0;
    public static final int BUZZER_ON = 1;

    public static final int HARDWARE_REV_1 = 1;
    public static final int HARDWARE_REV_2 = 2;
    public static final int HARDWARE_REV_3 = 3;
    public static final int HARDWARE_REV_4 = 4;
    public static final int HARDWARE_REV_5 = 5;
    public static final int HARDWARE_REV_6 = 6;

    public static final int SONAR_DISABLE = 0;
    public static final int SONAR_ENABLE = 1;

    public static final int STRIKE_ALARM_DISABLE = 0;
    public static final int STRIKE_ALARM_ENABLE = 1;

    public static final int STRIKE_ALARM_MAX_VALUE = 110;

    public static final int SLOW_MODE_DISABLE = 0;
    public static final int SLOW_MODE_ENSABLE = 1;

}
