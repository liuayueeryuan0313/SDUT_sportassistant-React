package com.dudu.run.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;
import android.widget.LinearLayout;
import com.dudu.run.R;
import com.dudu.run.activity.HomeActivity;
import com.dudu.run.activity.LoginActivity;
import com.dudu.run.activity.RouteLibraryActivity;
import com.dudu.run.activity.RunnerSettingsActivity;
import com.dudu.run.activity.AboutActivity;
import com.dudu.run.activity.QuestionsActivity;
import com.dudu.run.ui.NativeNavigation;
import com.dudu.run.b;
import com.dudu.run.c;
import com.dudu.run.simulation.OfflineTestActivity;
import com.dudu.run.ui.RunnerUi;

public class MineFragment extends a {
    private RunnerUi ui;
    private LinearLayout page;

    @Override int o() {
        return R.layout.fragment_mine;
    }

    @Override public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        render();
    }

    private void render() {
        ui = new RunnerUi(getActivity());
        getView().setBackgroundColor(ui.page);
        page = getView().findViewById(R.id.runner_profile_content);
        page.removeAllViews();
        page.setPadding(ui.dp(16), ui.dp(11), ui.dp(16), ui.dp(86));
        ui.addHeader(page, "我的", 0, null, null);
        LinearLayout profile = new LinearLayout(getActivity());
        profile.setGravity(Gravity.CENTER_VERTICAL);
        profile.setPadding(ui.dp(16), ui.dp(16), ui.dp(16), ui.dp(16));
        profile.setBackground(ui.background(ui.tone, 24));
        ImageView avatar = ui.icon(R.drawable.ic_demo_user_round, ui.accentText, 44);
        ui.styleAvatar(avatar);
        avatar.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams avatarLayout = new LinearLayout.LayoutParams(ui.dp(44), ui.dp(44));
        avatarLayout.setMarginEnd(ui.dp(12));
        profile.addView(avatar, avatarLayout);
        LinearLayout labels = ui.column();
        labels.addView(ui.text(b.c().o(), 14, ui.ink, true));
        ui.gap(labels, 3);
        labels.addView(ui.text(b.c().l(), 12, ui.muted, false));
        profile.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton signOut = ui.iconButton(R.drawable.ic_demo_log_out, "退出登录", this::confirmSignOut);
        profile.addView(signOut,
                new LinearLayout.LayoutParams(ui.dp(44), ui.dp(44)));
        page.addView(profile);
        ui.gap(page, 12);
        LinearLayout items = ui.group();
        ui.append(items, ui.option("跑步规则", "里程与达标条件", R.drawable.ic_demo_book_open,
                () -> ((HomeActivity) getActivity()).I0(0)));
        ui.append(items, ui.option("预设路线库", "管理已保存的路线", R.drawable.ic_demo_route,
                () -> NativeNavigation.launch(getActivity(), new Intent(getActivity(), RouteLibraryActivity.class)
                        .putExtra(RouteLibraryActivity.FROM_PROFILE, true))));
        ui.append(items, ui.option("设置", "主题与预设位置", R.drawable.ic_demo_settings,
                () -> NativeNavigation.launch(getActivity(), new Intent(getActivity(), RunnerSettingsActivity.class))));
        page.addView(items);
        ui.gap(page, 12);
        LinearLayout test = ui.group();
        ui.append(test, ui.option("常见问题", null, R.drawable.ic_demo_book_open,
                () -> NativeNavigation.launch(getActivity(), new Intent(getActivity(), QuestionsActivity.class))));
        ui.append(test, ui.option("测试模式", "使用示例数据", R.drawable.ic_demo_flask_conical,
                () -> ui.showSheet("测试模式", "使用本机示例数据体验运动界面。", "进入测试",
                        () -> OfflineTestActivity.open(getActivity(), true))));
        page.addView(test);
        ui.gap(page, 12);
        LinearLayout about = ui.group();
        about.addView(ui.option("关于", RunnerUi.version(getActivity()), R.drawable.ic_demo_info,
                () -> NativeNavigation.launch(getActivity(), new Intent(getActivity(), AboutActivity.class))));
        page.addView(about);
    }

    private void confirmSignOut() {
        ui.showSheet("退出登录", "确定退出当前账号？", "退出", () -> {
                    b.c().a();
                    c.n().L();
                    c.n().K();
                    c.n().B();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                });
    }
}
