package com.dudu.run.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.textfield.TextInputEditText;

public final class MaterialEditTextView extends TextInputEditText {
    public MaterialEditTextView(Context context, AttributeSet attrs) {
        super(new NativeMaterial(context).editContext(), attrs);
    }
}
