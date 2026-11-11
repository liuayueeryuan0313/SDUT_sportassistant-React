package com.dudu.run.ui;

import android.content.Context;
import android.util.AttributeSet;

/** Supplies the M3 widget theme without changing the legacy page's layout theme. */
public final class MaterialButtonView extends MotionMaterialButton {
    public MaterialButtonView(Context context, AttributeSet attrs) {
        super(new NativeMaterial(context).context(), attrs);
        int id = getId();
        if (id == com.dudu.run.R.id.runner_records_avatar || id == com.dudu.run.R.id.ib_username_clear
                || id == com.dudu.run.R.id.ib_psd_visible) {
            setInsetLeft(0); setInsetRight(0); setInsetTop(0); setInsetBottom(0);
            setMinWidth(0); setMinimumWidth(0); setMinHeight(0); setMinimumHeight(0);
        }
    }
}
