package com.dudu.run.ui;

import android.content.Context;
import com.google.android.material.internal.ClippableRoundedCornerLayout;

/** Material clips in local coordinates, including inset/offset content roots. */
public final class MotionSurface extends ClippableRoundedCornerLayout {
    public MotionSurface(Context context) { super(context); }
    @Override public void updateCornerRadii(float[] radii) {
        updateClipBoundsAndCornerRadii(0, 0, getWidth(), getHeight(), radii);
    }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateCornerRadii(getCornerRadii());
    }
}
