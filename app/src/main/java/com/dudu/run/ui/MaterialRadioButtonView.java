package com.dudu.run.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.radiobutton.MaterialRadioButton;

public final class MaterialRadioButtonView extends MaterialRadioButton {
    public MaterialRadioButtonView(Context context, AttributeSet attrs) {
        super(new NativeMaterial(context).context(), attrs);
    }
}
