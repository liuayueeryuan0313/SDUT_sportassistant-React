package com.dudu.run.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.TextView;
import android.widget.EditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.dudu.run.R;
import com.dudu.run.activity.HomeActivity;
import com.dudu.run.activity.RecordDetailActivity;
import com.dudu.run.activity.RouteLibraryActivity;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.bean.RecordDetailResponseData;
import com.dudu.run.bean.RecordItem;
import com.dudu.run.utils.o;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public final class SportDashboard {
    private static final int ICON_GRAVITY_TEXT_START = 2;
    private static final String[] CARD_NAMES = {"跑步记录", "达标进度", "累计运动", "预设路线库"};
    private final HomeActivity host;
    private final RunnerUi ui;
    private final ScrollView scroll;
    private final LinearLayout grid;
    private final SquareTile[] cards = new SquareTile[CARD_NAMES.length];
    private final List<Integer> order = new ArrayList<>();
    private final TextView lastDate, lastDistance, mileage, stepsTotal, caloriesTotal, progress, updateTime, libraryCount, locationLabel;
    private final TextView progressUnit;
    private final DashboardLocation deviceLocation;
    private final java.util.Set<String> runDates = new java.util.HashSet<>();
    private final java.util.Set<Integer> countedRecords = new java.util.HashSet<>();
    private boolean daysComplete;
    private final RoutePreview recentRoute;
    private final Ring ring;
    private final MaterialAutoCompleteTextView campus;
    private long refreshedAt;
    private boolean askedForRecent;
    private long recentVersion=-1;

    public SportDashboard(HomeActivity host, ScrollView scroll) {
        this.host = host;
        this.scroll = scroll;
        ui = new RunnerUi(host);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(ui.page);
        LinearLayout page = ui.column();
        page.setPadding(ui.dp(16), ui.dp(11), ui.dp(16), ui.dp(86));
        scroll.addView(page);
        ui.addHeader(page, greeting(), R.drawable.ic_demo_user_round, "打开我的", () -> host.I0(2));

        LinearLayout start = ui.column();
        start.setBackground(ui.outlined(ui.tone, 26));
        start.setPadding(ui.dp(16), ui.dp(17), ui.dp(16), ui.dp(17));
        LinearLayout startHeader = new LinearLayout(host);
        startHeader.setGravity(Gravity.CENTER_VERTICAL);
        startHeader.addView(ui.text("开始跑步", 21, ui.ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        campus = campusPicker();
        startHeader.addView(campus, new LinearLayout.LayoutParams(ui.dp(96), ui.dp(44)));
        start.addView(startHeader);
        ui.gap(start, 12);
        FrameLayout location = new FrameLayout(host);
        location.setBackground(ui.background(ui.map, 17));
        location.setClipToOutline(true);
        LinearLayout caption = new LinearLayout(host);
        caption.setGravity(Gravity.CENTER_VERTICAL);
        caption.setPadding(ui.dp(7), ui.dp(3), ui.dp(7), ui.dp(3));
        caption.setBackground(ui.background(ui.surface, 14));
        ImageView locate = ui.icon(R.drawable.ic_demo_locate_fixed, ui.muted, 13);
        LinearLayout.LayoutParams locateLayout = new LinearLayout.LayoutParams(ui.dp(13), ui.dp(13));
        locateLayout.setMarginEnd(ui.dp(4));
        caption.addView(locate, locateLayout);
        locationLabel = ui.text("", 11, ui.muted, false);
        caption.addView(locationLabel);
        FrameLayout.LayoutParams labelLayout = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.START);
        labelLayout.setMargins(ui.dp(8), 0, ui.dp(8), ui.dp(8));
        location.addView(caption, labelLayout);
        deviceLocation = new DashboardLocation(host, location, locationLabel);
        start.addView(location, new LinearLayout.LayoutParams(-1, ui.dp(152)));
        ui.gap(start, 11);
        NativeMaterial material = new NativeMaterial(host);
        LinearLayout runActions = new LinearLayout(host);
        runActions.setGravity(Gravity.CENTER_VERTICAL);
        MaterialButton run = material.button("自动跑步", null);
        run.setId(R.id.btn_run);
        run.setContentDescription("自动跑步，开始后绘制路线");
        run.setFocusable(true);
        run.setTextSize(14);
        run.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        run.setAllCaps(false);
        run.setIncludeFontPadding(false);
        run.setGravity(Gravity.CENTER);
        run.setIconResource(R.drawable.ic_demo_play);
        run.setIconTint(ColorStateList.valueOf(ui.onAccent));
        run.setIconSize(ui.dp(19));
        run.setIconPadding(ui.dp(6));
        run.setIconGravity(ICON_GRAVITY_TEXT_START);
        runActions.addView(run, new LinearLayout.LayoutParams(0, ui.dp(48), 1));
        MaterialButton realRun = material.tonalButton("开始跑步", null);
        realRun.setId(R.id.btn_run_real);
        realRun.setContentDescription("开始跑步，记录本人真实运动与设备定位");
        realRun.setTextSize(14);
        realRun.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        realRun.setIncludeFontPadding(false);
        realRun.setGravity(Gravity.CENTER);
        realRun.setIconResource(R.drawable.ic_runner_run);
        realRun.setIconTint(ColorStateList.valueOf(ui.accentText));
        realRun.setIconSize(ui.dp(19));
        realRun.setIconPadding(ui.dp(6));
        realRun.setIconGravity(ICON_GRAVITY_TEXT_START);
        LinearLayout.LayoutParams realPosition = new LinearLayout.LayoutParams(0, ui.dp(48), 1);
        realPosition.setMarginStart(ui.dp(8));
        runActions.addView(realRun, realPosition);
        start.addView(runActions, new LinearLayout.LayoutParams(-1, ui.dp(48)));
        page.addView(start);
        ui.gap(page, 10);

        grid = ui.column();
        page.addView(grid);
        SquareTile records = tile(0, () -> host.I0(3));
        recentRoute = new RoutePreview(host, true);
        FrameLayout routeSpace = new FrameLayout(host);
        FrameLayout.LayoutParams previewPosition=new FrameLayout.LayoutParams(-1,-1);
        previewPosition.setMargins(ui.dp(8),ui.dp(6),ui.dp(8),ui.dp(6));
        routeSpace.addView(recentRoute,previewPosition);
        records.addView(routeSpace, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout last = new LinearLayout(host);
        last.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout latest = ui.column();
        latest.addView(ui.text("最近一次", 12, ui.muted, false));
        lastDate = ui.text("", 10, ui.muted, false);
        lastDate.setSingleLine(true);
        lastDate.setVisibility(View.GONE);
        latest.addView(lastDate);
        last.addView(latest, new LinearLayout.LayoutParams(0, -2, 1));
        lastDistance = ui.text("—", 13, ui.ink, true);
        last.addView(lastDistance);
        records.addView(last);

        SquareTile goal = tile(1, this::editProgressGoals);
        View progressArrow = ((LinearLayout) goal.getChildAt(0)).getChildAt(1);
        ((LinearLayout) progressArrow.getParent()).removeView(progressArrow);
        goal.removeAllViews();
        FrameLayout ringFrame = new FrameLayout(host);
        FrameLayout ringCenter = new FrameLayout(host);
        ring = new Ring();
        ringCenter.addView(ring, new FrameLayout.LayoutParams(-1, -1));
        ringFrame.addView(ringCenter, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout numbers = ui.column();
        numbers.setGravity(Gravity.CENTER);
        numbers.addView(ui.text("达标进度", 12, ui.ink, true), new LinearLayout.LayoutParams(-2, -2));
        ui.gap(numbers, 2);
        progress = ui.text("—", 27, ui.ink, true);
        progress.setId(R.id.tv_my_count);
        progress.setGravity(Gravity.CENTER);
        numbers.addView(progress, new LinearLayout.LayoutParams(-2, -2));
        ui.gap(numbers, 1);
        progressUnit = ui.text("", 11, ui.muted, false);
        numbers.addView(progressUnit, new LinearLayout.LayoutParams(-2, -2));
        ringCenter.addView(numbers, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        ringFrame.addView(progressArrow, new FrameLayout.LayoutParams(ui.dp(26), ui.dp(26), Gravity.TOP | Gravity.END));
        goal.addView(ringFrame, new LinearLayout.LayoutParams(-1, -1));

        SquareTile totals = tile(2, null);
        ui.gap(totals, 9);
        stepsTotal=stat(totals, R.drawable.ic_demo_footprints, "步数", "—");
        mileage = stat(totals, R.drawable.ic_demo_route, "里程", "—");
        mileage.setId(R.id.tv_run_length);
        caloriesTotal=stat(totals, R.drawable.ic_demo_flame, "卡路里", "—");
        updateTime = ui.text("等待数据更新", 11, ui.muted, false);
        totals.addView(new View(host), new LinearLayout.LayoutParams(1, 0, 1));
        totals.addView(updateTime);

        SquareTile library = tile(3, () -> open(RouteLibraryActivity.class));
        FrameLayout libraryArt = new FrameLayout(host);
        ImageView glyph = new ImageView(host);
        glyph.setImageResource(R.drawable.runner_library_art);
        glyph.setColorFilter(ui.accent);
        glyph.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        libraryArt.addView(glyph, new FrameLayout.LayoutParams(ui.dp(110), ui.dp(49), Gravity.CENTER_VERTICAL | Gravity.START));
        library.addView(libraryArt, new LinearLayout.LayoutParams(-1, 0, 1));
        libraryCount = ui.text("", 12, ui.muted, false);
        library.addView(libraryCount);

        String saved = RunnerUi.preferences(host).getString("card_order", "0123");
        for (int i = 0; i < saved.length(); i++) {
            int index = Character.digit(saved.charAt(i), 10);
            if (index >= 0 && index < cards.length && !order.contains(index)) order.add(index);
        }
        for (int i = 0; i < cards.length; i++) if (!order.contains(i)) order.add(i);
        renderCards();
        ui.gap(page, 9);
        TextView reorder = ui.action("重新排序", false, this::chooseOrder);
        reorder.setTextSize(13);
        reorder.setTextColor(ui.accentText);
        addStartIcon(reorder, R.drawable.ic_demo_list_restart, ui.accentText, 17);
        LinearLayout.LayoutParams footer = new LinearLayout.LayoutParams(-2, -2);
        footer.gravity = Gravity.CENTER;
        page.addView(reorder, footer);
        scroll.setOnScrollChangeListener((v, x, y, oldX, oldY) -> host.updateSportTab(y > 0));
        renderStats(false);
        updateLocalLabels();
    }

    private void addStartIcon(TextView text, int resource, int color, int size) {
        android.graphics.drawable.Drawable icon = host.getDrawable(resource).mutate();
        icon.setTint(color);
        icon.setBounds(0, 0, ui.dp(size), ui.dp(size));
        text.setCompoundDrawablesRelative(icon, null, null, null);
        text.setCompoundDrawablePadding(ui.dp(6));
    }

    private TextView stat(LinearLayout parent, int resource, String name, String value) {
        LinearLayout row = new LinearLayout(host);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams mark = new LinearLayout.LayoutParams(ui.dp(13), ui.dp(13));
        mark.setMarginEnd(ui.dp(4));
        row.addView(ui.icon(resource, ui.muted, 13), mark);
        row.addView(ui.text(name, 12, ui.muted, false), new LinearLayout.LayoutParams(0, -2, 1));
        TextView amount = ui.text(value, 14, ui.ink, true);
        row.addView(amount);
        parent.addView(row);
        ui.gap(parent, 7);
        return amount;
    }

    public static int preferredCampus(android.content.Context context, int fallback) {
        int value = RunnerUi.preferences(context).getInt("campus", fallback);
        return value == 1 || value == 2 ? value : fallback;
    }

    public static void rememberCampus(android.content.Context context, int campus) {
        RunnerUi.preferences(context).edit().putInt("campus", campus).apply();
    }

    private MaterialAutoCompleteTextView campusPicker() {
        android.content.Context popup = new NativeMaterial(host).dropdownContext();
        MaterialAutoCompleteTextView picker = new MaterialAutoCompleteTextView(popup);
        picker.setInputType(android.text.InputType.TYPE_NULL);
        picker.setKeyListener(null);
        picker.setFocusable(false);
        picker.setFocusableInTouchMode(false);
        picker.setLongClickable(false);
        picker.setSingleLine(true);
        picker.setTextSize(13);
        picker.setTextColor(ui.ink);
        picker.setPadding(ui.dp(9), 0, ui.dp(9), 0);
        picker.setGravity(Gravity.CENTER);
        android.graphics.drawable.Drawable pin = host.getDrawable(R.drawable.ic_demo_map_pin).mutate();
        android.graphics.drawable.Drawable down = host.getDrawable(R.drawable.ic_demo_chevron_down).mutate();
        pin.setTint(ui.accent); down.setTint(ui.accent);
        pin.setBounds(0, 0, ui.dp(14), ui.dp(14));
        down.setBounds(0, 0, ui.dp(14), ui.dp(14));
        picker.setCompoundDrawablePadding(ui.dp(5));
        picker.setCompoundDrawablesRelative(pin, null, down, null);
        picker.setContentDescription("校区");
        picker.setBackground(ui.outlined(ui.surface, 25));
        picker.setDropDownBackgroundDrawable(ui.outlined(ui.surface, 16));
        picker.setDropDownVerticalOffset(ui.dp(44));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(popup, com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                new String[]{"西校区", "东校区"}) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView label = (TextView) super.getView(position, convertView, parent);
                label.setText(getItem(position));
                label.setTextColor(ui.ink);
                label.setTextSize(13);
                label.setSingleLine();
                return label;
            }
        };
        // Use the same official popup primitive as the exposed dropdown. The
        // compact, read-only 96dp anchor has no TextInputLayout end-icon delegate.
        androidx.appcompat.widget.ListPopupWindow menu = new androidx.appcompat.widget.ListPopupWindow(popup);
        menu.setAnchorView(picker);
        menu.setAdapter(adapter);
        menu.setModal(true);
        menu.setWidth(ui.dp(112));
        menu.setBackgroundDrawable(picker.getDropDownBackground());
        picker.setOnClickListener(view -> menu.show());
        picker.setText(preferredCampus(host, 2) == 1 ? "西校区" : "东校区", false);
        menu.setOnItemClickListener((parent, view, position, id) -> {
                int campus = position == 0 ? 1 : 2;
                if (!RunnerUi.preferences(host).contains("campus") || preferredCampus(host, campus) != campus) {
                    rememberCampus(host, campus);
                }
                picker.setText(position == 0 ? "西校区" : "东校区", false);
                menu.dismiss();
        });
        return picker;
    }

    public void toTop() {
        // ScrollView's platform scroller supplies the non-linear easing.
        scroll.smoothScrollTo(0, 0);
    }

    public void resetToTop() {
        scroll.scrollTo(0, 0);
    }

    public boolean scrolled() {
        return scroll.getScrollY() > 0;
    }

    public void updateLocalLabels() {
        campus.setText(preferredCampus(host, 2) == 1 ? "西校区" : "东校区", false);
        libraryCount.setText(RouteLibraryActivity.count(host) + " 条预设 · 管理路线");
        renderLocalStats();
    }

    public void resumeLocation() { deviceLocation.resume(); }
    public void pauseLocation() { deviceLocation.pause(); }
    public void closeLocation() { deviceLocation.close(); }

    public void renderStats(boolean updated) {
        if (updated) refreshedAt = System.currentTimeMillis();
        boolean kilometers = kilometersMode();
        double done = kilometers ? Math.max(0, com.dudu.run.b.c().j()) : runDates.size();
        double target = kilometers ? targetKilometers() : targetDays();
        boolean available = kilometers || daysComplete;
        String completed = "—";
        if (available) completed = kilometers ? formatKilometers(done) : String.valueOf(runDates.size());
        if (target > 0) {
            String goal = kilometers ? formatKilometers(target) : String.valueOf(targetDays());
            android.text.SpannableString ratio = new android.text.SpannableString(completed + "/" + goal);
            ratio.setSpan(new android.text.style.RelativeSizeSpan(15f / 27f), completed.length(), ratio.length(), 0);
            ratio.setSpan(new android.text.style.ForegroundColorSpan(ui.muted), completed.length(), ratio.length(), 0);
            progress.setText(ratio);
        } else {
            progress.setText(completed);
        }
        progressUnit.setText(kilometers ? "累计公里数 · km" : "累计跑步天数 · 天");
        progress.setContentDescription(progressDescription());
        ring.fraction = available && target > 0 ? (float) Math.min(1, done / target) : 0;
        ring.invalidate();
        mileage.setText(String.format(Locale.CHINA, "%.2f km", com.dudu.run.b.c().j()));
        renderLocalStats();
    }

    private void renderLocalStats() {
        LocalRunStats.Totals totals=LocalRunStats.read(host);
        stepsTotal.setText(totals.hasSteps?String.format(Locale.CHINA,"%,d 步",totals.steps):"—");
        caloriesTotal.setText(totals.hasRuns?String.format(Locale.CHINA,"%.1f 千卡",totals.calories):"—");
        long updated=Math.max(refreshedAt,totals.updatedAt);
        if(updated>0)updateTime.setText("更新于 "+new java.text.SimpleDateFormat("HH:mm",Locale.CHINA).format(new java.util.Date(updated)));
    }

    private String progressDescription() {
        if (kilometersMode()) return "累计 " + formatKilometers(com.dudu.run.b.c().j()) + " 公里，目标 "
                + formatKilometers(targetKilometers()) + " 公里";
        return (daysComplete ? "已跑 " + runDates.size() + " 天" : "跑步天数暂未统计完成") + "，目标 " + targetDays() + " 天";
    }

    private boolean kilometersMode() {
        return "kilometers".equals(RunnerUi.preferences(host).getString("progress_display", "days"));
    }

    private int targetDays() {
        int previous = com.dudu.run.b.c().semesterRunGoal();
        return RunnerUi.preferences(host).getInt("progress_days", previous > 0 ? previous : 30);
    }

    private double targetKilometers() {
        return RunnerUi.preferences(host).getFloat("progress_kilometers", 60);
    }

    private static String formatKilometers(double value) {
        return java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    private EditText goalInput(LinearLayout body, String label, String value, boolean decimal) {
        body.addView(ui.text(label, 14, ui.ink, false));
        ui.gap(body, 5);
        EditText input = new com.google.android.material.textfield.TextInputEditText(new NativeMaterial(host).editContext());
        input.setText(value);
        input.setSingleLine();
        input.setTextColor(ui.ink);
        input.setTextSize(16);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | (decimal ? android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));
        input.setBackground(ui.outlined(ui.page, 12));
        input.setPadding(ui.dp(12), 0, ui.dp(12), 0);
        body.addView(input, new LinearLayout.LayoutParams(-1, ui.dp(44)));
        ui.gap(body, 12);
        return input;
    }

    private void editProgressGoals() {
        ui.showSheet("达标进度", "设置目标，并选择主页进度环显示的项目。", (body, dialog) -> {
            EditText kilometers = goalInput(body, "目标公里数 · km", formatKilometers(targetKilometers()), true);
            EditText days = goalInput(body, "目标天数 · 天", String.valueOf(targetDays()), false);
            body.addView(ui.text("主页显示", 14, ui.ink, true));
            NativeMaterial material = new NativeMaterial(host);
            MaterialButtonToggleGroup display = material.segmentedButtons();
            MaterialButton showDays = material.segment("天数");
            MaterialButton showKilometers = material.segment("公里数");
            display.addView(showDays, new LinearLayout.LayoutParams(0, -2, 1));
            display.addView(showKilometers, new LinearLayout.LayoutParams(0, -2, 1));
            (kilometersMode() ? showKilometers : showDays).setChecked(true);
            body.addView(display);
            ui.gap(body, 12);
            body.addView(ui.action("保存", true, () -> {
                float distance;
                int numberOfDays;
                try {
                    distance = Float.parseFloat(kilometers.getText().toString().trim());
                    if (Float.isNaN(distance) || Float.isInfinite(distance) || distance <= 0) throw new NumberFormatException();
                } catch (NumberFormatException invalid) {
                    kilometers.setError("请输入大于 0 的公里数");
                    return;
                }
                try {
                    numberOfDays = Integer.parseInt(days.getText().toString().trim());
                    if (numberOfDays <= 0) throw new NumberFormatException();
                } catch (NumberFormatException invalid) {
                    days.setError("请输入大于 0 的整数天数");
                    return;
                }
                RunnerUi.preferences(host).edit().putFloat("progress_kilometers", distance)
                        .putInt("progress_days", numberOfDays)
                        .putString("progress_display", display.getCheckedButtonId() == showDays.getId()
                                ? "days" : "kilometers").apply();
                renderStats(false);
                dialog.dismiss();
            }), new LinearLayout.LayoutParams(-1, ui.dp(48)));
        });
    }

    private void collectRunDays(int page, List<RecordItem> items, long version) {
        if (host.isDestroyed()||recentVersion!=version) return;
        int before = countedRecords.size();
        if (items != null) {
            for (RecordItem item : items) {
                if (!countedRecords.add(item.b())) continue;
                String date = item.a();
                if (date != null && date.length() >= 10) runDates.add(date.substring(0, 10));
            }
        }
        if (items == null || items.size() < 20) {
            daysComplete = true;
            renderStats(false);
            return;
        }
        if (countedRecords.size() == before || page >= 500) return;
        new com.dudu.run.e.s.f(new com.dudu.run.e.l() {
            @Override public void N(int next, List<RecordItem> rows) { collectRunDays(next, rows, version); }
            @Override public void t(String message) {}
            @Override public void v(int ignored) {}
        }).a(page + 1, 20);
    }

    public void loadRecent() {
        long version=RunResultSession.recordsVersion();
        if(recentVersion!=version){recentVersion=version;askedForRecent=false;}
        if (askedForRecent) return;
        askedForRecent = true;
        new com.dudu.run.e.s.f(new com.dudu.run.e.l() {
            @Override public void N(int page, List<RecordItem> items) {
                if (host.isDestroyed()||recentVersion!=version) return;
                runDates.clear();
                countedRecords.clear();
                daysComplete = false;
                collectRunDays(page, items, version);
                recentRoute.setTrack(java.util.Collections.emptyList());
                if (items == null || items.isEmpty()) {
                    lastDistance.setText("—");
                    lastDate.setVisibility(View.GONE);
                    return;
                }
                RecordItem recent = items.get(0);
                lastDistance.setText(o.b(recent.c()) + " km");
                String date = recent.a();
                if (date != null && date.length() >= 10 && date.substring(0, 10).matches("\\d{4}-\\d{2}-\\d{2}")) {
                    lastDate.setText(date.substring(0, 10));
                    lastDate.setVisibility(View.VISIBLE);
                } else {
                    lastDate.setVisibility(View.GONE);
                }
                new com.dudu.run.e.s.e(new com.dudu.run.e.j() {
                    @Override public void A(RecordDetailResponseData details) {
                        if (host.isDestroyed() || recentVersion!=version || details == null) return;
                        RunnerWork.IO.execute(()->{
                            try {
                                List<LatLngInfo> track = RecordDetailActivity.parseTrack(details.b());
                                host.runOnUiThread(()->{if(!host.isDestroyed()&&recentVersion==version)recentRoute.setTrack(track);});
                            } catch (RuntimeException invalid) {
                                host.runOnUiThread(()->{if(!host.isDestroyed()&&recentVersion==version)recentRoute.setContentDescription("暂无轨迹预览");});
                            }
                        });
                    }
                    @Override public void q(String message) {
                        if(!host.isDestroyed()&&recentVersion==version)recentRoute.setContentDescription("暂无轨迹预览");
                    }
                    @Override public void x() {
                    }
                }).a(recent.b());
            }
            @Override public void t(String message) {
                if(recentVersion!=version||host.isDestroyed())return;
                askedForRecent = false;
                lastDistance.setText("—");
                lastDate.setVisibility(View.GONE);
            }
            @Override public void v(int page) {
            }
        }).a(1, 20);
    }

    private SquareTile tile(int index, Runnable action) {
        SquareTile card = new SquareTile();
        card.setPadding(ui.dp(12), ui.dp(12), ui.dp(12), ui.dp(12));
        LinearLayout top = new LinearLayout(host);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(ui.text(CARD_NAMES[index], 13, ui.ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        ImageView arrow = ui.icon(R.drawable.ic_demo_chevron_right, ui.ink, 26);
        arrow.setPadding(ui.dp(5), ui.dp(5), ui.dp(5), ui.dp(5));
        int arrowBackground = ui.dark ? ui.selectedTone : 0xffeeeeee;
        arrow.setBackground(ui.background(arrowBackground, 13));
        top.addView(arrow, new LinearLayout.LayoutParams(ui.dp(26), ui.dp(26)));
        card.addView(top);
        card.surface.setFocusable(true);
        card.surface.setOnClickListener(view -> {
            if (action != null) action.run();
            else ui.showSheet("累计运动", "里程："+mileage.getText()+"\n本机记录步数："+stepsTotal.getText()+"\n本机累计热量："+caloriesTotal.getText()
                    +"\n\n步数和热量仅累计本机保存的运动，不含离线测试。历史缺失步数不推算；热量与单次记录相同，按 60 kg 估算。", "知道了", null);
        });
        cards[index] = card;
        return card;
    }

    private void renderCards() {
        for (SquareTile card : cards) {
            if (card.surface.getParent() != null) ((ViewGroup) card.surface.getParent()).removeView(card.surface);
        }
        grid.removeAllViews();
        for (int row = 0; row < 2; row++) {
            if (row > 0) ui.gap(grid, 10);
            LinearLayout line = new LinearLayout(host);
            for (int col = 0; col < 2; col++) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
                if (col == 1) params.setMarginStart(ui.dp(10));
                line.addView(cards[order.get(row * 2 + col)].surface, params);
            }
            grid.addView(line);
        }
    }

    private void chooseOrder() {
        ui.showSheet("重新排序", "调整卡片顺序，从左到右、从上到下排列。", (body, dialog) -> {
            LinearLayout rows = ui.column();
            renderOrderRows(rows);
            body.addView(rows);
            ui.gap(body, 12);
            body.addView(ui.action("完成", true, dialog::dismiss), new LinearLayout.LayoutParams(-1, ui.dp(48)));
        });
    }

    private void renderOrderRows(LinearLayout rows) {
        rows.removeAllViews();
        for (int i = 0; i < order.size(); i++) {
            int position = i;
            LinearLayout row = new LinearLayout(host);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(ui.background(ui.page, 18));
            row.setPadding(ui.dp(12), ui.dp(2), ui.dp(4), ui.dp(2));
            row.addView(ui.text(CARD_NAMES[order.get(i)], 14, ui.ink, false),
                    new LinearLayout.LayoutParams(0, -2, 1));
            for (int shift : new int[]{-1, 1}) {
                TextView arrow = ui.action(shift < 0 ? "↑" : "↓", false, () -> {
                    java.util.Collections.swap(order, position, position + shift);
                    StringBuilder saved = new StringBuilder();
                    for (int index : order) saved.append(index);
                    RunnerUi.preferences(host).edit().putString("card_order", saved.toString()).apply();
                    renderCards();
                    renderOrderRows(rows);
                });
                arrow.setContentDescription((shift < 0 ? "上移" : "下移") + CARD_NAMES[order.get(i)]);
                arrow.setEnabled(position + shift >= 0 && position + shift < order.size());
                arrow.setAlpha(arrow.isEnabled() ? 1 : .35f);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ui.dp(44), ui.dp(44));
                params.setMarginStart(ui.dp(4));
                row.addView(arrow, params);
            }
            rows.addView(row);
            if (i < order.size() - 1) ui.gap(rows, 4);
        }
    }

    private void open(Class<?> target) {
        NativeNavigation.launch(host, new Intent(host, target));
    }

    private static String greeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String period = hour < 5 || hour >= 18 ? "晚上好" : hour < 11 ? "早上好" : hour < 14 ? "中午好" : "下午好";
        String name = com.dudu.run.b.c().o();
        return name == null || name.isEmpty() ? period : period + "，" + name;
    }

    private final class SquareTile extends LinearLayout {
        final SpringCardView surface;
        SquareTile() {
            super(host);
            setOrientation(VERTICAL);
            surface = new SpringCardView(new NativeMaterial(host).context());
            surface.setRadius(ui.dp(22));
            surface.setCardBackgroundColor(ui.surface);
            surface.setStrokeColor(ui.outline);
            surface.setStrokeWidth(ui.dp(1));
            surface.setCardElevation(0);
            surface.setPreventCornerOverlap(false);
            surface.setUseCompatPadding(false);
            surface.setContentPadding(0, 0, 0, 0);
            surface.setCheckable(false);
            surface.setClickable(true);
            surface.addView(this, new FrameLayout.LayoutParams(-1, -2));
        }
        @Override protected void onMeasure(int width, int height) {
            int size = MeasureSpec.getSize(width);
            super.onMeasure(width, size > 0 ? MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY) : height);
        }
    }

    private final class Ring extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float fraction;
        Ring() { super(host); }
        @Override protected void onDraw(Canvas canvas) {
            float size = Math.min(ui.dp(144), Math.min(getWidth(), getHeight()));
            float radius = size * 30f / 72f;
            float x = getWidth() / 2f, y = getHeight() / 2f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(size * 6f / 72f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(ui.tone);
            canvas.drawCircle(x, y, radius, paint);
            if (fraction > 0) {
                paint.setColor(ui.accent);
                canvas.drawArc(x - radius, y - radius, x + radius, y + radius, -90, fraction * 360, false, paint);
            }
        }
    }
}
