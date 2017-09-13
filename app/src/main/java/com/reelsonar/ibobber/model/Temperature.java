// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Temperature {

    private static String TAG = "Temperature";
    private static LinkedHashMap mTempMap = new LinkedHashMap();

    public Temperature() {

        mTempMap.put(1081, 0);
        mTempMap.put(1044, 1);
        mTempMap.put(1007, 2);
        mTempMap.put(972, 3);
        mTempMap.put(938, 4);
        mTempMap.put(905, 5);
        mTempMap.put(874, 6);
        mTempMap.put(843, 7);
        mTempMap.put(814, 8);
        mTempMap.put(785, 9);
        mTempMap.put(758, 10);
        mTempMap.put(731, 11);
        mTempMap.put(705, 12);
        mTempMap.put(681, 13);
        mTempMap.put(657, 14);
        mTempMap.put(634, 15);
        mTempMap.put(612, 16);
        mTempMap.put(591, 17);
        mTempMap.put(570, 18);
        mTempMap.put(551, 19);
        mTempMap.put(532, 20);
        mTempMap.put(513, 21);
        mTempMap.put(496, 22);
        mTempMap.put(479, 23);
        mTempMap.put(462, 24);
        mTempMap.put(447, 25);
        mTempMap.put(431, 26);
        mTempMap.put(417, 27);
        mTempMap.put(403, 28);
        mTempMap.put(389, 29);
        mTempMap.put(376, 30);

        mTempMap.put(363, 31);
        mTempMap.put(351, 32);
        mTempMap.put(339, 33);
        mTempMap.put(328, 34);
        mTempMap.put(317, 35);
        mTempMap.put(307, 36);
        mTempMap.put(297, 37);

    }

    static public int findCelsius(int celsius) {

        if ( celsius  >= 1081 ) return (0);
        if ( celsius  >= 1044 ) return (1);
        if ( celsius  >= 1007 ) return (2);
        if ( celsius  >= 972 ) return (3);
        if ( celsius  >= 938 ) return (4);
        if ( celsius  >= 905 ) return (5);
        if ( celsius  >= 874 ) return (6);
        if ( celsius  >= 843 ) return (7);
        if ( celsius  >= 814 ) return (8);
        if ( celsius  >= 785 ) return (9);
        if ( celsius  >= 758 ) return (10);
        if ( celsius  >= 731 ) return (11);
        if ( celsius  >= 705 ) return (12);
        if ( celsius  >= 681 ) return (13);
        if ( celsius  >= 657 ) return (14);
        if ( celsius  >= 634 ) return (15);
        if ( celsius  >= 612 ) return (16);
        if ( celsius  >= 591 ) return (17);
        if ( celsius  >= 570 ) return (18);
        if ( celsius  >= 551 ) return (19);
        if ( celsius  >= 532 ) return (20);
        if ( celsius  >= 513 ) return (21);
        if ( celsius  >= 496 ) return (22);
        if ( celsius  >= 479 ) return (23);
        if ( celsius  >= 462 ) return (24);
        if ( celsius  >= 447 ) return (25);
        if ( celsius  >= 431 ) return (26);
        if ( celsius  >= 417 ) return (27);
        if ( celsius  >= 403 ) return (28);
        if ( celsius  >= 389 ) return (29);
        if ( celsius  >= 376 ) return (30);

        if ( celsius  >= 363 ) return (31);
        if ( celsius  >= 351 ) return (32);
        if ( celsius  >= 339 ) return (33);
        if ( celsius  >= 328 ) return (34);
        if ( celsius  >= 317 ) return (35);
        if ( celsius  >= 307 ) return (36);
        if ( celsius  >= 297 ) return (37);


        return(38);

    } /* End of findCelsius */

    /*--------------------------------------------------------------------------------
        findCelsiusM

        Converts the given value to celsius from the table.
    ---------------------------------------------------------------------------------*/

    static public int findCelsiusM (int celsius) {

        int lastValue = 1081;

        Set set = mTempMap.entrySet();
        Iterator it = set.iterator();

        while( it.hasNext() ) {
            Map.Entry entry = (Map.Entry) it.next();

            int key = (Integer) entry.getKey();
            int value = (Integer) entry.getValue();

//            Log.d(TAG, "Temp key: " + key + " Temp value: " + value);

            if ( key < celsius ) {
                break;
            }

            lastValue = value;
        }

        return(lastValue);

    } /* End of findCelsiusM */


    /*--------------------------------------------------------------------------------
        celsiusToFahrenheit

        Converts a celsius value to fahrenheit
    ---------------------------------------------------------------------------------*/

    static public int celsiusToFahrenheit(int celsius) {

        int fahrenheit = (int) ((((double) celsius * 9.0) / 5.0) + 32.0);
        return(fahrenheit);

    } /* End of celsiusToFahrenheit */

}
