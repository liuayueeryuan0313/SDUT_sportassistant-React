package com.dudu.run.utils;

import android.content.Context;
import android.os.Vibrator;
public class s {
    public static void a(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(new long[]{100, 400, 200, 400}, -1);
        }
    }
}
