package com.dudu.run.simulation;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.ui.RunnerUi;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class PresetRoutes {
    private static final String KEY = "planned_routes";
    private static final int LIMIT = 20;

    private PresetRoutes() {}

    public static JSONArray read(Context context) {
        try {
            return new JSONArray(RunnerUi.preferences(context).getString(KEY, "[]"));
        } catch (JSONException invalid) {
            return new JSONArray();
        }
    }

    public static boolean canAdd(Context context) {
        if (read(context).length() < LIMIT) return true;
        Toast.makeText(context, "最多保存 " + LIMIT + " 条路线", Toast.LENGTH_SHORT).show();
        return false;
    }

    public static void nameAndSave(Activity activity, List<SimulationEngine.Point> points, Runnable onSaved) {
        if (!canAdd(activity)) return;
        if (points.size() < 2 || points.size() > 5000) {
            Toast.makeText(activity, "预设路线需有 2 至 5000 个路线点", Toast.LENGTH_SHORT).show();
            return;
        }
        SimulationEngine.Point first = points.get(0);
        boolean hasLength = false;
        for (int i = 1; i < points.size(); i++) {
            if (SimulationEngine.distance(first, points.get(i)) > .1) {
                hasLength = true;
                break;
            }
        }
        if (!hasLength) {
            Toast.makeText(activity, "请先画出一段路线", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = points.size();
        String coordinates = RouteIO.format(points);
        NativeMaterial material = new NativeMaterial(activity);
        LinearLayout body = material.dialogBody();
        EditText name = material.field(body, "路线名称", InputType.TYPE_CLASS_TEXT, false);
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        com.google.android.material.button.MaterialButton save = material.primaryAction(body, "保存");
        AlertDialog dialog = material.dialog().setTitle("保存预设路线").setView(body)
                .setNegativeButton("取消", null).create();
        save.setOnClickListener(view -> {
            String title = name.getText().toString().trim();
            if (title.isEmpty()) {
                name.setError("请输入路线名称");
                return;
            }
            JSONArray routes = read(activity);
            if (routes.length() >= LIMIT) {
                canAdd(activity);
                return;
            }
            try {
                routes.put(new JSONObject().put("name", title).put("points", count).put("coordinates", coordinates));
            } catch (JSONException invalid) {
                name.setError("路线无法保存");
                return;
            }
            write(activity, routes);
            dialog.dismiss();
            onSaved.run();
        });
        dialog.show();
    }

    public static void write(Context context, JSONArray routes) {
        RunnerUi.preferences(context).edit().putString(KEY, routes.toString()).apply();
    }
}
