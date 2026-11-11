package com.dudu.run.permission;

import android.app.Activity;
import android.content.Context;
import java.util.ArrayList;
public final class d {
    public static String[] a(Activity activity, String[] strArr) {
        ArrayList arrayList = new ArrayList();
        for (String str : strArr) {
            if (!b(activity, str)) {
                arrayList.add(str);
            }
        }
        String[] strArr2 = new String[arrayList.size()];
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            strArr2[i] = (String) arrayList.get(i);
        }
        return strArr2;
    }

    public static boolean b(Context context, String str) {
        try {
            return com.dudu.legacy.androidx.core.content.a.a(context, str) == 0;
        } catch (RuntimeException unused) {
            return false;
        }
    }

    public static boolean c(Context context, String... strArr) {
        for (String str : strArr) {
            if (!b(context, str)) {
                return false;
            }
        }
        return true;
    }

    public static boolean d(Activity activity, String... strArr) {
        for (String str : strArr) {
            if (com.dudu.legacy.androidx.core.app.a.l(activity, str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean e(int... iArr) {
        if (iArr.length == 0) {
            return false;
        }
        for (int i : iArr) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }
}
