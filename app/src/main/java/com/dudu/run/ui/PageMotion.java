package com.dudu.run.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import com.dudu.run.R;
import com.google.android.material.internal.ClippableRoundedCornerLayout;
import com.google.android.material.motion.MaterialMainContainerBackHelper;
import com.google.android.material.transition.MaterialSharedAxis;
import java.util.WeakHashMap;

/** Animates page content, while the dock remains in a separate stationary layer. */
public final class PageMotion {
    public static final String PAGE = "runner_motion_page";
    private static final WeakHashMap<Activity, State> states = new WeakHashMap<>();
    private PageMotion() {}

    public static boolean manages(Activity activity) {
        return activity.getIntent().getBooleanExtra(PAGE, false);
    }

    public static void install(Activity activity) {
        if (!manages(activity)) return;
        FrameLayout stage = activity.findViewById(android.R.id.content);
        State old = states.get(activity);
        if (old != null && old.surface.getParent() == stage) return;
        boolean entering = old == null;
        if (old != null) old.unregister();
        View dock = activity.findViewById(R.id.layout_tabs);
        if (dock != null && dock.getParent() instanceof ViewGroup) ((ViewGroup)dock.getParent()).removeView(dock);
        RunnerUi ui = new RunnerUi(activity);
        ClippableRoundedCornerLayout surface = new MotionSurface(new NativeMaterial(activity).context());
        surface.setId(R.id.runner_page_surface);
        surface.setTransitionGroup(true);
        surface.setBackgroundColor(ui.page);
        while (stage.getChildCount() > 0) {
            View child = stage.getChildAt(0); stage.removeViewAt(0);
            surface.addView(child, new FrameLayout.LayoutParams(-1, -1));
        }
        stage.addView(surface, new FrameLayout.LayoutParams(-1, -1));
        if (dock != null) stage.addView(dock);
        stage.setBackgroundColor(Color.TRANSPARENT);
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        disableWindowMotion(activity);
        State state = new State(activity, stage, surface);
        states.put(activity, state);
        state.register();
        if (entering) {
            surface.setVisibility(View.INVISIBLE);
            surface.getViewTreeObserver().addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener() {
                @Override public boolean onPreDraw() {
                    surface.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (activity.isFinishing() || surface.getParent() != stage) return true;
                    TransitionManager.beginDelayedTransition(stage, axis(true, surface, 360));
                    surface.setVisibility(View.VISIBLE);
                    return false;
                }
            });
        }
    }

    public static void tabs(ViewGroup stage, View previous, View next, boolean forward) {
        if (next == null) return;
        TransitionManager.endTransitions(stage);
        if (previous == null || previous == next || !stage.isLaidOut()) {
            next.setVisibility(View.VISIBLE);next.setAlpha(1);
            next.setTranslationX(0);next.setTranslationY(0);next.setScaleX(1);next.setScaleY(1);
            return;
        }
        previous.setVisibility(View.VISIBLE); next.setVisibility(View.INVISIBLE);
        previous.setTransitionName("runner_previous_tab"); next.setTransitionName("runner_next_tab");
        if (previous instanceof ViewGroup) ((ViewGroup)previous).setTransitionGroup(true);
        if (next instanceof ViewGroup) ((ViewGroup)next).setTransitionGroup(true);
        MaterialSharedAxis motion = new MaterialSharedAxis(MaterialSharedAxis.X, forward);
        configure(motion, stage.getWidth());
        motion.setDuration(300); motion.addTarget(previous); motion.addTarget(next);
        TransitionManager.beginDelayedTransition(stage, motion);
        previous.setVisibility(View.GONE); next.setVisibility(View.VISIBLE);
    }

    private static MaterialSharedAxis axis(boolean forward, View target, long duration) {
        MaterialSharedAxis motion = new MaterialSharedAxis(MaterialSharedAxis.X, forward);
        configure(motion, target.getWidth());
        motion.setDuration(duration); motion.addTarget(target);
        return motion;
    }

    private static void configure(MaterialSharedAxis motion, int width) {
        // PixelPlayer's push/pop emphasizes horizontal travel plus subtle depth.
        // Use official Material providers on Views, without importing its Compose navigation.
        if (motion.getPrimaryAnimatorProvider() instanceof com.google.android.material.transition.SlideDistanceProvider)
            ((com.google.android.material.transition.SlideDistanceProvider)motion.getPrimaryAnimatorProvider()).setSlideDistance(Math.max(1,width/3));
        com.google.android.material.transition.ScaleProvider depth = new com.google.android.material.transition.ScaleProvider();
        depth.setIncomingStartScale(.96f); depth.setIncomingEndScale(1f);
        depth.setOutgoingStartScale(1f); depth.setOutgoingEndScale(.96f);
        motion.addAdditionalAnimatorProvider(depth);
    }

    public static boolean finish(Activity activity, Runnable finish) {
        State state = states.get(activity);
        if (state != null && state.surface.getParent() != state.stage) { install(activity); state=states.get(activity); }
        if (activity.isFinishing() || state == null || !state.surface.isLaidOut()) return false;
        if (state.closing) return true;
        state.closing = true;
        TransitionManager.endTransitions(state.stage);
        Transition transition = axis(false, state.surface, 300);
        transition.addListener(new TransitionListenerAdapter() {
            private boolean done;
            private void complete() { if (!done) { done = true; finish.run(); } }
            @Override public void onTransitionEnd(Transition t) { complete(); }
            @Override public void onTransitionCancel(Transition t) { complete(); }
        });
        TransitionManager.beginDelayedTransition(state.stage, transition);
        state.surface.setVisibility(View.INVISIBLE);
        return true;
    }

    public static void destroy(Activity activity) {
        State state = states.remove(activity);
        if (state != null) state.unregister();
    }

    public static void disableWindowMotion(Activity activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0);
        } else activity.overridePendingTransition(0, 0);
    }

    private static final class State {
        final Activity activity;
        final FrameLayout stage;
        final ClippableRoundedCornerLayout surface;
        final MaterialMainContainerBackHelper helper;
        Object callback;
        boolean closing;
        State(Activity activity, FrameLayout stage, ClippableRoundedCornerLayout surface) {
            this.activity=activity; this.stage=stage; this.surface=surface;
            helper=new MaterialMainContainerBackHelper(surface);
        }
        void register() {
            if (Build.VERSION.SDK_INT >= 34) callback=Api34.register(this);
        }
        void unregister() {
            if (Build.VERSION.SDK_INT >= 34 && callback != null) Api34.unregister(this);
        }
    }

    @android.annotation.TargetApi(34)
    private static final class Api34 {
        static Object register(State state) {
            android.window.OnBackAnimationCallback callback=new android.window.OnBackAnimationCallback() {
                @Override public void onBackStarted(android.window.BackEvent event) {
                    if (state.closing) return;
                    TransitionManager.endTransitions(state.stage);
                    state.helper.startBackProgress(convert(event), null);
                }
                @Override public void onBackProgressed(android.window.BackEvent event) {
                    if (state.closing) return;
                    state.helper.updateBackProgress(convert(event), null, new RunnerUi(state.activity).dp(28));
                }
                @Override public void onBackCancelled() { if (!state.closing) state.helper.cancelBackProgress(null); }
                @Override public void onBackInvoked() {
                    // SharedAxis continues from the helper's current transform.
                    // Resetting it here briefly restores the page before the exit.
                    state.activity.finish();
                }
            };
            state.activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, callback);
            return callback;
        }
        static androidx.activity.BackEventCompat convert(android.window.BackEvent event) {
            return new androidx.activity.BackEventCompat(event.getTouchX(), event.getTouchY(), event.getProgress(), event.getSwipeEdge());
        }
        static void unregister(State state) {
            state.activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((android.window.OnBackAnimationCallback)state.callback);
        }
    }
}
