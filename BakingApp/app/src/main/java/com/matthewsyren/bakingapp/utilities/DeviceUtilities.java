package com.matthewsyren.bakingapp.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Used to provide utilities related to the device
 */

public class DeviceUtilities {
    //Returns the device's orientation
    public static int getDeviceOrientation(Context context){
        return context.getResources()
                .getConfiguration().orientation;
    }

    /*
     * Determines whether the device is a tablet by calculating the device's smallest width
     * Adapted from https://stackoverflow.com/questions/26231752/android-espresso-tests-for-phone-and-tablet?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     */
    public static boolean isTablet(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        activity.getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        //Calculates measurements
        float width = displayMetrics.widthPixels / displayMetrics.density;
        float height = displayMetrics.heightPixels / displayMetrics.density;
        float smallestWidth = Math.min(width, height);

        return smallestWidth >= 600;
    }
}