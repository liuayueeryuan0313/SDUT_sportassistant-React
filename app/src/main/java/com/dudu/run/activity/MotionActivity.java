package com.dudu.run.activity;

/** Platform Activity with content-only Material page motion. */
public class MotionActivity extends android.app.Activity {
    @Override public void setContentView(android.view.View view) {
        super.setContentView(view);
        // Theme changes and route-library edits rebuild these pages in place.
        // Rebind the content surface so predictive back never targets an old tree.
        com.dudu.run.ui.PageMotion.install(this);
    }
    @Override public void finish() {
        if (!com.dudu.run.ui.PageMotion.finish(this, () -> super.finish())) super.finish();
    }
}
