package com.dudu.run.ui;

import android.content.Context;
import android.util.AttributeSet;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.button.MaterialButton;

/** Keeps Material's own corner spring bound when its inset setters rebuild the background. */
public class MotionMaterialButton extends MaterialButton {
    private boolean motionReady;

    public MotionMaterialButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        motionReady = true;
        restoreCornerMotion();
    }

    private void restoreCornerMotion() {
        // Material's constructor may dispatch setters before its helper exists.
        if (!motionReady) return;
        SpringForce spring = getCornerSpringForce();
        if (spring != null) setCornerSpringForce(spring);
    }

    @Override public void setInsetLeft(int inset) {
        super.setInsetLeft(inset);
        restoreCornerMotion();
    }

    @Override public void setInsetTop(int inset) {
        super.setInsetTop(inset);
        restoreCornerMotion();
    }

    @Override public void setInsetRight(int inset) {
        super.setInsetRight(inset);
        restoreCornerMotion();
    }

    @Override public void setInsetBottom(int inset) {
        super.setInsetBottom(inset);
        restoreCornerMotion();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        restoreCornerMotion();
    }
}
