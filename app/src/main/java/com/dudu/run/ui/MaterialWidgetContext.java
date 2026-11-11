package com.dudu.run.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Prevents the recovered Activity's legacy Factory2 from inflating modern dialog
 * controls. Modern AppCompat installs its own official factory in this inflater.
 * This does not change the Activity theme or any application layout parameters.
 */
final class MaterialWidgetContext extends ContextWrapper {
    private LayoutInflater inflater;
    MaterialWidgetContext(Context base) { super(base); }
    @Override public Object getSystemService(String name) {
        if (!LAYOUT_INFLATER_SERVICE.equals(name)) return super.getSystemService(name);
        if (inflater == null) inflater = new WidgetInflater(this);
        return inflater;
    }
    private static final class WidgetInflater extends LayoutInflater {
        private final com.google.android.material.theme.MaterialComponentsViewInflater widgets =
                new com.google.android.material.theme.MaterialComponentsViewInflater();
        WidgetInflater(Context context) { super(context); }
        WidgetInflater(LayoutInflater original, Context context) { super(original, context); }
        @Override public LayoutInflater cloneInContext(Context context) {
            return new WidgetInflater(this, context);
        }
        @Override protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
            // Dialog list adapters can inflate through the builder context before
            // AppCompatDialog installs its Factory2. Use the same official widget
            // inflater there as well, including drawableStartCompat on choices.
            View widget = widgets.createView(null, name, getContext(), attrs, false, true, true, true);
            if (widget != null) return widget;
            for (String prefix : new String[]{"android.widget.", "android.view.", "android.webkit."}) {
                try { return createView(name, prefix, attrs); }
                catch (ClassNotFoundException ignored) { }
            }
            return super.onCreateView(name, attrs);
        }
    }
}
