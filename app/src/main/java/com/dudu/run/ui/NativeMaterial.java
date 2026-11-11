package com.dudu.run.ui;

import androidx.appcompat.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.dudu.run.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Material Views styled with the current runner palette. */
public final class NativeMaterial {
    private static final int BOX_OUTLINE = 2;
    private final RunnerUi ui;
    private final Context themed;

    public NativeMaterial(Context context) {
        ui = new RunnerUi(context);
        Context base = new ContextThemeWrapper(context, ui.dark ? R.style.RunnerM3Dark : R.style.RunnerM3Light);
        DynamicColorsOptions.Builder colors = new DynamicColorsOptions.Builder();
        boolean dynamic = RunnerUi.preferences(context).getBoolean("dynamic", false);
        int colorOverlay = dynamic ? (ui.dark ? R.style.ThemeOverlay_Material3_DynamicColors_Dark
                : R.style.ThemeOverlay_Material3_DynamicColors_Light) : R.style.ThemeOverlay_Material3_PersonalizedColors;
        colors.setThemeOverlay(colorOverlay);
        if (!dynamic) {
            colors.setContentBasedSource(RunnerUi.preferences(context).getInt("seed", 0xff6750a4));
        }
        Context colorsContext = Build.VERSION.SDK_INT >= 31 ? DynamicColors.wrapContextIfAvailable(base, colors.build()) : base;
        // Rebuild the complete widget theme against the final Resources object.
        // Copying a recovered theme across the color ResourcesLoader can leave
        // AppCompat drawable attributes unresolved on current Android versions.
        android.content.res.Resources.Theme complete = colorsContext.getResources().newTheme();
        complete.applyStyle(ui.dark ? R.style.RunnerM3Dark : R.style.RunnerM3Light, true);
        if (colorsContext != base) complete.applyStyle(colorOverlay, true);
        themed = new MaterialWidgetContext(new ContextThemeWrapper(colorsContext, complete));
    }

    public Context context() {
        return themed;
    }

    public Context editContext() {
        return new ContextThemeWrapper(themed, R.style.ThemeOverlay_Material3_TextInputEditText_OutlinedBox);
    }

    public Context dropdownContext() {
        return new ContextThemeWrapper(themed, R.style.ThemeOverlay_Material3_AutoCompleteTextView_OutlinedBox);
    }

    public MaterialAlertDialogBuilder dialog() {
        return new MaterialAlertDialogBuilder(themed);
    }

