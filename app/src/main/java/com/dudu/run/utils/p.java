package com.dudu.run.utils;

import android.content.Context;
import android.text.TextUtils;
import com.dudu.run.bean.CurrentSportDataInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
public class p {
    private static File a(Context context) {
        File file = new File(context.getExternalFilesDir(null), com.dudu.run.simulation.OfflineTestSession.active()?"OfflineTestCache":"CacheFiles");
        if (file.exists() || file.mkdirs()) {
            return file;
        }
        return null;
    }

    private static String b(File file) {
        try (FileInputStream input = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException error) {
            error.printStackTrace();
            return null;
        }
    }

    public static CurrentSportDataInfo c(Context context) {
        File fileA = a(context);
        if (fileA == null) {
            return null;
        }
        File file = new File(fileA, "SportDataCache");
        if (!file.exists()) {
            return null;
        }
        String strB = b(file);
        try {
            if (TextUtils.isEmpty(strB)) {
                return null;
            }
            return (CurrentSportDataInfo) new com.google.gson.e().i(strB, CurrentSportDataInfo.class);
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static void d(Context context) {
        File fileA = a(context);
        if (fileA == null) {
            return;
        }
        File file = new File(fileA, "SportDataCache");
        if (file.exists()) {
            file.delete();
        }
    }

    private static void e(File file, String str) {
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] bytes = str.getBytes();
            output.write(bytes);
            output.flush();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static String f(Context context, CurrentSportDataInfo currentSportDataInfo) {
        File fileA = a(context);
        if (fileA == null || currentSportDataInfo == null) {
            return null;
        }
        File file = new File(fileA, "SportDataCache");
        e(file, new com.google.gson.e().r(currentSportDataInfo));
        return file.getAbsolutePath();
    }
}
