package com.dudu.run.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.dudu.run.BuildConfig;
import com.dudu.run.R;
import com.dudu.run.ui.NativeNavigation;
import com.dudu.run.ui.RunnerUi;
import com.dudu.run.utils.StartupDisclaimer;

public final class AboutActivity extends MotionActivity {
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        RunnerUi ui = new RunnerUi(this);
        ui.applySystemBars(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(ui.page);
        LinearLayout page = ui.column();
        page.setPadding(ui.dp(16), ui.dp(11), ui.dp(16), ui.dp(86));
        scroll.addView(page);
        ui.addSubheader(page, "关于", this::finish, null);
        page.addView(ui.text("运动助手", 22, ui.ink, true));
        ui.gap(page, 10);
        page.addView(ui.text("版本 " + RunnerUi.version(this), 14, ui.muted, false));
        ui.gap(page, 5);
        page.addView(ui.text("编译日期 " + BuildConfig.BUILD_DATE, 14, ui.muted, false));
        ui.gap(page, 20);
        page.addView(ui.text("版权声明", 16, ui.ink, true));
        ui.gap(page, 10);
        page.addView(ui.text(StartupDisclaimer.MESSAGE, 14, ui.ink, false));
        ui.gap(page, 20);
        page.addView(ui.action("项目主页", false, () ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/liuayueeryuan0313/SDUT_sportassistant-React")))));
        FrameLayout root = new FrameLayout(this);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        ui.addDock(this, root, getIntent().getIntExtra(NativeNavigation.SELECTED_TAB, R.id.layout_tab_mine));
        setContentView(root);
    }
}
