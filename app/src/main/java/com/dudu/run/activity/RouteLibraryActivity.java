package com.dudu.run.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.dudu.run.simulation.RouteIO;
import com.dudu.run.simulation.PresetRoutes;
import com.dudu.run.simulation.SimulationActivity;
import com.dudu.run.R;
import com.dudu.run.simulation.SimulationEngine.Point;
import com.dudu.run.ui.RoutePreview;
import com.dudu.run.ui.RunnerUi;
import com.dudu.run.ui.NativeMaterial;
import java.io.InputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class RouteLibraryActivity extends MotionActivity {
    public static final String FROM_PROFILE = "runner_library_from_profile";
    private static final int PICK_GPX = 1;
    private RunnerUi ui;
    private int shownRouteCount;

    public static int count(android.content.Context context) {
        return PresetRoutes.read(context).length();
    }

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if (count(this) != shownRouteCount) render();
    }

    private void render() {
        ui = new RunnerUi(this);
        ui.applySystemBars(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(ui.page);
        LinearLayout page = ui.column();
        page.setPadding(ui.dp(16), ui.dp(11), ui.dp(16), ui.dp(86));
        scroll.addView(page);
        ui.addSubheader(page, "预设路线库", this::finish, this::createRoute);
        JSONArray routes = PresetRoutes.read(this);
        shownRouteCount = routes.length();
        if (routes.length() == 0) page.addView(ui.text("暂无预设路线", 14, ui.muted, false));
        LinearLayout items = ui.group();
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.optJSONObject(i);
            if (route == null) continue;
            int index = i;
            ui.append(items, ui.option(route.optString("name"), route.optInt("points") + " 个轨迹点",
                    R.drawable.ic_demo_route, () -> preview(index, route)));
        }
        page.addView(items);
        FrameLayout root = new FrameLayout(this);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        ui.addDock(this, root, getIntent().getBooleanExtra(FROM_PROFILE, false)
                ? R.id.layout_tab_mine : R.id.layout_tab_sport);
        setContentView(root);
    }

    private void createRoute() {
        if (!PresetRoutes.canAdd(this)) return;
        new NativeMaterial(this).dialog().setTitle("新建预设路线")
                .setItems(new String[]{"在地图上手绘", "导入 GPX"}, (dialog, which) -> {
                    if (which == 0) SimulationActivity.openPresetEditor(this);
                    else pickGpx();
                }).show();
    }

    private void pickGpx() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*"), PICK_GPX);
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_GPX || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        new Thread(() -> {
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) throw new IllegalArgumentException("无法读取文件");
                List<Point> points = RouteIO.readGpx(this, input, false, 0);
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) PresetRoutes.nameAndSave(this, points, this::render);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) Toast.makeText(this, "无法导入 GPX", Toast.LENGTH_SHORT).show();
                });
            }
        }, "planned-gpx-import").start();
    }

    private void preview(int index, JSONObject route) {
        try {
            List<Point> points = RouteIO.parse(route.optString("coordinates"), 0);
            double[] coordinates = new double[points.size() * 2];
            for (int i = 0; i < points.size(); i++) {
                coordinates[i * 2] = points.get(i).latitude;
                coordinates[i * 2 + 1] = points.get(i).longitude;
            }
            LinearLayout content = ui.column();
            content.setPadding(ui.dp(16), ui.dp(10), ui.dp(16), ui.dp(12));
            RoutePreview preview = new RoutePreview(this);
            preview.setCoordinates(coordinates);
            content.addView(preview, new LinearLayout.LayoutParams(-1, ui.dp(180)));
            new NativeMaterial(this).dialog().setTitle(route.optString("name")).setView(content)
                    .setPositiveButton("关闭", null).setNeutralButton("删除", (dialog, which) -> confirmDelete(index))
                    .show();
        } catch (IllegalArgumentException invalid) {
            Toast.makeText(this, "无法读取路线", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete(int index) {
        new NativeMaterial(this).dialog().setTitle("删除这条预设路线？").setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    JSONArray routes = PresetRoutes.read(this);
                    routes.remove(index);
                    PresetRoutes.write(this, routes);
                    render();
                }).show();
    }
}
