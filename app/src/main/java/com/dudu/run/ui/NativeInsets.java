package com.dudu.run.ui;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.dudu.run.R;

public final class NativeInsets implements Application.ActivityLifecycleCallbacks {
    public static void apply(Activity activity, int background) {
        Window window = activity.getWindow();
        edgeToEdge(window);
        View content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        content.setBackgroundColor(PageMotion.manages(activity) ? Color.TRANSPARENT : background);
        installPadding(content, true);
    }

    public static void apply(Dialog dialog, View panel) {
        edgeToEdge(dialog.getWindow());
        installPadding(panel, false);
    }

    private static void edgeToEdge(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 28) window.setNavigationBarDividerColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false);
        } else {
            View decor = window.getDecorView();
            decor.setSystemUiVisibility(decor.getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    private static void installPadding(View view, boolean includeTop) {
        if (view.getTag(R.id.runner_insets_padding) == null) {
            int[] initial = {view.getPaddingLeft(), view.getPaddingTop(),
                    view.getPaddingRight(), view.getPaddingBottom()};
            view.setTag(R.id.runner_insets_padding, initial);
            view.setOnApplyWindowInsetsListener((target, insets) -> {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                if (Build.VERSION.SDK_INT >= 30) {
                    Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                    left = bars.left;
                    top = bars.top;
                    right = bars.right;
                    bottom = bars.bottom;
                    if (includeTop) bottom = Math.max(bottom, insets.getInsets(WindowInsets.Type.ime()).bottom);
                }
                target.setPadding(initial[0] + left, initial[1] + (includeTop ? top : 0),
                        initial[2] + right, initial[3] + bottom);
                return insets;
            });
        }
        view.requestApplyInsets();
    }

    @Override public void onActivityResumed(Activity activity) {
        NativeNavigation.attachDock(activity);
        new RunnerUi(activity).applySystemBars(activity);
        PageMotion.install(activity);
    }
    @Override public void onActivityCreated(Activity activity, Bundle state) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
    @Override public void onActivityDestroyed(Activity activity) { PageMotion.destroy(activity); }
}
