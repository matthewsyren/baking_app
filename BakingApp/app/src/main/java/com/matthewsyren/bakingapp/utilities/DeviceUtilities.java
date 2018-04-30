package com.matthewsyren.bakingapp.utilities;

import android.content.Context;

/**
 * Used to provide utilities related to the device
 */

public class DeviceUtilities {
    //Returns the device's orientation
    public static int getDeviceOrientation(Context context){
        return context.getResources()
                .getConfiguration().orientation;
    }
}