package com.dudu.run.utils;

import android.content.Context;
import android.widget.Toast;
public class r {
    private static Toast a;

    public static void a(Context context, CharSequence charSequence) {
        if (context == null || charSequence == null || charSequence.toString().trim().isEmpty()) return;
        Toast toast = a;
        if (toast != null) {
            toast.cancel();
        }
        if (context != null) {
            Toast toastMakeText = Toast.makeText(context.getApplicationContext(), charSequence, 0);
            a = toastMakeText;
            toastMakeText.show();
        }
    }
}
