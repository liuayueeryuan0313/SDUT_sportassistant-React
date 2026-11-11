package com.dudu.run.activity;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.dudu.run.R;
import com.dudu.run.simulation.MapKeySettings;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.ui.RunnerUi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.Locale;

public final class RunnerSettingsActivity extends MotionActivity {
    private RunnerUi ui;
    private NativeMaterial material;
    private SharedPreferences preferences;
    private boolean themePage;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        preferences = RunnerUi.preferences(this);
        themePage = saved != null ? saved.getBoolean("theme") : getIntent().getBooleanExtra("theme", false);
        render();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putBoolean("theme", themePage);
        super.onSaveInstanceState(out);
    }

    @Override protected void onResume() {
        super.onResume();
        if (!ui.hasSamePalette(new RunnerUi(this))) {
            render();
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == 2 && result == RESULT_OK) {
            setResult(RESULT_OK);
            render();
        }
    }

    private void render() {
        ui = new RunnerUi(this);
        material = new NativeMaterial(this);
        ui.applySystemBars(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(ui.page);
        LinearLayout page = ui.column();
        page.setPadding(ui.dp(16), ui.dp(11), ui.dp(16), ui.dp(86));
        scroll.addView(page);
        ui.addSubheader(page, themePage ? "主题设置" : "设置", this::finish, null);
        if (themePage) {
            renderTheme(page);
        } else {
            LinearLayout items = ui.group();
            add(items, "主题设置", dynamic() ? "Material 自动取色" : "手动选色", R.drawable.ic_demo_palette,
                    () -> {
                        Intent intent = new Intent(this, RunnerSettingsActivity.class).putExtra("theme", true);
                        com.dudu.run.ui.NativeNavigation.launchForResult(this, intent, 2);
                    });
            String location = preferences.getString("location_name", "");
            add(items, "预设位置", location.isEmpty() ? "手动添加" : location, R.drawable.ic_demo_map_pin,
                    this::editLocation);
            page.addView(items);
            ui.gap(page, 12);
            LinearLayout support = ui.group();
            add(support, "地图 Key 设置", null, R.drawable.ic_demo_settings, () -> MapKeySettings.show(this));
            add(support, "意见反馈", null, R.drawable.ic_demo_info,
                    () -> com.dudu.run.ui.NativeNavigation.launch(this, new Intent(this, FeedbackActivity.class)));
            page.addView(support);
        }
        FrameLayout root = new FrameLayout(this);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        ui.addDock(this, root, R.id.layout_tab_mine);
        setContentView(root);
    }

    private void add(LinearLayout page, String title, String summary, int icon, Runnable click) {
        ui.append(page, ui.option(title, summary, icon, click));
    }

    private boolean dynamic() {
        return Build.VERSION.SDK_INT >= 31 && preferences.getBoolean("dynamic", false);
    }

    private void renderTheme(LinearLayout page) {
        LinearLayout sample = new LinearLayout(this);
        sample.setGravity(Gravity.CENTER_VERTICAL);
        sample.setPadding(ui.dp(15), ui.dp(15), ui.dp(15), ui.dp(15));
        sample.setBackground(ui.background(ui.tone, 22));
        ImageView mark = ui.icon(R.drawable.ic_demo_palette, ui.onAccent, 39);
        mark.setBackground(ui.background(ui.accent, 15));
        LinearLayout.LayoutParams markParams = new LinearLayout.LayoutParams(ui.dp(39), ui.dp(39));
        markParams.setMarginEnd(ui.dp(12));
        sample.addView(mark, markParams);
        LinearLayout labels = ui.column();
        labels.addView(ui.text("主题预览", 16, ui.ink, true));
        ui.gap(labels, 3);
        labels.addView(ui.text(dynamic() ? "系统壁纸配色" : "手动主题色", 12, ui.muted, false));
        sample.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(sample);
        ui.gap(page, 12);
        LinearLayout colors = ui.group();
        ui.append(colors, settingRow("主题色来源", null, picker(dynamic() ? "Material 自动取色" : "手动选色", this::chooseColorSource)));
        if (!dynamic()) {
            int seed = preferences.getInt("seed", 0xff6750a4);
            MaterialButton swatch = material.button(String.format(Locale.ROOT, "#%06X", seed & 0xffffff), this::editColor);
            ui.append(colors, settingRow("手动主题色", null, swatch));
        }
        page.addView(colors);
        ui.gap(page, 10);
        LinearLayout modes = ui.group();
        String appearance = preferences.getString("appearance", "system");
        MaterialSwitch system = material.toggle();
        system.setContentDescription("自动深色模式");
        system.setChecked("system".equals(appearance));
        system.setOnCheckedChangeListener((button, checked) -> {
            String manual = preferences.getString("manual_appearance", ui.dark ? "dark" : "light");
            preferences.edit().putString("appearance", checked ? "system" : manual).apply();
            setResult(RESULT_OK);
            render();
        });
        ui.append(modes, settingRow("自动深色模式", "跟随系统外观", system));
        if (!"system".equals(appearance)) {
            ui.append(modes, settingRow("显示模式", null,
                    picker("dark".equals(appearance) ? "深色" : "浅色", this::chooseAppearance)));
        }
        page.addView(modes);
    }

    private View picker(String name, Runnable action) {
        MaterialButton label = material.button(name + "  ▾", action);
        label.setMinimumWidth(ui.dp(144));
        label.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        label.setTextColor(ui.ink);
        label.setTextSize(13);
        label.setBackgroundTintList(ColorStateList.valueOf(ui.tone));
        return label;
    }

    private View settingRow(String name, String hint, View control) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(ui.dp(60));
        row.setPadding(ui.dp(12), ui.dp(8), ui.dp(12), ui.dp(8));
        LinearLayout labels = ui.column();
        labels.addView(ui.text(name, 14, ui.ink, false));
        if (hint != null) {
            ui.gap(labels, 3);
            labels.addView(ui.text(hint, 12, ui.muted, false));
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(control, new LinearLayout.LayoutParams(-2, -2));
        return row;
    }

    private void chooseAppearance() {
        String[] values = {"light", "dark"};
        String current = preferences.getString("appearance", "system");
        int selected = "dark".equals(current) ? 1 : 0;
        material.dialog().setTitle("显示模式")
                .setSingleChoiceItems(new String[]{"浅色", "深色"}, selected, (dialog, which) -> {
                    preferences.edit().putString("appearance", values[which])
                            .putString("manual_appearance", values[which]).apply();
                    setResult(RESULT_OK);
                    dialog.dismiss();
                    render();
                }).setNegativeButton("取消", null).show();
    }

    private void chooseColorSource() {
        String[] choices = Build.VERSION.SDK_INT >= 31
                ? new String[]{"手动选色", "Material 自动取色"} : new String[]{"手动选色"};
        boolean dynamic = preferences.getBoolean("dynamic", false) && choices.length > 1;
        material.dialog().setTitle("主题色来源")
                .setSingleChoiceItems(choices, dynamic ? 1 : 0, (dialog, which) -> {
                    preferences.edit().putBoolean("dynamic", which == 1).apply();
                    setResult(RESULT_OK);
                    dialog.dismiss();
                    render();
                }).setNegativeButton("取消", null).show();
    }

    private void editColor() {
        int current = preferences.getInt("seed", 0xff6750a4);
        LinearLayout body = material.dialogBody();
        EditText input = material.field(body, "十六进制颜色", InputType.TYPE_CLASS_TEXT, false);
        input.setText(String.format(Locale.ROOT, "#%06X", current & 0xffffff));
        MaterialButton save = material.primaryAction(body, "保存");
        AlertDialog dialog = material.dialog().setTitle("手动主题色")
                .setMessage("输入颜色值，例如 #6750A4。").setView(body)
                .setNegativeButton("取消", null).create();
        save.setOnClickListener(view -> {
            String value = input.getText().toString().trim();
            if (!value.matches("#[0-9a-fA-F]{6}")) {
                input.setError("请输入 6 位十六进制颜色");
                return;
            }
            preferences.edit().putInt("seed", Color.parseColor(value)).apply();
            setResult(RESULT_OK);
            dialog.dismiss();
            render();
        });
        dialog.show();
    }

    private void editLocation() {
        LinearLayout body = material.dialogBody();
        EditText input = material.field(body, "位置名称", InputType.TYPE_CLASS_TEXT, false);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        input.setText(preferences.getString("location_name", ""));
        MaterialButton save = material.primaryAction(body, "保存");
        AlertDialog.Builder builder = material.dialog().setTitle("预设位置").setView(body)
                .setNegativeButton("取消", null);
        if (input.length() != 0) {
            builder.setNeutralButton("清除", (dialog, which) -> saveLocation(""));
        }
        AlertDialog dialog = builder.create();
        save.setOnClickListener(view -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                input.setError("请输入位置名称");
                return;
            }
            dialog.dismiss();
            saveLocation(value);
        });
        dialog.show();
    }

    private void saveLocation(String name) {
        preferences.edit().putString("location_name", name).apply();
        setResult(RESULT_OK);
        render();
    }
}
