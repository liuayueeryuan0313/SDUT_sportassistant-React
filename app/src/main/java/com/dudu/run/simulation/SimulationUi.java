package com.dudu.run.simulation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.view.Gravity;
import android.widget.*;
import com.dudu.run.ui.NativeMaterial;
import com.google.android.material.button.MaterialButton;

final class SimulationUi {
    static final int NAVY=Color.rgb(19,42,66),TEAL=Color.rgb(0,165,148),INK=Color.rgb(31,49,68);
    static final int MUTED=Color.rgb(108,126,144),LINE=Color.rgb(225,233,240),PALE=Color.rgb(243,247,250),RED=Color.rgb(213,76,88);
    static int dp(Context c,float value) { return (int)(c.getResources().getDisplayMetrics().density*value+.5f); }
    static GradientDrawable shape(Context c,int color,float radius,int border) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c,radius));
        if(border!=0) d.setStroke(dp(c,1),border); return d;
    }
    static TextView text(Context c,String value,int size,int color,boolean bold) {
        TextView v=c instanceof SimulationActivity ? new com.google.android.material.textview.MaterialTextView(((SimulationActivity)c).material.context()) : new TextView(c);
        v.setText(value); v.setTextSize(size); v.setTextColor(textColor(c,color));
        if(bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v;
    }
    static TextView button(Context c,String value,int fill,int color,Runnable action) {
        MaterialButton v=(c instanceof SimulationActivity ? ((SimulationActivity)c).material : new NativeMaterial(c)).button(value,action); v.setTextSize(13); v.setTextColor(color);
        v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setAllCaps(false); v.setIncludeFontPadding(false);
        v.setMinWidth(0); v.setMinimumWidth(0); v.setMinHeight(0); v.setGravity(Gravity.CENTER);
        v.setSingleLine(true); v.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8)); v.setMinHeight(dp(c,44));
        if (c instanceof SimulationActivity) {
            com.dudu.run.ui.RunnerUi ui=((SimulationActivity)c).ui;
            boolean primary=fill==NAVY || fill==TEAL;
            boolean destructive=color==RED;
            v.setBackgroundTintList(ColorStateList.valueOf(primary ? ui.accent : destructive ? ui.tone : ui.selectedTone));
            v.setTextColor(primary ? ui.onAccent : destructive ? errorColor(c) : ui.accentText);
        } else v.setBackgroundTintList(ColorStateList.valueOf(fill));
        return v;
    }
    static MaterialButton iconButton(Context c,int icon,String label,int tint,Runnable action) {
        MaterialButton v=new NativeMaterial(c).iconButton(icon,label,action);v.setIconTint(ColorStateList.valueOf(tint));
        v.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8));
        v.setContentDescription(label);if(android.os.Build.VERSION.SDK_INT>=26)v.setTooltipText(label);
        v.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        v.setOnClickListener(view->action.run());return v;
    }
    static int textColor(Context c,int legacy) {
        if (!(c instanceof SimulationActivity)) return legacy;
        com.dudu.run.ui.RunnerUi ui=((SimulationActivity)c).ui;
        if(legacy==NAVY || legacy==INK) return ui.ink;
        if(legacy==MUTED) return ui.muted;
        if(legacy==TEAL) return ui.accentText;
        if(legacy==RED) return errorColor(c);
        return legacy;
    }
    static int errorColor(Context c) {
        return com.google.android.material.color.MaterialColors.getColor(new NativeMaterial(c).context(),com.dudu.run.R.attr.colorError,RED);
    }
    static void styleSheet(Context c,android.app.Dialog dialog) {
        android.view.View sheet=dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if(sheet!=null) {
            sheet.setBackgroundTintList(ColorStateList.valueOf(new com.dudu.run.ui.RunnerUi(c).surface));
            sheet.setClipToOutline(true);
        }
    }
    static LinearLayout column(Context c) { LinearLayout v=new LinearLayout(c); v.setOrientation(LinearLayout.VERTICAL); return v; }
    static LinearLayout row(Context c) { LinearLayout v=new LinearLayout(c); v.setOrientation(LinearLayout.HORIZONTAL); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    static void gap(LinearLayout parent,int height) { android.view.View v=new android.view.View(parent.getContext()); parent.addView(v,new LinearLayout.LayoutParams(1,dp(parent.getContext(),height))); }
    static void weighted(LinearLayout row,android.view.View child,int height) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(row.getContext(),height),1); p.setMargins(dp(row.getContext(),3),0,dp(row.getContext(),3),0); row.addView(child,p);
    }
}
