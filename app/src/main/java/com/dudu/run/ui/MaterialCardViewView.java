package com.dudu.run.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.card.MaterialCardView;

/** Official Material card, supplied with the app's current M3 palette. */
public final class MaterialCardViewView extends MaterialCardView {
    public MaterialCardViewView(Context context, AttributeSet attrs) {
        super(new NativeMaterial(context).context(), attrs);
    }
}
