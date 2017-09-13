package com.reelsonar.ibobber.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by james on 9/19/14.
 */
public class Style {

    public static Typeface appTypeface(final Context context) {
        return Typeface.createFromAsset(context.getAssets(),"Gotham-Medium.otf");
    }

    public static Typeface formTypeface(final Context context) {
        return appTypeface(context);
    }

}
