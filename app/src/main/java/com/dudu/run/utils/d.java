package com.dudu.run.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
public class d {
    private static int a = 4;
    static class a implements Animation.AnimationListener {
        final /* synthetic */ b a;
        final /* synthetic */ TextView b;

        a(b bVar, TextView textView) {
            this.a = bVar;
            this.b = textView;
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            this.b.setVisibility(8);
            this.a.M();
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
            d.b();
            if (d.a == 0) {
                this.b.setTextSize(1, 96.0f);
                this.b.setText("Go");
            } else {
                this.b.setText(String.valueOf(d.a));
            }
            this.a.I();
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
            this.a.start();
        }
    }
    public interface b {
        void I();

        void M();

        void start();
    }

    static /* synthetic */ int b() {
        int i = a - 1;
        a = i;
        return i;
    }

    private static <T extends TextView> void c(T t, int i, b bVar) {
        a = i - 1;
        t.setTextSize(1, 96.0f);
        t.setText(String.valueOf(a));
        t.setVisibility(0);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1.3f, 0.1f, 1.3f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setRepeatCount(a);
        alphaAnimation.setRepeatCount(a);
        alphaAnimation.setDuration(1000L);
        scaleAnimation.setDuration(1000L);
        scaleAnimation.setAnimationListener(new a(bVar, t));
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        t.startAnimation(animationSet);
    }

    public static <T extends TextView> void d(T t, b bVar) {
        c(t, 4, bVar);
    }
}
