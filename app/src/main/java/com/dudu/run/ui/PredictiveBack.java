package com.dudu.run.ui;

import android.app.Activity;
import android.os.Build;
import android.window.OnBackInvokedDispatcher;

public final class PredictiveBack {
    private PredictiveBack() {}

    public static void confirmBeforeLeaving(Activity activity, Runnable action) {
        if (Build.VERSION.SDK_INT >= 33) Api33.register(activity, action);
    }

    @android.annotation.TargetApi(33)
    private static final class Api33 {
        static void register(Activity activity, Runnable action) {
            activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, action::run);
        }
    }

}
