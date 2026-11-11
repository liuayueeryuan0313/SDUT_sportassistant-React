package com.dudu.run.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.view.ViewGroup;
import com.dudu.run.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class RunnerUi {
    public final Context context;
    public final boolean dark;
    public final int page, surface, ink, muted, accent, accentText, onAccent, tone, selectedTone, outline, map;
    private final int resolvedSeed;

    public RunnerUi(Context context) {
        this.context = context;
        String mode = preferences(context).getString("appearance", "system");
        boolean systemDark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        dark = "dark".equals(mode) || ("system".equals(mode) && systemDark);
        page = dark ? 0xff212027 : 0xfff5f7f1;
        surface = dark ? 0xff2d2e36 : 0xfffdfef9;
        ink = dark ? 0xffe2ebdf : 0xff20291e;
        muted = dark ? 0xffb9c8b7 : 0xff505e50;
        outline = dark ? 0xff3c4a3e : 0xffdce4d8;
        map = dark ? 0xff333e36 : 0xffe6eddf;
        int seed = preferences(context).getInt("seed", 0xff6750a4);
        if (preferences(context).getBoolean("dynamic", false) && Build.VERSION.SDK_INT >= 31) {
            seed = context.getColor(android.R.color.system_accent1_500);
        }
        resolvedSeed = seed;
        boolean defaultSeed = seed == 0xff6750a4;
        accent = dark ? (defaultSeed ? 0xffbeb9d6 : mix(seed, 0xffefedf5, .66f)) : seed;
        accentText = dark ? accent : mix(0xff16131b, seed, .68f);
        tone = dark ? (defaultSeed ? 0xff373549 : mix(page, accent, .14f)) : mix(0xfffaf9fc, seed, .12f);
        selectedTone = mix(tone, accent, .15f);
        double luminance = .2126 * linear(Color.red(accent)) + .7152 * linear(Color.green(accent))
                + .0722 * linear(Color.blue(accent));
        onAccent = dark ? mix(0xff111111, seed, .30f) : luminance > .179 ? Color.BLACK : Color.WHITE;
    }

    public boolean hasSamePalette(RunnerUi other) {
        return dark == other.dark && resolvedSeed == other.resolvedSeed;
    }

    public static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("runner_ui", Context.MODE_PRIVATE);
    }

    public static String version(Context context) {
        return com.dudu.run.utils.b.b(context);
    }

    public static String recordDate(String value) {
        if (value == null || value.isEmpty()) return "时间未知";
        if (value.length() < 10) return value;
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        date.setLenient(false);
        try {
            Calendar recorded = Calendar.getInstance();
            recorded.setTime(date.parse(value.substring(0, 10)));
            Calendar today = Calendar.getInstance();
            if (sameDay(recorded, today)) return "今天";
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (sameDay(recorded, today)) return "昨天";
            return (recorded.get(Calendar.MONTH) + 1) + " 月 " + recorded.get(Calendar.DAY_OF_MONTH) + " 日";
        } catch (ParseException unrecognized) {
            return value;
        }
    }

    private static boolean sameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    public static String duration(int seconds) {
        int safe = Math.max(0, seconds);
        String hours = safe >= 3600 ? safe / 3600 + " 小时 " : "";
        return hours + (safe / 60 % 60) + " 分 " + (safe % 60) + " 秒";
    }

    private static double linear(int channel) {
        double value = channel / 255.0;
        return value <= .04045 ? value / 12.92 : Math.pow((value + .055) / 1.055, 2.4);
    }

    private static int mix(int first, int second, float ratio) {
        return Color.rgb(
                Math.round(Color.red(first) + (Color.red(second) - Color.red(first)) * ratio),
                Math.round(Color.green(first) + (Color.green(second) - Color.green(first)) * ratio),
                Math.round(Color.blue(first) + (Color.blue(second) - Color.blue(first)) * ratio));
    }

    public int dp(float size) {
        return Math.round(size * context.getResources().getDisplayMetrics().density);
    }

    public GradientDrawable background(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    public GradientDrawable outlined(int color, int radius) {
        GradientDrawable shape = background(color, radius);
        shape.setStroke(dp(1), outline);
        return shape;
    }

    public RippleDrawable ripple(int color, int radius) {
        return new RippleDrawable(ColorStateList.valueOf(dark ? 0x28ffffff : 0x20000000),
                background(color, radius), background(Color.WHITE, radius));
    }

    public LinearLayout column() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public TextView text(String value, int size, int color, boolean bold) {
        TextView label = new TextView(context);
        label.setText(value);
        label.setTextSize(size);
        label.setTextColor(color);
        label.setIncludeFontPadding(false);
        if (bold) {
            label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            label.getPaint().setFakeBoldText(true);
        }
        return label;
    }

    public void gap(LinearLayout parent, int size) {
        parent.addView(new View(context), new LinearLayout.LayoutParams(1, dp(size)));
    }

    public LinearLayout group() {
        LinearLayout group = column();
        group.setBackground(background(surface, 22));
        group.setClipToOutline(true);
        return group;
    }

    public void append(LinearLayout group, View row) {
        if (group.getChildCount() != 0) {
            View gap = new View(context);
            gap.setBackgroundColor(page);
            group.addView(gap, new LinearLayout.LayoutParams(-1, dp(3)));
        }
        group.addView(row);
    }

    public View option(String title, String summary, int icon, Runnable click) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setMinimumHeight(dp(68));
        row.setBackground(ripple(surface, 0));
        if (icon != 0) {
            ImageView mark = icon(icon, accent, 39);
            mark.setBackground(background(tone, 15));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(39), dp(39));
            params.setMarginEnd(dp(11));
            row.addView(mark, params);
        }
        LinearLayout labels = column();
        labels.addView(text(title, 14, ink, true));
        if (summary != null && !summary.isEmpty()) {
            TextView hint = text(summary, 12, muted, false);
            hint.setPadding(0, dp(3), 0, 0);
            labels.addView(hint);
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(icon(R.drawable.ic_demo_chevron_right, ink, 19),
                new LinearLayout.LayoutParams(dp(19), dp(19)));
        row.setFocusable(true);
        row.setOnClickListener(view -> click.run());
        return row;
    }

    public TextView action(String title, boolean primary, Runnable click) {
        MaterialButton view = new NativeMaterial(context).button(title, click);
        view.setTextSize(15);
        view.setTextColor(primary ? onAccent : accent);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setIncludeFontPadding(false);
        view.setAllCaps(false);
        view.setMinHeight(0);
        view.setMinWidth(0);
        view.setGravity(Gravity.CENTER);
        view.setMinimumHeight(dp(44));
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        view.setBackgroundTintList(ColorStateList.valueOf(primary ? accent : tone));
        view.setFocusable(true);
        view.setOnClickListener(button -> click.run());
        return view;
    }

    public ImageView icon(int resource, int color, int size) {
        ImageView image = new ImageView(context);
        styleIcon(image, resource, color, size);
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return image;
    }

    private void styleIcon(ImageView image, int resource, int color, int size) {
        android.graphics.drawable.Drawable glyph = context.getDrawable(resource).mutate();
        glyph.setTint(color);
        int inset = Math.max(0, (size - 19) / 2);
        image.setImageDrawable(glyph);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setPadding(dp(inset), dp(inset), dp(inset), dp(inset));
    }

    public MaterialButton iconButton(int resource, String label, Runnable click) {
        MaterialButton button = new NativeMaterial(context).iconButton(resource, label, click);
        if (resource == R.drawable.ic_demo_user_round) {
            button.setIcon(new InitialAvatar(accentText));
            button.setIconTint(null);
            button.setIconSize(dp(44));
        }
        return button;
    }

    public TextView addHeader(LinearLayout pageLayout, String title, int icon, String label, Runnable click) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout labels = column();
        labels.addView(text("运动助手", 12, accentText, true));
        gap(labels, 5);
        TextView heading = text(title, 29, ink, true);
        heading.setLetterSpacing(-.035f);
        labels.addView(heading);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        if (icon != 0) row.addView(iconButton(icon, label, click), new LinearLayout.LayoutParams(dp(44), dp(44)));
        pageLayout.addView(row);
        gap(pageLayout, 19);
        return heading;
    }

    public void addSubheader(LinearLayout pageLayout, String title, Runnable back, Runnable add) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(iconButton(R.drawable.ic_demo_arrow_left, "返回", back),
                new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView label = text(title, 22, ink, true);
        LinearLayout.LayoutParams labelLayout = new LinearLayout.LayoutParams(0, -2, 1);
        labelLayout.setMarginStart(dp(8));
        row.addView(label, labelLayout);
        if (add != null) {
            row.addView(iconButton(R.drawable.ic_demo_plus, "新建预设", add),
                    new LinearLayout.LayoutParams(dp(44), dp(44)));
        }
        pageLayout.addView(row);
        gap(pageLayout, 19);
    }

    public interface SheetContent {
        void add(LinearLayout body, Dialog dialog);
    }

    public void showSheet(String title, String message, String action, Runnable onConfirm) {
        showSheet(title, message, (body, dialog) -> body.addView(action(action, true, () -> {
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        }), new LinearLayout.LayoutParams(-1, dp(48))));
    }

    public void showSheet(String title, String message, SheetContent content) {
        BottomSheetDialog dialog = new NativeMaterial(context).sheet();
        LinearLayout body = column();
        body.setPadding(dp(17), dp(10), dp(17), dp(21));
        GradientDrawable panel = background(surface, 0);
        int r = dp(26);
        panel.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        body.setBackground(panel);
        View handle = new View(context);
        handle.setBackground(background(outline, 6));
        LinearLayout.LayoutParams handleLayout = new LinearLayout.LayoutParams(dp(32), dp(4));
        handleLayout.gravity = Gravity.CENTER_HORIZONTAL;
        body.addView(handle, handleLayout);
        gap(body, 6);
        LinearLayout header = new LinearLayout(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text(title, 20, ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(iconButton(R.drawable.ic_demo_x, "关闭", dialog::dismiss),
                new LinearLayout.LayoutParams(dp(44), dp(44)));
        body.addView(header);
        if (message != null && !message.isEmpty()) {
            gap(body, 8);
            body.addView(text(message, 14, muted, false));
            gap(body, 12);
        }
        content.add(body, dialog);
        dialog.setContentView(body);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setGravity(Gravity.BOTTOM);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(.52f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        dialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        if (Build.VERSION.SDK_INT >= 26) {
            int flags = window.getDecorView().getSystemUiVisibility();
            flags = dark ? flags & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    : flags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    public void applySystemBars(Activity activity) {
        Window window = activity.getWindow();
        NativeInsets.apply(activity, page);
        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= 26) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if (!dark) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    public void styleDock(LinearLayout dock, int selectedId) {
        boolean changed = false;
        for (int i = 0; i < dock.getChildCount(); i++) {
            View tab = dock.getChildAt(i);
            if (tab.isSelected() != (tab.getId() == selectedId)) changed = true;
        }
        if (changed && dock.isLaidOut() && dock.getParent() instanceof ViewGroup) {
            ChangeBounds resize = new ChangeBounds();
            resize.addTarget(dock);
            resize.removeTarget(dock);
            resize.setDuration(140);
            resize.setInterpolator(new androidx.interpolator.view.animation.FastOutSlowInInterpolator());
            for (int i = 0; i < dock.getChildCount(); i++) resize.addTarget(dock.getChildAt(i));
            TransitionManager.beginDelayedTransition(dock, resize);
        }
        dock.setBackground(outlined(surface, 40));
        dock.setElevation(dp(5));
        for (int i = 0; i < dock.getChildCount(); i++) {
            MaterialButton tab = (MaterialButton) dock.getChildAt(i);
            boolean selected = tab.getId() == selectedId;
            int color = selected ? accentText : ink;
            tab.setSelected(selected);
            tab.setBackgroundTintList(ColorStateList.valueOf(selected ? selectedTone : surface));
            tab.setFocusable(true);
            tab.setIconTint(ColorStateList.valueOf(color));
            tab.setIconPadding(selected ? dp(6) : 0);
            String label = tab.getId() == R.id.layout_tab_sport ? "运动" : tab.getId() == R.id.layout_tab_rule ? "记录" : "我的";
            tab.setTextColor(color);
            tab.setText(selected ? label : "");
            tab.setContentDescription(label);
        }
    }

    public void addDock(Activity host, FrameLayout root, int selectedId) {
        LinearLayout dock = (LinearLayout) host.getLayoutInflater().inflate(R.layout.runner_dock, root, false);
        styleDock(dock, selectedId);
        dock.findViewById(R.id.layout_tab_sport).setOnClickListener(v -> openHome(host, 1));
        dock.findViewById(R.id.layout_tab_mine).setOnClickListener(v -> openHome(host, 2));
        dock.findViewById(R.id.layout_tab_rule).setOnClickListener(v -> {
            openHome(host, 3);
        });
        root.addView(dock);
    }

    public void openHome(Activity host, int tab) {
        host.startActivity(new Intent(host, com.dudu.run.activity.HomeActivity.class)
                .putExtra(com.dudu.run.activity.HomeActivity.OPEN_TAB, tab)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                android.app.ActivityOptions.makeCustomAnimation(host, 0, 0).toBundle());
        host.finish();
    }

    public void styleAvatar(MaterialButton avatar) {
        avatar.setPadding(0, 0, 0, 0);
        avatar.setIconTint(null);
        avatar.setIcon(new InitialAvatar(accentText));
        avatar.setIconSize(dp(44));
        avatar.setBackgroundTintList(ColorStateList.valueOf(tone));
        avatar.setContentDescription("打开我的");
    }

    public void styleAvatar(ImageView avatar) {
        avatar.setPadding(0, 0, 0, 0);
        avatar.clearColorFilter();
        avatar.setImageDrawable(new InitialAvatar(accentText));
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatar.setBackground(ripple(tone, 22));
        avatar.setContentDescription("打开我的");
    }
}