    public BottomSheetDialog sheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(themed);
        dialog.setDismissWithAnimation(true);
        Window window = dialog.getWindow();
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 28) window.setNavigationBarDividerColor(android.graphics.Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 29) window.setNavigationBarContrastEnforced(false);
        return dialog;
    }

    public void showStartupNotice(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (Build.VERSION.SDK_INT >= 29) window.getDecorView().setForceDarkAllowed(false);
        dialog.show();
    }

    public LinearLayout dialogBody() {
        LinearLayout body = ui.column();
        body.setPadding(ui.dp(24), ui.dp(8), ui.dp(24), ui.dp(12));
        body.setFocusableInTouchMode(true);
        return body;
    }

    public LinearLayout settingsPage(Activity activity, String title) {
        LinearLayout column = ui.column();
        column.setBackgroundColor(ui.page);
        MaterialToolbar toolbar = new MaterialToolbar(themed, null);
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(ui.ink);
        toolbar.setNavigationIcon(R.drawable.ic_demo_arrow_left);
        toolbar.setNavigationContentDescription("返回");
        toolbar.setNavigationOnClickListener(view -> activity.finish());
        toolbar.setNavigationIconTint(ui.accentText);
        column.addView(toolbar, new LinearLayout.LayoutParams(-1, ui.dp(56)));
        ScrollView scroll = new ScrollView(activity);
        LinearLayout body = ui.column();
        body.setPadding(ui.dp(16), ui.dp(12), ui.dp(16), ui.dp(86));
        scroll.addView(body);
        column.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        FrameLayout root = new FrameLayout(activity);
        root.addView(column, new FrameLayout.LayoutParams(-1, -1));
        ui.addDock(activity, root, R.id.layout_tab_mine);
        activity.setContentView(root);
        ui.applySystemBars(activity);
        return body;
    }

    public EditText field(LinearLayout body, String label, int inputType, boolean multiline) {
        TextInputLayout field = new TextInputLayout(themed, null);
        field.setBoxBackgroundMode(BOX_OUTLINE);
        field.setBoxStrokeColor(ui.accent);
        field.setHintTextColor(ColorStateList.valueOf(ui.accentText));
        field.setHint(label);
        TextInputEditText text = new TextInputEditText(editContext(), null);
        text.setBackground(null);
        text.setSingleLine(!multiline);
        text.setInputType(inputType);
        text.setTextColor(ui.ink);
        if (multiline) {
            text.setGravity(Gravity.TOP | Gravity.START);
            text.setMinLines(4);
        }
        field.addView(text, new LinearLayout.LayoutParams(-1, -2));
        body.addView(field, new LinearLayout.LayoutParams(-1, -2));
        return text;
    }

    public MaterialButton button(String title, Runnable action) {
        MaterialButton button = new MotionMaterialButton(themed, null);
        button.setText(title);
        button.setAllCaps(false);
        button.setMinHeight(ui.dp(48));
        button.setBackgroundTintList(ColorStateList.valueOf(ui.accent));
        button.setTextColor(ui.onAccent);
        if (action != null) button.setOnClickListener(view -> action.run());
        return button;
    }

    public MaterialButton iconButton(int icon, String label, Runnable action) {
        Context style = new ContextThemeWrapper(themed, R.style.RunnerM3IconTheme);
        MaterialButton button = new MotionMaterialButton(style, null);
        button.setText("");
        button.setIconResource(icon);
        button.setIconSize(ui.dp(19));
        button.setIconTint(ColorStateList.valueOf(ui.accentText));
        button.setIconPadding(0);
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setInsetLeft(0);
        button.setInsetRight(0);
        button.setPadding(0, 0, 0, 0);
        button.setContentDescription(label);
        button.setOnClickListener(view -> action.run());
        return button;
    }

    public MaterialButton tonalButton(String title, Runnable action) {
        MaterialButton button = new MotionMaterialButton(new ContextThemeWrapper(themed, R.style.RunnerM3TonalButtonTheme), null);
        button.setText(title);
        button.setAllCaps(false);
        button.setMinHeight(ui.dp(48));
        button.setBackgroundTintList(ColorStateList.valueOf(ui.tone));
        button.setTextColor(ui.accentText);
        if (action != null) button.setOnClickListener(view -> action.run());
        return button;
    }

    public MaterialButton primaryAction(LinearLayout body, String title) {
        MaterialButton button = button(title, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, ui.dp(48));
        params.topMargin = ui.dp(12);
        body.addView(button, params);
        return button;
    }

    public MaterialButtonToggleGroup segmentedButtons() {
        MaterialButtonToggleGroup group = new MaterialButtonToggleGroup(themed, null);
        group.setOrientation(LinearLayout.HORIZONTAL);
        group.setSingleSelection(true);
        group.setSelectionRequired(true);
        return group;
    }

    public MaterialButton segment(String title) {
        MaterialButton button = button(title, null);
        button.setId(android.view.View.generateViewId());
        int[][] states = {{android.R.attr.state_checked}, {}};
        button.setBackgroundTintList(new ColorStateList(states, new int[]{ui.selectedTone, ui.surface}));
        button.setTextColor(new ColorStateList(states, new int[]{ui.accentText, ui.ink}));
        button.setStrokeWidth(ui.dp(1));
        button.setStrokeColor(ColorStateList.valueOf(ui.outline));
        return button;
    }

    public MaterialSwitch toggle() {
        return new MaterialSwitch(themed);
    }
}
