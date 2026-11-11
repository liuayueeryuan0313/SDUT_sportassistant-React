package com.dudu.run.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import com.dudu.legacy.androidx.core.content.FileProvider;
import com.tencent.mapsdk.internal.y;
import java.io.File;
public class b {
    public static int a(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    public static String b(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static void c(Context context, String str) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                Uri uriE = FileProvider.e(context, context.getPackageName() + ".fileProvider", new File(str));
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setFlags(y.a);
                intent.addFlags(1);
                intent.setDataAndType(uriE, "application/vnd.android.package-archive");
                context.startActivity(intent);
            } else {
                Intent intent2 = new Intent("android.intent.action.VIEW");
                intent2.setDataAndType(Uri.parse("file://" + str), "application/vnd.android.package-archive");
                intent2.addFlags(y.a);
                context.startActivity(intent2);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
