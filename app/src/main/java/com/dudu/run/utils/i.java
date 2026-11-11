package com.dudu.run.utils;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
public class i {
    public static final boolean a(Context context) {
        return ((LocationManager) context.getSystemService("location")).isProviderEnabled("gps");
    }

    public static final void b(Context context) {
        context.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
    }
}
