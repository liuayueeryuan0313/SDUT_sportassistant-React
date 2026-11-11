package com.dudu.run.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.dudu.run.R;

public final class NativeNavigation {
    public static final String SELECTED_TAB = "native_navigation_tab";
    private NativeNavigation() {}

    private static void copyDockSelection(Activity source, Intent intent) {
        intent.putExtra(PageMotion.PAGE, true);
        LinearLayout dock = source.findViewById(R.id.layout_tabs);
        if (dock == null) return;
        int selected = R.id.layout_tab_mine;
        for (int i = 0; i < dock.getChildCount(); i++) {
            if (dock.getChildAt(i).isSelected()) selected = dock.getChildAt(i).getId();
        }
        intent.putExtra(SELECTED_TAB, selected);
    }

    public static void launch(Activity source, Intent intent) {
        copyDockSelection(source, intent);
        source.startActivity(intent, android.app.ActivityOptions.makeCustomAnimation(source, 0, 0).toBundle());
    }

    public static void launchForResult(Activity source, Intent intent, int request) {
        copyDockSelection(source, intent);
        source.startActivityForResult(intent, request, android.app.ActivityOptions.makeCustomAnimation(source, 0, 0).toBundle());
    }

    public static void attachDock(Activity activity) {
        if (activity instanceof com.dudu.run.activity.RecordDetailActivity) return;
        View existing = activity.findViewById(R.id.layout_tabs);
        if (existing != null) return;
        if (!activity.getIntent().hasExtra(SELECTED_TAB)) return;
        FrameLayout content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        RunnerUi ui = new RunnerUi(activity);
        if (content.getChildCount() > 0) {
            View page = content.getChildAt(0);
            page.setPadding(page.getPaddingLeft(), page.getPaddingTop(),
                    page.getPaddingRight(), page.getPaddingBottom() + ui.dp(86));
        }
        ui.addDock(activity, content, activity.getIntent().getIntExtra(SELECTED_TAB, R.id.layout_tab_mine));
    }
}
