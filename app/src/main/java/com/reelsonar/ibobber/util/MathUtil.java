package com.reelsonar.ibobber.util;

import android.content.Context;
import com.reelsonar.ibobber.service.UserService;

/**
 * Created by james on 9/5/14.
 */
public class MathUtil {

    public static final double FEET_PER_METER = 3.28084;

    public static double metersToUnitOfMeasure(double meters, Context context) {
        UserService userService = UserService.getInstance(context);
        if (userService.isMetric()) {
            return meters;
        } else {
            return meters * FEET_PER_METER;
        }
    }

    public static int celsiusToUnitOfMeasurement(int celsius, Context context) {
        UserService userService = UserService.getInstance(context);
        if (userService.isMetric()) {
            return celsius;
        } else {
            double fahrenheit = (((double)celsius * 9.0) / 5.0) + 32.0;
            return (int)fahrenheit;
        }
    }

    public static double feetToMeters(double feet) {
        return feet / FEET_PER_METER;
    }

    public static int roundToNearest(int val, int modVal) {
        int remainder = val % modVal;
        int nearest = val;

        if (remainder != 0) {
            nearest += modVal - remainder;
        }

        return nearest;
    }

    public static String getFishDepthText(final double depth) {
        int rounded = (int)Math.round(depth);
        return String.valueOf(rounded);
    }


}
