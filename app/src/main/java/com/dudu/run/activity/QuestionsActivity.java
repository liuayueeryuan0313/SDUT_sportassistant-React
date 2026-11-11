package com.dudu.run.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.ui.RunnerUi;

public final class QuestionsActivity extends MotionActivity {
    private static final Uri SCHOOL_FAQ = Uri.parse("http://sports2.sdut.edu.cn/qa.html");

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        RunnerUi ui = new RunnerUi(this);
        NativeMaterial material = new NativeMaterial(this);
        LinearLayout page = material.settingsPage(this, "常见问题");
        LinearLayout card = ui.group();
        card.setPadding(ui.dp(16), ui.dp(16), ui.dp(16), ui.dp(16));
        card.addView(ui.text("学校运动助手帮助", 18, ui.ink, true));
        ui.gap(card, 8);
        card.addView(ui.text("在浏览器中查看学校发布的常见问题。", 14, ui.muted, false));
        ui.gap(card, 16);
        card.addView(material.button("打开常见问题", this::openFaq));
        page.addView(card);
    }

    private void openFaq() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, SCHOOL_FAQ));
        } catch (ActivityNotFoundException missing) {
            Toast.makeText(this, "未找到可打开网页的浏览器", Toast.LENGTH_SHORT).show();
        }
    }
}
