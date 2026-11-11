package com.dudu.run.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
public class o {
    public static String a(double d2, float f2) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        double d3 = d2 * (((double) f2) / 1000.0d) * 1.036d;
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        return decimalFormat.format(d3);
    }

    public static String b(float f2) {
        if (f2 == 0.0f) {
            return "0.00";
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        return decimalFormat.format(((double) f2) / 1000.0d);
    }

    public static String c(long j) {
        long j2 = j % 3600;
        return String.format("%02d:%02d:%02d", Long.valueOf(j / 3600), Long.valueOf(j2 / 60), Long.valueOf(j2 % 60));
    }

    public static String d(long j) {
        long j2 = j % 3600;
        String str = String.format("%02d:%02d:%02d", Long.valueOf(j / 3600), Long.valueOf(j2 / 60), Long.valueOf(j2 % 60));
        return str.startsWith("00") ? str.substring(3) : str;
    }

    public static String e(float f2, int i) {
        return (f2 < 20.0f || i == 0) ? "00'00\"" : f((int) (i / (f2 / 1000.0f)));
    }

    public static String f(int i) {
        return i == 0 ? "00'00\"" : String.format("%02d'%02d\"", Integer.valueOf(i / 60), Integer.valueOf(i % 60));
    }
}
